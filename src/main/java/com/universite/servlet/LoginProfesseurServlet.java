package com.universite.servlet;

import com.universite.dao.ProfesseurDAO;
import com.universite.model.Professeur;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import org.json.JSONObject;

@WebServlet("/api/login/professeur")
public class LoginProfesseurServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Access-Control-Allow-Origin", "*");

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = req.getReader().readLine()) != null)
            sb.append(line);

        try {
            JSONObject body = new JSONObject(sb.toString());
            String email    = body.getString("email");
            String password = body.getString("password");

            Professeur p = new ProfesseurDAO().login(email, password);

            if (p == null) {
                res.setStatus(401);
                res.getWriter().write(
                    "{\"success\":false,\"message\":\"Email ou mot de passe incorrect.\"}"
                );
                return;
            }

            String statut = p.getStatut() == null ? "" : p.getStatut().trim();
            System.out.println("=== STATUT RECU: '" + statut + "' ===");

            if (statut.equals("en attente")) {
                res.getWriter().write(
                    "{\"success\":false,\"message\":\"Votre compte est en attente de vérification par l'administrateur. Veuillez patienter.\"}"
                );
            } else if (statut.equals("en cours")) {
                res.getWriter().write(
                    "{\"success\":false,\"message\":\"Votre demande est en cours de traitement par l'administrateur.\"}"
                );
            } else if (statut.equals("rejete")) {
                res.getWriter().write(
                    "{\"success\":false,\"message\":\"Votre compte a été rejeté. Contactez l'administration.\"}"
                );
            } else if (statut.equals("approuve")) {
                res.getWriter().write(
                    "{\"success\":true," +
                    "\"nom\":\"" + p.getNom() + "\"," +
                    "\"prenom\":\"" + p.getPrenom() + "\"," +
                    "\"cin\":\"" + p.getCin() + "\"}"
                );
            } else {
                res.getWriter().write(
                    "{\"success\":false,\"message\":\"Statut inconnu: " + statut + ". Contactez l'administration.\"}"
                );
            }

        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(
                "{\"success\":false,\"message\":\"Erreur serveur: " + e.getMessage() + "\"}"
            );
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "*");
        res.setStatus(200);
    }
}