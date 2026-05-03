package com.universite.model;

public class Admin implements Authentifiable {
    private int id;
    private String email;
    private String password;
    private String nomUtilisateur;

    public Admin(int id, String email, String password, String nomUtilisateur) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nomUtilisateur = nomUtilisateur;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    @Override
    public boolean sAuthentifier(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }
}