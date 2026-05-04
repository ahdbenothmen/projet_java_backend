package com.universite.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.universite.config.DBConnection;
import com.universite.util.TokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/etudiant/profile")
public class ProfilServlet extends HttpServlet {

    private final Gson gson = new Gson();

    private String getCinFromToken(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            resp.setStatus(401);
            resp.getWriter().write("{\"success\":false,\"message\":\"Token manquant\"}");
            return null;
        }
        try {
            String email = TokenUtil.verifyToken(auth.substring(7));
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "SELECT cin FROM user WHERE email = ? AND role = 'ETUDIANT'")) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getString("cin");
                resp.setStatus(403);
                resp.getWriter().write("{\"success\":false,\"message\":\"Accès refusé\"}");
                return null;
            }
        } catch (Exception e) {
            resp.setStatus(401);
            resp.getWriter().write("{\"success\":false,\"message\":\"Token invalide\"}");
            return null;
        }
    }

    // GET — récupérer le profil via token
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String cin = getCinFromToken(req, resp);
        if (cin == null) return;

        try (Connection conn = DBConnection.getConnection()) {
            String sql =
                "SELECT u.cin, u.nom, u.prenom, u.email, u.adresse, u.telephone, " +
                "       u.demandestatus, " +
                "       e.niveau, e.speciality AS specialite " +
                "FROM user u " +
                "JOIN etudiant e ON u.cin = e.cin " +
                "WHERE u.cin = ? AND u.role = 'ETUDIANT'";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cin);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("cin",        rs.getString("cin"));
                    data.put("nom",        rs.getString("nom"));
                    data.put("prenom",     rs.getString("prenom"));
                    data.put("email",      rs.getString("email"));
                    data.put("adresse",    rs.getString("adresse")    != null ? rs.getString("adresse")    : "");
                    data.put("telephone",  rs.getString("telephone")  != null ? rs.getString("telephone")  : "");
                    data.put("niveau",     rs.getString("niveau")     != null ? rs.getString("niveau")     : "");
                    data.put("specialite", rs.getString("specialite") != null ? rs.getString("specialite") : "");
                    data.put("statut",     rs.getString("demandestatus"));
                    resp.getWriter().write(gson.toJson(data));
                } else {
                    resp.setStatus(404);
                    resp.getWriter().write("{\"success\":false,\"message\":\"Étudiant non trouvé\"}");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"success\":false,\"message\":\"Erreur serveur: " + e.getMessage() + "\"}");
        }
    }

    // PUT — modifier le profil
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String cin = getCinFromToken(req, resp);
        if (cin == null) return;

        try {
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            Map<?, ?> body    = gson.fromJson(sb.toString(), Map.class);
            String nom        = (String) body.get("nom");
            String prenom     = (String) body.get("prenom");
            String email      = (String) body.get("email");
            String adresse    = (String) body.get("adresse");
            String telephone  = (String) body.get("telephone");
            String niveau     = (String) body.get("niveau");
            String specialite = (String) body.get("specialite");

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Mise à jour table user
                    String sqlUser =
                        "UPDATE user SET nom=?, prenom=?, email=?, adresse=?, telephone=? WHERE cin=?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                        ps.setString(1, nom);
                        ps.setString(2, prenom);
                        ps.setString(3, email);
                        ps.setString(4, adresse);
                        ps.setString(5, telephone);
                        ps.setString(6, cin);
                        ps.executeUpdate();
                    }

                    // Mise à jour table etudiant
                    String sqlEtudiant =
                        "UPDATE etudiant SET niveau=?, speciality=? WHERE cin=?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlEtudiant)) {
                        ps.setString(1, niveau);
                        ps.setString(2, specialite);
                        ps.setString(3, cin);
                        ps.executeUpdate();
                    }

                    conn.commit();
                    resp.getWriter().write("{\"success\":true,\"message\":\"Profil mis à jour avec succès !\"}");

                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"success\":false,\"message\":\"Erreur serveur: " + e.getMessage() + "\"}");
        }
    }
    @Override
protected void doOptions(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    response.setStatus(HttpServletResponse.SC_OK);
}
}