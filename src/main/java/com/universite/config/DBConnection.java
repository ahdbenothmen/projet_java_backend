package com.universite.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe utilitaire — fournit uniquement les connexions JDBC.
 * L'initialisation des tables est gérée par DatabaseInitializer (WebListener).
 */
public class DBConnection {

    private static final String HOST_URL =
        "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
    private static final String DB_URL =
        "jdbc:mysql://localhost:3306/universite_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "lnnaya";


    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver MySQL chargé.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL introuvable : " + e.getMessage());
        }
    }

    /** Connexion vers universite_db */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /** Connexion sans base (pour CREATE DATABASE) */
    public static Connection getConnectionNoDB() throws SQLException {
        return DriverManager.getConnection(HOST_URL, DB_USER, DB_PASSWORD);
    }
}