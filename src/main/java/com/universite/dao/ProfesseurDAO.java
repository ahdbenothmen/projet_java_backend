package com.universite.dao;

import com.universite.config.DBConnection;
import com.universite.model.Professeur;
import java.sql.*;

public class ProfesseurDAO {

    // =====================
    // INSCRIPTION
    // =====================
    public boolean inscrire(Professeur p) throws Exception {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);

        try {
            // 1) Insert dans user
            String sqlUser =
                "INSERT INTO user (cin, nom, prenom, email, password, adresse, telephone, " +
                "photo, photoCin, role, demandestatus) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PROFESSEUR', 'en_attente')"; // ✅ FIXED: underscore

            PreparedStatement ps1 = conn.prepareStatement(sqlUser);
            ps1.setString(1, p.getCin());
            ps1.setString(2, p.getNom());
            ps1.setString(3, p.getPrenom());
            ps1.setString(4, p.getEmail());
            ps1.setString(5, p.getPassword());
            ps1.setString(6, p.getAdresse());
            ps1.setString(7, p.getTelephone());
            ps1.setBytes(8, p.getPhoto());
            ps1.setBytes(9, p.getPhotoCin());
            ps1.executeUpdate();
            ps1.close();

            // 2) Insert dans professeur
            String sqlProf =
                "INSERT INTO professeur (cin, diplomes, speciality, diplome_pdf) " +
                "VALUES (?, ?, ?, ?)";

            PreparedStatement ps2 = conn.prepareStatement(sqlProf);
            ps2.setString(1, p.getCin());
            ps2.setString(2, p.getDiplomes());
            ps2.setString(3, p.getSpeciality());
            ps2.setBytes(4, p.getDiplomePdf());
            ps2.executeUpdate();
            ps2.close();

            conn.commit();
            conn.close();
            System.out.println("=== INSCRIPTION OK: user + professeur ===");
            return true;

        } catch (Exception e) {
            conn.rollback();
            conn.close();
            System.err.println("=== ERREUR INSCRIPTION: " + e.getMessage() + " ===");
            throw e;
        }
    }

    // =====================
    // LOGIN
    // =====================
    public Professeur login(String email, String password) throws Exception {
        Connection conn = DBConnection.getConnection();

        String sql =
            "SELECT u.cin, u.nom, u.prenom, u.email, u.demandestatus, " +
            "p.diplomes AS prof_diplomes, p.speciality AS prof_speciality " +
            "FROM user u LEFT JOIN professeur p ON u.cin = p.cin " +
            "WHERE u.email = ? AND u.password = ? AND u.role = 'PROFESSEUR'";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Professeur p = new Professeur();
            p.setCin(rs.getString("cin"));
            p.setNom(rs.getString("nom"));
            p.setPrenom(rs.getString("prenom"));
            p.setEmail(rs.getString("email"));
            p.setStatut(rs.getString("demandestatus"));
            p.setDiplomes(rs.getString("prof_diplomes"));
            p.setSpeciality(rs.getString("prof_speciality"));

            System.out.println("=== LOGIN: cin=" + p.getCin() + " statut='" + p.getStatut() + "' ===");

            ps.close();
            conn.close();
            return p;
        }

        ps.close();
        conn.close();
        return null;
    }

    // =====================
    // ADMIN — APPROUVER
    // =====================
    public boolean approuver(String cin) throws Exception {
        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE user SET demandestatus = 'approuve' WHERE cin = ?"
        );
        ps.setString(1, cin);
        ps.executeUpdate();
        ps.close();
        conn.close();
        return true;
    }

    // =====================
    // ADMIN — REJETER
    // =====================
    public boolean rejeter(String cin) throws Exception {
        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE user SET demandestatus = 'rejete' WHERE cin = ?"
        );
        ps.setString(1, cin);
        ps.executeUpdate();
        ps.close();
        conn.close();
        return true;
    }

    // =====================
    // ADMIN — METTRE EN COURS
    // =====================
    public boolean mettreEnCours(String cin) throws Exception {
        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE user SET demandestatus = 'en cours' WHERE cin = ?"
        );
        ps.setString(1, cin);
        ps.executeUpdate();
        ps.close();
        conn.close();
        return true;
    }
}