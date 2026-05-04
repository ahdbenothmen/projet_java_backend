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

@WebServlet("/api/admin/stats")
public class AdminStatsServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setCors(resp);
        resp.setStatus(200);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setCors(resp);
        resp.setContentType("application/json;charset=UTF-8");
        if (!verifierToken(req, resp)) return;

        try (Connection conn = DBConnection.getConnection()) {

            // ── Profs ──
            int profsApprouves = count(conn,
                "SELECT COUNT(*) FROM user WHERE role='PROFESSEUR' AND demandestatus='approuve'");
            int profsRejetes = count(conn,
                "SELECT COUNT(*) FROM user WHERE role='PROFESSEUR' AND demandestatus='rejete'");
            int profsAttente = count(conn,
                "SELECT COUNT(*) FROM user WHERE role='PROFESSEUR' AND demandestatus='en_attente'");

            // ── Étudiants ──
            int etuApprouves = count(conn,
                "SELECT COUNT(*) FROM user WHERE role='ETUDIANT' AND demandestatus='approuve'");
            int etuRejetes = count(conn,
                "SELECT COUNT(*) FROM user WHERE role='ETUDIANT' AND demandestatus='rejete'");
            int etuAttente = count(conn,
                "SELECT COUNT(*) FROM user WHERE role='ETUDIANT' AND demandestatus='en_attente'");

            // ── Modules ──
            int totalModules = count(conn, "SELECT COUNT(*) FROM module");

            resp.getWriter().write(
                "{" +
                "\"profsApprouves\":"  + profsApprouves + "," +
                "\"profsRejetes\":"    + profsRejetes   + "," +
                "\"profsAttente\":"    + profsAttente   + "," +
                "\"etuApprouves\":"    + etuApprouves   + "," +
                "\"etuRejetes\":"      + etuRejetes     + "," +
                "\"etuAttente\":"      + etuAttente     + "," +
                "\"totalModules\":"    + totalModules   +
                "}"
            );

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private int count(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private boolean verifierToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"Token manquant\"}");
            return false;
        }
        try {
            String email = TokenUtil.verifyToken(auth.substring(7));
            if (email == null || email.isEmpty()) {
                resp.setStatus(401);
                resp.getWriter().write("{\"error\":\"Token invalide\"}");
                return false;
            }
            return true;
        } catch (Exception e) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"Token invalide ou expiré\"}");
            return false;
        }
    }

    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin",  "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}