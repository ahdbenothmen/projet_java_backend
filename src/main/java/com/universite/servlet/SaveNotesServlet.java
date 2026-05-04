package com.universite.servlet;

import java.io.*;
import java.sql.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import com.universite.config.DBConnection;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/api/notes/save")
public class SaveNotesServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Access-Control-Allow-Origin", "*");

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = req.getReader().readLine()) != null) sb.append(line);

        try {
            JSONArray notes = new JSONArray(sb.toString());
            Connection conn = DBConnection.getConnection();

            for (int i = 0; i < notes.length(); i++) {
                JSONObject n = notes.getJSONObject(i);
                int moduleId = n.getInt("moduleId");
                String etudiantCin = n.getString("etudiantCin");
                double noteVal = n.isNull("note") ? -1 : n.getDouble("note");

                String sql = "UPDATE etudiant_module SET note = ? WHERE etudiant_cin = ? AND module_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                if (noteVal >= 0) ps.setDouble(1, noteVal);
                else ps.setNull(1, Types.DECIMAL);
                ps.setString(2, etudiantCin);
                ps.setInt(3, moduleId);
                ps.executeUpdate();
            }
            conn.close();

            res.getWriter().write("{\"success\":true}");
        } catch (Exception e) {
            e.printStackTrace();
            res.setStatus(500);
            res.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
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