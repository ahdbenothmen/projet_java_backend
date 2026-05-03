package com.universite.servlet;

import com.universite.config.DBConnection;
import com.universite.util.TokenUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

@WebServlet("/api/modules/professeur")
public class ModulesProfesseurServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "*");

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setStatus(401);
            res.getWriter().write("{\"error\":\"Token manquant\"}");
            return;
        }
        try {
            String email = TokenUtil.verifyToken(authHeader.substring(7));
            if (email == null || email.isEmpty()) {
                res.setStatus(401);
                res.getWriter().write("{\"error\":\"Token invalide\"}");
                return;
            }
        } catch (Exception e) {
            res.setStatus(401);
            res.getWriter().write("{\"error\":\"Token invalide\"}");
            return;
        }

        String cin = req.getParameter("cin");
        if (cin == null || cin.isEmpty()) {
            res.getWriter().write("[]");
            return;
        }

        String sql =
            "SELECT m.id, m.nom, m.coefficient, " +
            "       COUNT(em.etudiant_cin) AS nbEtudiants " +
            "FROM module m " +
            "LEFT JOIN etudiant_module em ON em.module_id = m.id " +
            "WHERE m.professeur_cin = ? " +
            "GROUP BY m.id, m.nom, m.coefficient";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cin);
            ResultSet rs = ps.executeQuery();

            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                first = false;
                json.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"nom\":\"").append(escape(rs.getString("nom"))).append("\",")
                    .append("\"coefficient\":").append(rs.getDouble("coefficient")).append(",")
                    .append("\"nbEtudiants\":").append(rs.getInt("nbEtudiants"))
                    .append("}");
            }
            json.append("]");
            res.getWriter().write(json.toString());

        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "*");
        res.setStatus(200);
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}