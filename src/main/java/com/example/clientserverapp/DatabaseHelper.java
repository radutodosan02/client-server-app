package com.example.clientserverapp;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:C:/radu/Projects/Java Projects/client-server-app/src/main/resources/com/example/clientserverapp/tables/users.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static boolean registerUser(String username, String password) {
        // Verifică dacă utilizatorul există deja
        if (userExists(username)) {
            return false;  // Numele de utilizator există deja
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean userExists(String username) {
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();  // Dacă există un rând, utilizatorul există deja
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean authenticateUser(String username, String password) {
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return BCrypt.checkpw(password, rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
