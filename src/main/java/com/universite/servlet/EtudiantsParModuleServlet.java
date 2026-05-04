package com.universite.servlet;

import java.io.*;
import java.sql.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import com.universite.config.DBConnection;

@WebServlet("/api/etudiants/module")
public class EtudiantsParModuleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "*");

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

        String sql =
            "SELECT e.cin, u.nom, u.prenom, u.email, em.note " +
            "FROM etudiant e " +
            "JOIN user u ON e.cin = u.cin " +
            "JOIN etudiant_module em ON em.etudiant_cin = e.cin " +
            "WHERE em.module_id = ? " +
            "ORDER BY u.nom, u.prenom";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, moduleId);
            ResultSet rs = ps.executeQuery();

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (rs.next()) {
                if (!first) json.append(",");
                first = false;

                double note = rs.getDouble("note");
                boolean noteNull = rs.wasNull();

                json.append("{")
                    .append("\"cin\":\"").append(escape(rs.getString("cin"))).append("\",")
                    .append("\"nom\":\"").append(escape(rs.getString("nom"))).append("\",")
                    .append("\"prenom\":\"").append(escape(rs.getString("prenom"))).append("\",")
                    .append("\"email\":\"").append(escape(rs.getString("email"))).append("\",")
                    .append("\"note\":").append(noteNull ? "null" : note)
                    .append("}");
            }
            json.append("]");
            resp.getWriter().write(json.toString());

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "*");
        resp.setStatus(200);
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}