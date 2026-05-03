package com.universite.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.universite.dao.ModuleDAO;
import com.universite.util.TokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet REST pour la gestion des modules et leurs prérequis.
 *
 * GET    /api/modules           → liste tous les modules (avec prérequis)
 * POST   /api/modules           → crée un module + ses prérequis
 * DELETE /api/modules?id=X      → supprime un module (cascade sur prérequis)
 */
@WebServlet("/api/modules")
public class ModuleServlet extends HttpServlet {

    private final ModuleDAO moduleDAO = new ModuleDAO();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setCors(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    /* ── GET : liste des modules ── */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setCors(resp);
        resp.setContentType("application/json;charset=UTF-8");
                 if (!verifierToken(req, resp)) return;
        try {
            List<Map<String, Object>> modules = moduleDAO.getAllModules();
            resp.getWriter().write(toJsonArray(modules));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    /* ── POST : créer un module avec prérequis ── */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setCors(resp);
        resp.setContentType("application/json;charset=UTF-8");
        //if (!verifierToken(req, resp)) return;
        String body = lireBody(req);

        String nom         = extraireValeur(body, "nom");
        String coeffStr    = extraireValeur(body, "coefficient");
        String adminIdStr  = extraireValeur(body, "adminId");
        String profCin     = extraireValeur(body, "professeurCin");

        if (nom == null || nom.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Le nom du module est obligatoire\"}");
            return;
        }

        double coefficient = 0;
        try { coefficient = Double.parseDouble(coeffStr != null ? coeffStr : "0"); }
        catch (NumberFormatException ignored) {}

        int adminId = 1; // par défaut admin principal
        try { adminId = Integer.parseInt(adminIdStr != null ? adminIdStr : "1"); }
        catch (NumberFormatException ignored) {}

        // Extraction tableau prerequis : ["nom1","nom2",...]
        List<String> prerequisNoms = extraireTableauStrings(body, "prerequis");

        // Extraction tableau isObligatoire : [true,false,...]
        List<Boolean> prerequisObl = extraireTableauBooleans(body, "isObligatoire");

        try {
            int moduleId = moduleDAO.creerModule(nom, coefficient, adminId, profCin, prerequisNoms, prerequisObl);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"success\":true,\"moduleId\":" + moduleId + "}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    /* ── DELETE : supprimer un module ── */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        setCors(resp);
        resp.setContentType("application/json;charset=UTF-8");
                 if (!verifierToken(req, resp)) return;
        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Paramètre id manquant\"}");
            return;
        }

        try {
            moduleDAO.supprimerModule(Integer.parseInt(idStr));
            resp.getWriter().write("{\"success\":true}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    /* ── PUT : modifier un module ── */
@Override
protected void doPut(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
    setCors(resp);
    resp.setContentType("application/json;charset=UTF-8");
    if (!verifierToken(req, resp)) return;

    String body = lireBody(req);

    String idStr    = extraireValeur(body, "id");
    String nom      = extraireValeur(body, "nom");
    String coeffStr = extraireValeur(body, "coefficient");
    String profCin  = extraireValeur(body, "professeurCin");

    if (idStr == null) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write("{\"success\":false,\"message\":\"Paramètre id manquant\"}");
        return;
    }
    if (nom == null || nom.isBlank()) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write("{\"success\":false,\"message\":\"Le nom du module est obligatoire\"}");
        return;
    }

    double coefficient = 0;
    try { coefficient = Double.parseDouble(coeffStr != null ? coeffStr : "0"); }
    catch (NumberFormatException ignored) {}

    List<String>  prerequisNoms = extraireTableauStrings(body, "prerequis");
    List<Boolean> prerequisObl  = extraireTableauBooleans(body, "isObligatoire");

    try {
        moduleDAO.modifierModule(Integer.parseInt(idStr), nom, coefficient, profCin, prerequisNoms, prerequisObl);
        resp.getWriter().write("{\"success\":true}");
    } catch (Exception e) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.getWriter().write("{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
    }
}

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------


   private boolean verifierToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String authHeader = req.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().write("{\"error\":\"Token manquant\"}");
        return false;
    }
    String token = authHeader.substring(7);
    try {
        String email = TokenUtil.verifyToken(token); // retourne email ou lève exception
        if (email == null || email.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Token invalide\"}");
            return false;
        }
        return true;
    } catch (Exception e) {
        // JWT expiré, signature invalide, token malformé → auth0 lève une exception
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().write("{\"error\":\"Token invalide ou expiré\"}");
        return false;
    }
}
    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin",  "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT,  OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private String lireBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    /** Extrait la valeur d'une clé JSON simple (string ou number). */
    private String extraireValeur(String json, String cle) {
        String motif = "\"" + cle + "\"";
        int idx = json.indexOf(motif);
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx + motif.length());
        if (colon == -1) return null;
        // Sauter les espaces
        int start = colon + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (start >= json.length()) return null;
        char first = json.charAt(start);
        if (first == '"') {
            int end = json.indexOf('"', start + 1);
            return end == -1 ? null : json.substring(start + 1, end);
        } else {
            // nombre ou booléen
            int end = start;
            while (end < json.length() && ",}\n\r ".indexOf(json.charAt(end)) == -1) end++;
            return json.substring(start, end).trim();
        }
    }

    /** Extrait un tableau JSON de strings : ["a","b"] */
    private List<String> extraireTableauStrings(String json, String cle) {
        List<String> result = new java.util.ArrayList<>();
        String motif = "\"" + cle + "\"";
        int idx = json.indexOf(motif);
        if (idx == -1) return result;
        int debut = json.indexOf("[", idx);
        int fin   = json.indexOf("]", debut);
        if (debut == -1 || fin == -1) return result;
        String content = json.substring(debut + 1, fin);
        for (String part : content.split(",")) {
            String s = part.trim().replaceAll("^\"|\"$", "");
            if (!s.isEmpty()) result.add(s);
        }
        return result;
    }

    /** Extrait un tableau JSON de booléens : [true,false] */
    private List<Boolean> extraireTableauBooleans(String json, String cle) {
        List<Boolean> result = new java.util.ArrayList<>();
        String motif = "\"" + cle + "\"";
        int idx = json.indexOf(motif);
        if (idx == -1) return result;
        int debut = json.indexOf("[", idx);
        int fin   = json.indexOf("]", debut);
        if (debut == -1 || fin == -1) return result;
        String content = json.substring(debut + 1, fin);
        for (String part : content.split(",")) {
            result.add("true".equalsIgnoreCase(part.trim()));
        }
        return result;
    }

    /** Sérialise une liste de Map en tableau JSON. */
    private String toJsonArray(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(toJsonObject(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    @SuppressWarnings("unchecked")
    private String toJsonObject(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            sb.append("\"").append(e.getKey()).append("\":");
            Object v = e.getValue();
            if (v == null)                          sb.append("null");
            else if (v instanceof List)             sb.append(toJsonArray((List<Map<String, Object>>) v));
            else if (v instanceof String)           sb.append("\"").append(escape((String) v)).append("\"");
            else                                    sb.append(v);
            if (i++ < map.size() - 1) sb.append(",");
        }
        return sb.append("}").toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}