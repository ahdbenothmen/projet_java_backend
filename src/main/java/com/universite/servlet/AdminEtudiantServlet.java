package com.universite.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import com.universite.config.DBConnection;
import com.universite.util.EmailService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/admin/etudiants/*")
public class AdminEtudiantServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String status = req.getParameter("status");
        String q      = req.getParameter("q");
        if (q == null) q = "";
        q = q.trim().toLowerCase();

        StringBuilder sql = new StringBuilder(
            "SELECT u.cin, u.nom, u.prenom, u.email, u.telephone, u.adresse, " +
            "       u.demandestatus, u.date_inscription, u.photo, u.photoCin, " +
            "       e.niveau, e.speciality " +
            "FROM user u " +
            "JOIN etudiant e ON u.cin = e.cin " +
            "WHERE u.role = 'ETUDIANT' "
        );

        if (status != null && !status.isEmpty()) {
            sql.append("AND u.demandestatus = ? ");
        }

        if (!q.isEmpty()) {
            sql.append(
                "AND (" +
                "  LOWER(u.nom)    LIKE ? OR " +
                "  LOWER(u.prenom) LIKE ? OR " +
                "  LOWER(CONCAT(u.prenom,' ',u.nom)) LIKE ? OR " +
                "  LOWER(CONCAT(u.nom,' ',u.prenom)) LIKE ? " +
                ") "
            );
        }

        sql.append("ORDER BY u.date_inscription DESC LIMIT 100");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (status != null && !status.isEmpty()) {
                ps.setString(idx++, status);
            }
            if (!q.isEmpty()) {
                String pattern = "%" + q + "%";
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
            }

            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (rs.next()) {
                if (!first) json.append(",");
                first = false;

                byte[] photo    = rs.getBytes("photo");
                byte[] photoCin = rs.getBytes("photoCin");

                json.append("{");
                json.append("\"cin\":\"");             json.append(escape(rs.getString("cin")));              json.append("\",");
                json.append("\"nom\":\"");             json.append(escape(rs.getString("nom")));              json.append("\",");
                json.append("\"prenom\":\"");          json.append(escape(rs.getString("prenom")));           json.append("\",");
                json.append("\"email\":\"");           json.append(escape(rs.getString("email")));            json.append("\",");
                json.append("\"telephone\":\"");       json.append(escape(rs.getString("telephone")));        json.append("\",");
                json.append("\"adresse\":\"");         json.append(escape(rs.getString("adresse")));          json.append("\",");
                json.append("\"status\":\"");          json.append(escape(rs.getString("demandestatus")));    json.append("\",");
                json.append("\"dateInscription\":\""); json.append(escape(rs.getString("date_inscription"))); json.append("\",");
                json.append("\"niveau\":\"");          json.append(escape(rs.getString("niveau")));           json.append("\",");
                json.append("\"speciality\":\"");      json.append(escape(rs.getString("speciality")));       json.append("\",");
                json.append("\"photo\":");             json.append(toBase64Json(photo,    "image/jpeg"));     json.append(",");
                json.append("\"photoCin\":");          json.append(toBase64Json(photoCin, "image/jpeg"));
                json.append("}");
            }
            json.append("]");
            resp.getWriter().write(json.toString());

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.split("/").length < 3) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"URL invalide\"}");
            return;
        }

        String[] parts = pathInfo.split("/");
        String cin    = parts[1];
        String action = parts[2];

        String newStatus = switch (action) {
            case "approuver" -> "approuve";
            case "rejeter"   -> "rejete";
            default -> null;
        };

        if (newStatus == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Action inconnue\"}");
            return;
        }

        String sqlUpdate = "UPDATE user SET demandestatus = ? WHERE cin = ? AND role = 'ETUDIANT'";
        String sqlSelect = "SELECT nom, prenom, email FROM user WHERE cin = ?";

        try (Connection conn = DBConnection.getConnection()) {

            String nom = "", prenom = "", email = "";
            try (PreparedStatement ps = conn.prepareStatement(sqlSelect)) {
                ps.setString(1, cin);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    nom    = rs.getString("nom");
                    prenom = rs.getString("prenom");
                    email  = rs.getString("email");
                }
            }

            int rows;
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, newStatus);
                ps.setString(2, cin);
                rows = ps.executeUpdate();
            }

            if (rows == 0) {
                resp.setStatus(404);
                resp.getWriter().write("{\"success\":false,\"message\":\"Étudiant introuvable\"}");
                return;
            }

            final String finalEmail  = email;
            final String finalPrenom = prenom;
            final String finalNom    = nom;
            final String finalStatus = newStatus;

            new Thread(() -> {
                try {
                    if ("approuve".equals(finalStatus)) {
                        EmailService.sendApproval(finalEmail, finalPrenom, finalNom, "etudiant");
                    } else {
                        EmailService.sendRejection(finalEmail, finalPrenom, finalNom, "etudiant");
                    }
                } catch (Exception e) {
                    System.err.println("Erreur envoi email: " + e.getMessage());
                }
            }).start();

            resp.getWriter().write("{\"success\":true}");

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private String toBase64Json(byte[] data, String mimeType) {
        if (data == null) return "null";
        return "\"data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(data) + "\"";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}