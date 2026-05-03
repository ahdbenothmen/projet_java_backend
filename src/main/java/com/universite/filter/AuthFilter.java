package com.universite.filter;

import java.io.IOException;

import com.universite.util.TokenUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter("/api/*")
public class AuthFilter implements Filter {

    private static final String[] PUBLIC_PATHS = {
        "/api/signin/professeur",
        "/api/signin/etudiant",
        "/api/login/professeur",
        "/api/login/etudiant",
        "/api/admin/login",
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req   = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setHeader("Access-Control-Allow-Origin",  "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // ✅ Laisser passer les preflight OPTIONS sans vérification
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = req.getRequestURI();
        for (String publicPath : PUBLIC_PATHS) {
            if (path.contains(publicPath)) {
                chain.doFilter(req, resp);
                return;
            }
        }

        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            resp.setStatus(401);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"error\":\"Token manquant\"}");
            return;
        }

        String token = authHeader.substring(7);
        try {
            TokenUtil.verifyToken(token);
            chain.doFilter(request, response);
        } catch (Exception e) {
            resp.setStatus(401);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"error\":\"Token invalide\"}");
        }
    }
}