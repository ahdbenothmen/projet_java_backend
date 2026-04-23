package com.universite.config;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void init() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "cin VARCHAR(20) PRIMARY KEY," +
                "nom VARCHAR(100) NOT NULL," +
                "prenom VARCHAR(100) NOT NULL," +
                "email VARCHAR(150) UNIQUE NOT NULL," +
                "password VARCHAR(255) NOT NULL," +
                "adresse VARCHAR(255)," +
                "telephone VARCHAR(20)," +
                "photo_etd VARCHAR(500)," +
                "photo_cin VARCHAR(500)," +
                "demande_status ENUM('EN_ATTENTE','APPROUVE','REJETE') DEFAULT 'EN_ATTENTE'," +
                "role ENUM('ETUDIANT','PROFESSEUR','ADMIN') NOT NULL," +
                "date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        String createEtudiants = "CREATE TABLE IF NOT EXISTS etudiants (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "cin VARCHAR(20) NOT NULL," +
                "niveau VARCHAR(50)," +
                "FOREIGN KEY (cin) REFERENCES users(cin) ON DELETE CASCADE" +
                ")";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createUsers);
            System.out.println("✅ Table users créée !");
            stmt.executeUpdate(createEtudiants);
            System.out.println("✅ Table etudiants créée !");
        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }
    }
}