package com.universite.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.universite.config.DBConnection;
import com.universite.model.Admin;

public class AdminDAO {

    public Admin login(String email, String password) {
        String sql = "SELECT * FROM admin WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("nom_utilisateur")
                );

                if (admin.sAuthentifier(email, password)) {
                    return admin;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}