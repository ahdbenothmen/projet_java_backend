package com.universite.servlet;

import java.io.BufferedReader;
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

@WebServlet("/api/etudiant/modules/*")
public class EtudiantModuleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Headers", "*");

        String cin = getCinFromToken(req, resp);
        if (cin == null) return;

        String sql =
            "SELECT m.id, m.nom, m.coefficient, " +
            "       u.nom AS prof_nom, u.prenom AS prof_prenom, " +
            "       (SELECT COUNT(*) FROM etudiant_module em WHERE em.etudiant_cin = ? AND em.module_id = m.id) AS inscrit " +
            "FROM module m " +
            "JOIN professeur p ON m.professeur_cin = p.cin " +
            "JOIN user u ON p.cin = u.cin " +
            "ORDER BY m.id";

        String sqlPrereq = "SELECT id, nom, isObligatoire FROM prerequis WHERE module_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cin);
            ResultSet rs = ps.executeQuery();

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (rs.next()) {
                if (!first) json.append(",");
                first = false;

                int moduleId = rs.getInt("id");
                boolean inscrit = rs.getInt("inscrit") > 0;

                StringBuilder prereqs = new StringBuilder("[");
                boolean firstP = true;
                try (PreparedStatement psp = conn.prepareStatement(sqlPrereq)) {
                    psp.setInt(1, moduleId);
                    ResultSet rsp = psp.executeQuery();
                    while (rsp.next()) {
                        if (!firstP) prereqs.append(",");
                        firstP = false;
                        prereqs.append("{")
                            .append("\"id\":").append(rsp.getInt("id")).append(",")
                            .append("\"nom\":\"").append(escape(rsp.getString("nom"))).append("\",")
                            .append("\"obligatoire\":").append(rsp.getBoolean("isObligatoire"))
                            .append("}");
                    }
                }
                prereqs.append("]");

                json.append("{")
                    .append("\"id\":").append(moduleId).append(",")
                    .append("\"nom\":\"").append(escape(rs.getString("nom"))).append("\",")
                    .append("\"coefficient\":").append(rs.getDouble("coefficient")).append(",")
                    .append("\"professeur\":\"").append(escape(rs.getString("prof_prenom"))).append(" ").append(escape(rs.getString("prof_nom"))).append("\",")
                    .append("\"inscrit\":").append(inscrit).append(",")
                    .append("\"prerequis\":").append(prereqs)
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Headers", "*");

        String cin = getCinFromToken(req, resp);
        if (cin == null) return;

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        String body = sb.toString();

        String moduleIdStr = extraireValeur(body, "moduleId");
        if (moduleIdStr == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"success\":false,\"message\":\"moduleId manquant\"}");
            return;
        }

        int moduleId;
        try { moduleId = Integer.parseInt(moduleIdStr); }
        catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"success\":false,\"message\":\"moduleId invalide\"}");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sqlCheck = "SELECT COUNT(*) FROM etudiant_module WHERE etudiant_cin = ? AND module_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ps.setString(1, cin);
                ps.setInt(2, moduleId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    resp.getWriter().write("{\"success\":false,\"message\":\"Déjà inscrit à ce module\"}");
                    return;
                }
            }

            String sqlInsert = "INSERT INTO etudiant_module (etudiant_cin, module_id) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setString(1, cin);
                ps.setInt(2, moduleId);
                ps.executeUpdate();
            }

            resp.getWriter().write("{\"success\":true,\"message\":\"Inscription réussie\"}");

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Headers", "*");

        String cin = getCinFromToken(req, resp);
        if (cin == null) return;

        String moduleIdStr = req.getParameter("moduleId");
        if (moduleIdStr == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"success\":false,\"message\":\"moduleId manquant\"}");
            return;
        }

        int moduleId;
        try { moduleId = Integer.parseInt(moduleIdStr); }
        catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"success\":false,\"message\":\"moduleId invalide\"}");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sqlCheck = "SELECT COUNT(*) FROM etudiant_module WHERE etudiant_cin = ? AND module_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ps.setString(1, cin);
                ps.setInt(2, moduleId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    resp.getWriter().write("{\"success\":false,\"message\":\"Vous n'êtes pas inscrit à ce module\"}");
                    return;
                }
            }

            String sqlDelete = "DELETE FROM etudiant_module WHERE etudiant_cin = ? AND module_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDelete)) {
                ps.setString(1, cin);
                ps.setInt(2, moduleId);
                ps.executeUpdate();
            }

            resp.getWriter().write("{\"success\":true,\"message\":\"Désinscription réussie\"}");

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "*");
        resp.setStatus(200);
    }

    private String getCinFromToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"Token manquant\"}");
            return null;
        }
        try {
            String email = TokenUtil.verifyToken(authHeader.substring(7));
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "SELECT cin FROM user WHERE email = ? AND role = 'ETUDIANT'")) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getString("cin");
                resp.setStatus(403);
                resp.getWriter().write("{\"error\":\"Accès refusé\"}");
                return null;
            }
        } catch (Exception e) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"Token invalide\"}");
            return null;
        }
    }

    private String extraireValeur(String json, String cle) {
        String motif = "\"" + cle + "\"";
        int idx = json.indexOf(motif);
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx + motif.length());
        if (colon == -1) return null;
        int start = colon + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (start >= json.length()) return null;
        char first = json.charAt(start);
        if (first == '"') {
            int end = json.indexOf('"', start + 1);
            return end == -1 ? null : json.substring(start + 1, end);
        } else {
            int end = start;
            while (end < json.length() && ",}\n\r ".indexOf(json.charAt(end)) == -1) end++;
            return json.substring(start, end).trim();
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}