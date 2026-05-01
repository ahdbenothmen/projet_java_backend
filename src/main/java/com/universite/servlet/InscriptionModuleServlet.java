package com.universite.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

public class InscriptionModuleServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Scanner s = new Scanner(request.getInputStream()).useDelimiter("\\A");
        String json = s.hasNext() ? s.next() : "";

        Map<?, ?> body = gson.fromJson(json, Map.class);

        List<Map<String, Object>> prerequis =
                (List<Map<String, Object>>) body.get("prerequis");

        // ❌ Bloquer si obligatoire non coché
        for (Map<String, Object> p : prerequis) {

            boolean obligatoire = (boolean) p.get("obligatoire");
            boolean checked = (boolean) p.get("checked");

            if (obligatoire && !checked) {
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Pré-requis obligatoire non validé\"}"
                );
                return;
            }
        }

        // ✅ OK
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"success\":true,\"message\":\"Inscription réussie\"}"
        );
    }
}