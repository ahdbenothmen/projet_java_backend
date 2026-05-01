package com.universite.config;


import java.sql.Connection;
import java.sql.Statement;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
 
/**
 * Listener Tomcat – crée toutes les tables au démarrage.
 * Ordre respecté pour les clés étrangères :
 * 1. admin
 * 2. user
 * 3. professeur
 * 4. etudiant
 * 5. module
 * 6. etudiant_module (relation s'inscrire)
 * 7. prerequis (relation peut avoir)
 */
@WebListener
public class DatabaseInitializer implements ServletContextListener {
 
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== Démarrage application – initialisation DB ===");
 
        // 1. Créer la base si elle n'existe pas
        try (Connection conn = DBConnection.getConnectionNoDB();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE DATABASE IF NOT EXISTS universite_db " +
                "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
            );
            System.out.println(" Base universite_db créée ou déjà existante.");
        } catch (Exception e) {
            System.err.println(" Erreur création base : " + e.getMessage());
            return;
        }
 
        // 2. Créer les tables
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
 
            creerTableAdmin(stmt);
            creerTableUser(stmt);
            creerTableProfesseur(stmt);
            creerTableEtudiant(stmt);
            creerTableModule(stmt);
            creerTableEtudiantModule(stmt);
            creerTablePrerequis(stmt);
            insererAdminParDefaut(stmt);
 
            System.out.println("=== Initialisation DB terminée avec succès ===");
 
        } catch (Exception e) {
            System.err.println(" Erreur initialisation DB : " + e.getMessage());
            e.printStackTrace();
        }
    }
 
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== Arrêt application ===");
    }
 
    // -------------------------------------------------------------------------
    // Tables
    // -------------------------------------------------------------------------
 
    private static void creerTableAdmin(Statement stmt) throws Exception {
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS admin (" +
            " id INT PRIMARY KEY AUTO_INCREMENT," +
            " email VARCHAR(100) NOT NULL UNIQUE," +
            " password VARCHAR(255) NOT NULL," +
            " nom_utilisateur VARCHAR(100) NOT NULL" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        );
        System.out.println(" Table admin OK.");
    }
 
    private static void creerTableUser(Statement stmt) throws Exception {
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS user (" +
            " cin VARCHAR(8) PRIMARY KEY," +
            " nom VARCHAR(50) NOT NULL," +
            " prenom VARCHAR(50) NOT NULL," +
            " email VARCHAR(150) NOT NULL UNIQUE," +
            " password VARCHAR(255) NOT NULL," +
            " adresse VARCHAR(200)," +
            " telephone VARCHAR(15)," +
            " photo LONGBLOB," +
            " photoCin LONGBLOB," +
            " demandestatus ENUM('en_attente','en_cours','approuve','rejete')" +
            " NOT NULL DEFAULT 'en_attente'," +
            " role ENUM('ETUDIANT','PROFESSEUR') NOT NULL," +
            " date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        );
        System.out.println(" Table user OK.");
    }
 
    private static void creerTableProfesseur(Statement stmt) throws Exception {
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS professeur (" +
            " cin VARCHAR(8) PRIMARY KEY," +
            " diplomes VARCHAR(255)," +
            " speciality VARCHAR(100)," +
            " diplome_pdf LONGBLOB," +
            " FOREIGN KEY (cin) REFERENCES user(cin) ON DELETE CASCADE" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        );
        System.out.println(" Table professeur OK.");
    }
 
    private static void creerTableEtudiant(Statement stmt) throws Exception {
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS etudiant (" +
            " cin VARCHAR(8) PRIMARY KEY," +
            " niveau VARCHAR(50)," +
            " speciality VARCHAR(100)," +
            " FOREIGN KEY (cin) REFERENCES user(cin) ON DELETE CASCADE" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        );
        System.out.println("Table etudiant OK.");
    }
 
   private static void creerTableModule(Statement stmt) throws Exception {
    stmt.executeUpdate(
        "CREATE TABLE IF NOT EXISTS module (" +
        " id INT PRIMARY KEY AUTO_INCREMENT," +
        " nom VARCHAR(100) NOT NULL," +
        " coefficient DECIMAL(4,2)," +
        " note DECIMAL(5,2)," +
        " admin_id INT NOT NULL," +
        " professeur_cin VARCHAR(8) NOT NULL," +
        " FOREIGN KEY (admin_id) REFERENCES admin(id) ON DELETE RESTRICT," +
        " FOREIGN KEY (professeur_cin) REFERENCES professeur(cin) ON DELETE RESTRICT" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
    );
    System.out.println(" Table module OK.");
}
 
    private static void creerTableEtudiantModule(Statement stmt) throws Exception {
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS etudiant_module (" +
            " etudiant_cin VARCHAR(8) NOT NULL," +
            " module_id INT NOT NULL," +
            " PRIMARY KEY (etudiant_cin, module_id)," +
            " FOREIGN KEY (etudiant_cin) REFERENCES etudiant(cin) ON DELETE CASCADE," +
            " FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE CASCADE" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        );
        System.out.println(" Table etudiant_module (s'inscrire) OK.");
    }
 
    private static void creerTablePrerequis(Statement stmt) throws Exception {
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS prerequis (" +
            " id INT PRIMARY KEY AUTO_INCREMENT," +
            " nom VARCHAR(100) NOT NULL," +
            " isObligatoire BOOLEAN NOT NULL DEFAULT FALSE," +
            " module_id INT NOT NULL," +
            " FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE CASCADE" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        );
        System.out.println(" Table prerequis OK.");
    }
 
    private static void insererAdminParDefaut(Statement stmt) throws Exception {
        stmt.executeUpdate(
            "INSERT INTO admin (email, password, nom_utilisateur) " +
            "SELECT 'admin@universite.tn', 'admin123', 'Admin Principal' " +
            "WHERE NOT EXISTS (" +
            " SELECT 1 FROM admin WHERE email = 'admin@universite.tn'" +
            ")"
        );
        System.out.println(" Admin par défaut inséré si inexistant.");
    }
}
