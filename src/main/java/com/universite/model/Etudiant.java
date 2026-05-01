package com.universite.model;

public class Etudiant {

    private String cin;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String adresse;
    private String telephone;
    private String niveau;
    private String photoCin;
    private String photoEtd;
    private String statut;
    private String role;
    private String dateInscription;
    private String specialite;
    public Etudiant() {}

    public String getCin() { return cin; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getAdresse() { return adresse; }
    public String getTelephone() { return telephone; }
    public String getNiveau() { return niveau; }
    public String getPhotoCin() { return photoCin; }
    public String getPhotoEtd() { return photoEtd; }
    public String getStatut() { return statut; }
    public String getRole() { return role; }
    public String getSpecialite() { return specialite; }
    public String getDateInscription() { return dateInscription; }

    public void setCin(String cin) { this.cin = cin; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public void setPhotoCin(String photoCin) { this.photoCin = photoCin; }
    public void setPhotoEtd(String photoEtd) { this.photoEtd = photoEtd; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setRole(String role) { this.role = role; }
    public void setDateInscription(String dateInscription) { this.dateInscription = dateInscription; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

}