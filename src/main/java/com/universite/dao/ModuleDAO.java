package com.universite.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.universite.config.DBConnection;

/**
 * DAO pour les modules et leurs prérequis.
 */
public class ModuleDAO {

    /**
     * Récupère tous les modules avec leurs prérequis.
     * Retourne une liste de Map prête à sérialiser en JSON.
     */
    public List<Map<String, Object>> getAllModules() throws SQLException {
String sql = "SELECT m.id, m.nom, m.coefficient, m.note, " +
             "       m.admin_id, m.professeur_cin, " +
             "       u.nom AS professeur_nom, u.prenom AS professeur_prenom " +
             "FROM module m " +
             "LEFT JOIN user u ON u.cin = m.professeur_cin " +  // ← user, pas professeur
             "ORDER BY m.id DESC";

    List<Map<String, Object>> modules = new ArrayList<>();

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Map<String, Object> module = new LinkedHashMap<>();
            module.put("id",               rs.getInt("id"));
            module.put("nom",              rs.getString("nom"));
            module.put("coefficient",      rs.getDouble("coefficient"));
            module.put("note",             rs.getObject("note"));
            module.put("adminId",          rs.getInt("admin_id"));
            module.put("professeurCin",    rs.getString("professeur_cin"));
            module.put("professeurNom",    rs.getString("professeur_nom"));    // ← ajout
            module.put("professeurPrenom", rs.getString("professeur_prenom")); // ← ajout
            module.put("prerequis",        getPrerequisDuModule(conn, rs.getInt("id")));
            modules.add(module);
        }
    }
    return modules;
}

    /**
     * Crée un module et insère ses prérequis dans une transaction.
     * @return l'id du module créé
     */
    public int creerModule(String nom, double coefficient, int adminId,
                           String professeurCin,
                           List<String> prerequisNoms,
                           List<Boolean> prerequisObl) throws SQLException {

        String sqlModule = "INSERT INTO module (nom, coefficient, admin_id, professeur_cin) " +
                           "VALUES (?, ?, ?, ?)";
        String sqlPrereq = "INSERT INTO prerequis (nom, isObligatoire, module_id) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int moduleId;

                // 1. Insérer le module
                try (PreparedStatement ps = conn.prepareStatement(
                        sqlModule, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, nom);
                    ps.setDouble(2, coefficient);
                    ps.setInt(3, adminId);
                    ps.setString(4, professeurCin != null ? professeurCin : "");
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    if (!keys.next()) throw new SQLException("Impossible d'obtenir l'id du module");
                    moduleId = keys.getInt(1);
                }

                // 2. Insérer les prérequis
                if (prerequisNoms != null && !prerequisNoms.isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement(sqlPrereq)) {
                        for (int i = 0; i < prerequisNoms.size(); i++) {
                            String pNom = prerequisNoms.get(i).trim();
                            if (pNom.isEmpty()) continue;
                            boolean obl = (prerequisObl != null && i < prerequisObl.size())
                                          && prerequisObl.get(i);
                            ps.setString(1, pNom);
                            ps.setBoolean(2, obl);
                            ps.setInt(3, moduleId);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                conn.commit();
                return moduleId;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Supprime un module (la cascade supprime ses prérequis automatiquement).
     */
    public void supprimerModule(int moduleId) throws SQLException {
        String sql = "DELETE FROM module WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            ps.executeUpdate();
        }
    }

  public void modifierModule(int id, String nom, double coefficient, String profCin,
                        List<String> prerequisNoms, List<Boolean> prerequisObl) throws Exception {

    String sqlUpdate = "UPDATE module SET nom = ?, coefficient = ?, professeur_cin = ? WHERE id = ?";
    String sqlDelete = "DELETE FROM prerequis WHERE module_id = ?";
    String sqlInsert = "INSERT INTO prerequis (nom, isObligatoire, module_id) VALUES (?, ?, ?)";

    try (Connection conn = DBConnection.getConnection()) {  // ← DBConnection comme partout
        conn.setAutoCommit(false);
        try {
            // Mettre à jour le module
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, nom);
                ps.setDouble(2, coefficient);
                ps.setString(3, profCin != null ? profCin : "");
                ps.setInt(4, id);
                ps.executeUpdate();
            }

            // Supprimer les anciens prérequis
            try (PreparedStatement del = conn.prepareStatement(sqlDelete)) {
                del.setInt(1, id);
                del.executeUpdate();
            }

            // Réinsérer les nouveaux prérequis
            if (prerequisNoms != null && !prerequisNoms.isEmpty()) {
                try (PreparedStatement ins = conn.prepareStatement(sqlInsert)) {
                    for (int i = 0; i < prerequisNoms.size(); i++) {
                        String pNom = prerequisNoms.get(i).trim();
                        if (pNom.isEmpty()) continue;
                        boolean obl = prerequisObl != null && i < prerequisObl.size() && prerequisObl.get(i);
                        ins.setString(1, pNom);
                        ins.setBoolean(2, obl);
                        ins.setInt(3, id);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }

            conn.commit();

        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
        // pas besoin de finally pour setAutoCommit/close → try-with-resources s'en charge
    }
}




    // ── Privé ────────────────────────────────────────────────────────────────

    private List<Map<String, Object>> getPrerequisDuModule(Connection conn, int moduleId)
            throws SQLException {
        String sql = "SELECT id, nom, isObligatoire FROM prerequis WHERE module_id = ? ORDER BY id";
        List<Map<String, Object>> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("id",            rs.getInt("id"));
                p.put("nom",           rs.getString("nom"));
                p.put("isObligatoire", rs.getBoolean("isObligatoire"));
                list.add(p);
            }
        }
        return list;
    }
}