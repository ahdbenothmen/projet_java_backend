package com.universite.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import com.google.gson.Gson;
import com.universite.service.EtudiantService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/login/etudiant")  
public class LoginServlet extends HttpServlet {

    private final EtudiantService service = new EtudiantService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            Map<?, ?> body = gson.fromJson(sb.toString(), Map.class);
            String cin      = (String) body.get("cin");
            String password = (String) body.get("password");

            Map<String, Object> result = service.login(cin, password);
            response.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().write("{\"success\":false,\"message\":\"Erreur: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(200);
    }
}