package com.universite.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.universite.config.DBConnection;
import com.universite.util.TokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// GET /api/admin/module-etudiants?moduleId=X
@WebServlet("/api/admin/module-etudiants")
public class ModuleEtudiantsServlet extends HttpServlet {

    

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        if (!verifierToken(req, resp)) return;

        String moduleIdStr = req.getParameter("moduleId");
        if (moduleIdStr == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"moduleId manquant\"}");
            return;
        }

        int moduleId;
        try { moduleId = Integer.parseInt(moduleIdStr); }
        catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"moduleId invalide\"}");
            return;
        }

        // Nombre d'inscrits pour ce module
        String sqlCount =
            "SELECT COUNT(*) FROM etudiant_module WHERE module_id = ?";

        // Liste des étudiants inscrits
        String sqlList =
            "SELECT u.cin, u.nom, u.prenom, u.email, e.niveau " +
            "FROM etudiant_module em " +
            "JOIN user u ON em.etudiant_cin = u.cin " +
            "JOIN etudiant e ON e.cin = u.cin " +
            "WHERE em.module_id = ? " +
            "ORDER BY u.nom, u.prenom";

        try (Connection conn = DBConnection.getConnection()) {

            int nbInscrits = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlCount)) {
                ps.setInt(1, moduleId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) nbInscrits = rs.getInt(1);
            }

            StringBuilder etudiants = new StringBuilder("[");
            boolean first = true;
            try (PreparedStatement ps = conn.prepareStatement(sqlList)) {
                ps.setInt(1, moduleId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (!first) etudiants.append(",");
                    first = false;
                    etudiants.append("{")
                        .append("\"cin\":\"").append(escape(rs.getString("cin"))).append("\",")
                        .append("\"nom\":\"").append(escape(rs.getString("nom"))).append("\",")
                        .append("\"prenom\":\"").append(escape(rs.getString("prenom"))).append("\",")
                        .append("\"email\":\"").append(escape(rs.getString("email"))).append("\",")
                        .append("\"niveau\":\"").append(escape(rs.getString("niveau"))).append("\"")
                        .append("}");
                }
            }
            etudiants.append("]");

            resp.getWriter().write(
                "{\"nbInscrits\":" + nbInscrits + ",\"etudiants\":" + etudiants + "}"
            );

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private boolean verifierToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            resp.setStatus(401); resp.getWriter().write("{\"error\":\"Token manquant\"}"); return false;
        }
        try {
            String email = TokenUtil.verifyToken(auth.substring(7));
            if (email == null || email.isEmpty()) {
                resp.setStatus(401); resp.getWriter().write("{\"error\":\"Token invalide\"}"); return false;
            }
            return true;
        } catch (Exception e) {
            resp.setStatus(401); resp.getWriter().write("{\"error\":\"Token invalide ou expiré\"}"); return false;
        }
    }

    @Override
protected void doOptions(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    response.setStatus(HttpServletResponse.SC_OK);
}

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}