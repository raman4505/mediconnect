package com.mediconnect.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private Database() { }
    public static Connection connect() throws SQLException {
        String url = System.getenv("MEDICONNECT_DB_URL"), user = System.getenv("MEDICONNECT_DB_USER"), password = System.getenv("MEDICONNECT_DB_PASSWORD");
        if (url == null || user == null || password == null) throw new SQLException("MySQL settings are missing. Configure MEDICONNECT_DB_URL, MEDICONNECT_DB_USER, and MEDICONNECT_DB_PASSWORD.");
        return DriverManager.getConnection(url, user, password);
    }
}
