package com.universite.repository;

import java.sql.*;
import com.universite.config.DatabaseConfig;
import com.universite.model.Etudiant;

public class EtudiantRepository {

    public boolean existsByCin(String cin) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE cin = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cin);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public void save(Etudiant e) throws SQLException {
        Connection conn = DatabaseConfig.getConnection();
        conn.setAutoCommit(false);
        try {
            // 1. Insérer dans users
            String sqlUser = "INSERT INTO users (cin, nom, prenom, email, password, adresse, telephone, photo_etd, photo_cin, demande_status, role) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'EN_ATTENTE', 'ETUDIANT')";
            try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                ps.setString(1, e.getCin());
                ps.setString(2, e.getNom());
                ps.setString(3, e.getPrenom());
                ps.setString(4, e.getEmail());
                ps.setString(5, e.getPassword());
                ps.setString(6, e.getAdresse());
                ps.setString(7, e.getTelephone());
                ps.setString(8, e.getPhotoEtd());
                ps.setString(9, e.getPhotoCin());
                ps.executeUpdate();
            }

            // 2. Insérer dans etudiants
            String sqlEtudiant = "INSERT INTO etudiants (cin, niveau) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlEtudiant)) {
                ps.setString(1, e.getCin());
                ps.setString(2, e.getNiveau());
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Étudiant sauvegardé !");

        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    public Etudiant findByCin(String cin) throws SQLException {
        String sql = "SELECT u.*, e.niveau FROM users u " +
                     "JOIN etudiants e ON u.cin = e.cin " +
                     "WHERE u.cin = ? AND u.role = 'ETUDIANT'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cin);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Etudiant etudiant = new Etudiant();
                etudiant.setCin(rs.getString("cin"));
                etudiant.setNom(rs.getString("nom"));
                etudiant.setPrenom(rs.getString("prenom"));
                etudiant.setEmail(rs.getString("email"));
                etudiant.setPassword(rs.getString("password"));
                etudiant.setAdresse(rs.getString("adresse"));
                etudiant.setTelephone(rs.getString("telephone"));
                etudiant.setNiveau(rs.getString("niveau"));
                etudiant.setPhotoCin(rs.getString("photo_cin"));
                etudiant.setPhotoEtd(rs.getString("photo_etd"));
                etudiant.setStatut(rs.getString("demande_status"));
                etudiant.setRole(rs.getString("role"));
                return etudiant;
            }
            return null;
        }
    }
}