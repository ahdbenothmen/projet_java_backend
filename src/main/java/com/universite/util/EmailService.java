package com.universite.util;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

    private static final String HOST     = "smtp.gmail.com";
    private static final int    PORT     = 587;
    private static final String USERNAME = "manoubauniversity6@gmail.com";
    private static final String PASSWORD = "wxojxemxfrjcqnos";


    public static void sendEmail(String to, String subject, String htmlBody) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            HOST);
        props.put("mail.smtp.port",            String.valueOf(PORT));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME, "Université de Mannouba"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(htmlBody, "text/html; charset=utf-8");
        Transport.send(message);
    }

    // "professeur" ou "etudiant"
    public static void sendApproval(String to, String prenom, String nom, String role) throws Exception {
        boolean isProf = "professeur".equalsIgnoreCase(role);

        String subject = isProf
            ? "Votre compte professeur a été approuvé"
            : "Votre compte étudiant a été approuvé";

        String loginUrl = isProf
            ? "http://localhost:3000/login/professeur"
            : "http://localhost:3000/login/etudiant";

        String roleLabel = isProf ? "professeur" : "étudiant";

        String body =
            "<div style='font-family:sans-serif;max-width:500px;margin:auto'>" +
            "<div style='background:#185FA5;padding:24px;border-radius:12px 12px 0 0'>" +
            "<h2 style='color:#fff;margin:0'>Université de Mannouba</h2></div>" +
            "<div style='padding:28px;border:1px solid #D3D1C7;border-top:none;border-radius:0 0 12px 12px'>" +
            "<p style='font-size:16px;color:#2C2C2A'>Bonjour <strong>" + prenom + " " + nom + "</strong>,</p>" +
            "<p style='color:#444'>Votre compte " + roleLabel + " a été <strong style='color:#3B6D11'>approuvé</strong> par l'administration.</p>" +
            "<p style='color:#444'>Vous pouvez désormais vous connecter à votre espace " + roleLabel + ".</p>" +
            "<a href='" + loginUrl + "' " +
            "style='display:inline-block;margin-top:16px;padding:12px 28px;" +
            "background:#185FA5;color:#fff;border-radius:8px;text-decoration:none;font-weight:600'>" +
            "Se connecter</a>" +
            "<p style='margin-top:28px;font-size:12px;color:#888'>Université de Mannouba — Administration</p>" +
            "</div></div>";

        sendEmail(to, subject, body);
    }

    public static void sendRejection(String to, String prenom, String nom, String role) throws Exception {
        boolean isProf = "professeur".equalsIgnoreCase(role);

        String subject = isProf
            ? "Décision concernant votre demande de compte professeur"
            : "Décision concernant votre demande de compte étudiant";

        String roleLabel = isProf ? "professeur" : "étudiant";

        String body =
            "<div style='font-family:sans-serif;max-width:500px;margin:auto'>" +
            "<div style='background:#185FA5;padding:24px;border-radius:12px 12px 0 0'>" +
            "<h2 style='color:#fff;margin:0'>Université de Mannouba</h2></div>" +
            "<div style='padding:28px;border:1px solid #D3D1C7;border-top:none;border-radius:0 0 12px 12px'>" +
            "<p style='font-size:16px;color:#2C2C2A'>Bonjour <strong>" + prenom + " " + nom + "</strong>,</p>" +
            "<p style='color:#444'>Après examen de votre dossier, nous sommes au regret de vous informer que " +
            "votre demande de compte " + roleLabel + " a été <strong style='color:#A32D2D'>refusée</strong>.</p>" +
            "<p style='color:#444'>Pour plus d'informations, veuillez contacter l'administration.</p>" +
            "<p style='margin-top:28px;font-size:12px;color:#888'>Université de Mannouba — Administration</p>" +
            "</div></div>";

        sendEmail(to, subject, body);
    }
}
