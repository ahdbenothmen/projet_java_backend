package com.universite.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL_NO_DB = 
        "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
    
    private static final String URL = 
        "jdbc:mysql://localhost:3306/universite_db?useSSL=false&serverTimezone=UTC";
    
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static Connection getConnectionNoDB() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL_NO_DB, USER, PASSWORD);
    }
}