package com.universite.servlet;

import com.universite.dao.AdminDAO;
import com.universite.model.Admin;
import com.universite.util.TokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/api/admin/login")
public class LoginAdminServlet extends HttpServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCors(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCors(resp);
        resp.setContentType("application/json;charset=UTF-8");

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        String body = sb.toString();

        String email = extraireValeur(body, "email");
        String password = extraireValeur(body, "password");

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Champs manquants\"}");
            return;
        }

        Admin admin = adminDAO.login(email, password);

        if (admin != null) {
            resp.setStatus(HttpServletResponse.SC_OK);
           String token = TokenUtil.generateToken(admin.getEmail());

            resp.getWriter().write(
                "{"
                + "\"success\":true,"
                + "\"message\":\"Connexion réussie\","
                + "\"token\":\"" + token + "\","
                + "\"nomUtilisateur\":\"" + admin.getNomUtilisateur() + "\","
                + "\"email\":\"" + admin.getEmail() + "\""
                + "}"
            );
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"success\":false,\"message\":\"Email ou mot de passe incorrect\"}");
        }
    }

    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private String extraireValeur(String json, String cle) {
        String motif = "\"" + cle + "\":";
        int index = json.indexOf(motif);

        if (index == -1) return null;

        int debut = json.indexOf("\"", index + motif.length());
        int fin = json.indexOf("\"", debut + 1);

        if (debut == -1 || fin == -1) return null;

        return json.substring(debut + 1, fin);
    }
}