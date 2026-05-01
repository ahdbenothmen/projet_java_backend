package com.universite.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import com.universite.config.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/professeurs/search")
public class ProfesseurSearchServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setCors(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setCors(resp);
        resp.setContentType("application/json;charset=UTF-8");

        String q = req.getParameter("q");
        if (q == null) q = "";
        q = q.trim().toLowerCase();

        String sql =
    "SELECT u.cin, u.nom, u.prenom, u.photo, p.speciality " +
    "FROM user u " +
    "JOIN professeur p ON u.cin = p.cin " +
    "WHERE u.role = 'PROFESSEUR' " +
    "  AND u.demandestatus = 'approuve' " +   
    "  AND (" +
    "    LOWER(u.nom)       LIKE ? OR " +
    "    LOWER(u.prenom)    LIKE ? OR " +
    "    LOWER(CONCAT(u.prenom,' ',u.nom)) LIKE ? OR " +
    "    LOWER(CONCAT(u.nom,' ',u.prenom)) LIKE ? OR " +
    "    LOWER(p.speciality) LIKE ?" +
    "  ) " +
    "LIMIT 20";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + q + "%";
            for (int i = 1; i <= 5; i++) ps.setString(i, pattern);

            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (rs.next()) {
                if (!first) json.append(",");
                first = false;

                String cin        = escape(rs.getString("cin"));
                String nom        = escape(rs.getString("nom"));
                String prenom     = escape(rs.getString("prenom"));
                String speciality = escape(rs.getString("speciality"));
                byte[] photo      = rs.getBytes("photo");
                String photoB64   = photo != null
                    ? Base64.getEncoder().encodeToString(photo)
                    : null;

                json.append("{")
                    .append("\"cin\":\"").append(cin).append("\",")
                    .append("\"nom\":\"").append(nom).append("\",")
                    .append("\"prenom\":\"").append(prenom).append("\",")
                    .append("\"speciality\":\"").append(speciality).append("\",")
                    .append("\"photo\":").append(photoB64 != null
                        ? "\"data:image/jpeg;base64," + photoB64 + "\""
                        : "null")
                    .append("}");
            }
            json.append("]");
            resp.getWriter().write(json.toString());

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin",  "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"")
                .replace("\n","\\n").replace("\r","\\r");
    }
}