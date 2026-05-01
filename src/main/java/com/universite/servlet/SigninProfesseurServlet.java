package com.universite.servlet;

import com.universite.dao.ProfesseurDAO;
import com.universite.model.Professeur;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;

@WebServlet("/api/signin/professeur")
@MultipartConfig(maxFileSize = 10485760, maxRequestSize = 20971520)
public class SigninProfesseurServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Access-Control-Allow-Origin", "*");

        try {
            String cin        = req.getParameter("cin");
            String nom        = req.getParameter("nom");
            String prenom     = req.getParameter("prenom");
            String email      = req.getParameter("email");
            String password   = req.getParameter("password");
            String adresse    = req.getParameter("adresse");
            String telephone  = req.getParameter("telephone");
            String diplomes   = req.getParameter("diplomes");
            String speciality = req.getParameter("speciality");

            byte[] photo      = readPart(req.getPart("photo"));
            byte[] photoCin   = readPart(req.getPart("photoCin"));
            byte[] diplomePdf = readPart(req.getPart("diplomePdf"));

            Professeur p = new Professeur();
            p.setCin(cin);
            p.setNom(nom);
            p.setPrenom(prenom);
            p.setEmail(email);
            p.setPassword(password);
            p.setAdresse(adresse);
            p.setTelephone(telephone);
            p.setDiplomes(diplomes);
            p.setSpeciality(speciality);
            p.setPhoto(photo);
            p.setPhotoCin(photoCin);
            p.setDiplomePdf(diplomePdf);

            boolean success = new ProfesseurDAO().inscrire(p);

            if (success) {
                res.getWriter().write("{\"success\":true,\"message\":\"Compte créé avec succès\"}");
            } else {
                res.getWriter().write("{\"success\":false,\"message\":\"Erreur lors de la création\"}");
            }

        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    private byte[] readPart(Part part) throws IOException {
        if (part == null) return null;
        try (InputStream is = part.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) != -1) bos.write(buf, 0, len);
            return bos.toByteArray();
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