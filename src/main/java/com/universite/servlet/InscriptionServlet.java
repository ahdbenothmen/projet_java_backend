package com.universite.servlet;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import com.google.gson.Gson;
import com.universite.model.Etudiant;
import com.universite.service.EtudiantService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class InscriptionServlet extends HttpServlet {

    private EtudiantService service = new EtudiantService();
    private Gson gson = new Gson();
    private String uploadDir = "C:/uploads/";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Files.createDirectories(Paths.get(uploadDir));

            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List<FileItem> items = upload.parseRequest(request);

            Map<String, String> fields = new HashMap<>();
            Map<String, FileItem> files = new HashMap<>();

            for (FileItem item : items) {
                if (item.isFormField()) {
                    fields.put(item.getFieldName(), item.getString("UTF-8"));
                } else {
                    files.put(item.getFieldName(), item);
                }
            }

            // Log pour debug
            System.out.println("=== CHAMPS REÇUS ===");
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }

            Etudiant etudiant = new Etudiant();
            etudiant.setCin(fields.get("cin"));
            etudiant.setNom(fields.get("nom"));
            etudiant.setPrenom(fields.get("prenom"));
            etudiant.setEmail(fields.get("email"));
            etudiant.setPassword(fields.get("password"));
            etudiant.setAdresse(fields.getOrDefault("adresse", ""));
            etudiant.setTelephone(fields.getOrDefault("telephone", ""));
            etudiant.setNiveau(fields.get("niveau"));

            String nomPhotoCin = saveFile(files.get("photoCin"));
            String nomPhotoEtd = saveFile(files.get("photoEtd"));
            etudiant.setPhotoCin(nomPhotoCin != null ? nomPhotoCin : "photo_cin.jpg");
            etudiant.setPhotoEtd(nomPhotoEtd != null ? nomPhotoEtd : "photo_etd.jpg");

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

    private String saveFile(FileItem item) throws Exception {
        if (item == null || item.getSize() == 0) return null;
        String fileName = UUID.randomUUID() + "_" + item.getName();
        File file = new File(uploadDir + fileName);
        item.write(file);
        return fileName;
    }
}