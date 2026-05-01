package com.universite.servlet;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import javax.servlet.http.*;

import com.google.gson.Gson;
import com.universite.config.DatabaseConfig;

public class PrerequisServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String moduleId = request.getParameter("moduleId");

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection()) {

            String sql = "SELECT * FROM prerequis WHERE module_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(moduleId));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getInt("id"));
                p.put("nom", rs.getString("nom"));
                p.put("obligatoire", rs.getBoolean("isObligatoire"));
                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(list));
    }
}