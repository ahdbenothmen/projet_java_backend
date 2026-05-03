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

@WebServlet("/api/etudiant/profile")
public class EtudiantProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"Token manquant\"}");
            return;
        }

        try {
            String email = TokenUtil.verifyToken(authHeader.substring(7));

            String sql =
                "SELECT u.cin, u.nom, u.prenom, u.email, u.telephone, " +
                "       u.demandestatus, e.niveau, e.speciality " +
                "FROM user u " +
                "JOIN etudiant e ON u.cin = e.cin " +
                "WHERE u.email = ? AND u.role = 'ETUDIANT'";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    resp.getWriter().write("{" +
                        "\"cin\":\""    + escape(rs.getString("cin"))          + "\"," +
                        "\"nom\":\""    + escape(rs.getString("nom"))          + "\"," +
                        "\"prenom\":\"" + escape(rs.getString("prenom"))       + "\"," +
                        "\"email\":\""  + escape(rs.getString("email"))        + "\"," +
                        "\"niveau\":\"" + escape(rs.getString("niveau"))       + "\"," +
                        "\"statut\":\"" + escape(rs.getString("demandestatus"))+ "\"," +
                        "\"speciality\":\"" + escape(rs.getString("speciality")) + "\"" +
                    "}");
                } else {
                    resp.setStatus(404);
                    resp.getWriter().write("{\"error\":\"Étudiant introuvable\"}");
                }
            }
        } catch (Exception e) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"Token invalide\"}");
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}