package com.universite.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;
import com.universite.model.Etudiant;
import com.universite.repository.EtudiantRepository;

public class EtudiantService {

    private EtudiantRepository repository = new EtudiantRepository();

    public Map<String, Object> inscrire(Etudiant etudiant) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Validation
            if (etudiant.getCin() == null || etudiant.getCin().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Le CIN est obligatoire.");
                return result;
            }
            if (etudiant.getEmail() == null || etudiant.getEmail().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "L'email est obligatoire.");
                return result;
            }
            if (etudiant.getPassword() == null || etudiant.getPassword().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Le mot de passe est obligatoire.");
                return result;
            }

            // Vérifier CIN
            if (repository.existsByCin(etudiant.getCin().trim())) {
                result.put("success", false);
                result.put("message", "Ce numéro CIN est déjà utilisé.");
                return result;
            }

            // Vérifier Email
            if (repository.existsByEmail(etudiant.getEmail().trim())) {
                result.put("success", false);
                result.put("message", "Cet email est déjà utilisé.");
                return result;
            }

            // Hasher mot de passe
            String hashedPassword = BCrypt.hashpw(etudiant.getPassword(), BCrypt.gensalt());
            etudiant.setPassword(hashedPassword);
            etudiant.setCin(etudiant.getCin().trim());
            etudiant.setEmail(etudiant.getEmail().trim());

            repository.save(etudiant);

            result.put("success", true);
            result.put("message", "Inscription réussie ! Votre compte est en attente de vérification par l'administrateur.");

        } catch (SQLException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Erreur serveur : " + e.getMessage());
        }
        return result;
    }

    public Map<String, Object> login(String cin, String password) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (cin == null || cin.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Le CIN est obligatoire.");
                return result;
            }

            Etudiant etudiant = repository.findByCin(cin.trim());

            if (etudiant == null) {
                result.put("success", false);
                result.put("message", "CIN ou mot de passe incorrect.");
                return result;
            }

            if (!BCrypt.checkpw(password, etudiant.getPassword())) {
                result.put("success", false);
                result.put("message", "CIN ou mot de passe incorrect.");
                return result;
            }

            if ("EN_ATTENTE".equals(etudiant.getStatut())) {
                result.put("success", false);
                result.put("message", "Votre compte est en attente de vérification par l'administrateur. Veuillez patienter.");
                return result;
            }

            if ("REJETE".equals(etudiant.getStatut())) {
                result.put("success", false);
                result.put("message", "Votre demande d'inscription a été rejetée. Veuillez contacter l'administration.");
                return result;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("cin", etudiant.getCin());
            data.put("nom", etudiant.getNom());
            data.put("prenom", etudiant.getPrenom());
            data.put("email", etudiant.getEmail());
            data.put("niveau", etudiant.getNiveau());
            data.put("statut", etudiant.getStatut());
            data.put("role", etudiant.getRole());

            result.put("success", true);
            result.put("message", "Connexion réussie !");
            result.put("data", data);

        } catch (SQLException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Erreur serveur : " + e.getMessage());
        }
        return result;
    }
}