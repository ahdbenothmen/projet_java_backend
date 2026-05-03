package com.universite.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.universite.config.DBConnection;
import com.universite.model.Etudiant;

public class EtudiantRepository {

    public boolean existsByCin(String cin) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE cin = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cin);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

   public void save(Etudiant e) throws SQLException {
    Connection conn = DBConnection.getConnection();
    conn.setAutoCommit(false);
    try {
        String sqlUser =
            "INSERT INTO user (cin, nom, prenom, email, password, adresse, telephone, photo, photoCin, demandestatus, role) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'en_attente', 'ETUDIANT')";
        try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
            ps.setString(1, e.getCin());
            ps.setString(2, e.getNom());
            ps.setString(3, e.getPrenom());
            ps.setString(4, e.getEmail());
            ps.setString(5, e.getPassword());
            ps.setString(6, e.getAdresse());
            ps.setString(7, e.getTelephone());
            ps.setBytes(8, e.getPhotoEtd());   // ← photo profil
            ps.setBytes(9, e.getPhotoCin());   // ← photo CIN
            ps.executeUpdate();
        }

        String sqlEtudiant = "INSERT INTO etudiant (cin, niveau, speciality) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlEtudiant)) {
            ps.setString(1, e.getCin());
            ps.setString(2, e.getNiveau());
            ps.setString(3, e.getSpecialite());
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
        String sql =
            "SELECT u.*, e.niveau FROM user u " +
            "JOIN etudiant e ON u.cin = e.cin " +
            "WHERE u.cin = ? AND u.role = 'ETUDIANT'";
        try (Connection conn = DBConnection.getConnection();
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
                etudiant.setStatut(rs.getString("demandestatus"));
                etudiant.setRole(rs.getString("role"));
                return etudiant;
            }
            return null;
        }
    }
}