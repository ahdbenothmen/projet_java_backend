package com.universite.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.google.gson.Gson;
import com.universite.model.Etudiant;
import com.universite.service.EtudiantService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/api/signin/etudiant")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize       = 10 * 1024 * 1024,
    maxRequestSize    = 20 * 1024 * 1024
)
public class InscriptionServlet extends HttpServlet {

    private final EtudiantService service = new EtudiantService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            request.setCharacterEncoding("UTF-8");

            Etudiant etudiant = new Etudiant();
            etudiant.setCin(request.getParameter("cin"));
            etudiant.setNom(request.getParameter("nom"));
            etudiant.setPrenom(request.getParameter("prenom"));
            etudiant.setEmail(request.getParameter("email"));
            etudiant.setPassword(request.getParameter("password"));
            etudiant.setAdresse(nvl(request.getParameter("adresse")));
            etudiant.setTelephone(nvl(request.getParameter("telephone")));
            etudiant.setNiveau(request.getParameter("niveau"));
            etudiant.setSpecialite(nvl(request.getParameter("specialite")));

            // ← lire directement en bytes, pas de fichier sur disque
            etudiant.setPhotoCin(readBytes(request.getPart("photoCin")));
            etudiant.setPhotoEtd(readBytes(request.getPart("photoEtd")));

            Map<String, Object> result = service.inscrire(etudiant);
            response.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write("{\"success\":false,\"message\":\"Erreur: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(200);
    }

    private byte[] readBytes(Part part) throws IOException {
        if (part == null || part.getSize() == 0) return null;
        try (InputStream is = part.getInputStream()) {
            return is.readAllBytes();
        }
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }
}