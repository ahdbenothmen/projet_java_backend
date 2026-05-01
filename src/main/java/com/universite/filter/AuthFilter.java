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

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // laisser login libre
        if (path.contains("/api/admin/login")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"Token manquant\"}");
            return;
        }

        String token = authHeader.substring(7);

        try {
            TokenUtil.verifyToken(token);
            chain.doFilter(request, response); // OK
        } catch (Exception e) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"Token invalide\"}");
        }
    }
}