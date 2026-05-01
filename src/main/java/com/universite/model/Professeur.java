package com.universite.model;

public class Professeur {
    private String cin;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String adresse;
    private String telephone;
    private String diplomes;
    private String speciality;
    private String statut;
    private byte[] photo;
    private byte[] photoCin;
    private byte[] diplomePdf;

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getDiplomes() { return diplomes; }
    public void setDiplomes(String diplomes) { this.diplomes = diplomes; }
    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }
    public byte[] getPhotoCin() { return photoCin; }
    public void setPhotoCin(byte[] photoCin) { this.photoCin = photoCin; }
    public byte[] getDiplomePdf() { return diplomePdf; }
    public void setDiplomePdf(byte[] diplomePdf) { this.diplomePdf = diplomePdf; }
}