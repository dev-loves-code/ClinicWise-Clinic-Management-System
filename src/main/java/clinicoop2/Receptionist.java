package clinicoop2;

import java.sql.*;

public class Receptionist {
    private int id;
    private String name;
    private String username;
    private String password;

    public Receptionist(int id, String name, String username, String password) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }

    public static boolean validateReceptionistLogin(String username, String password) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT * FROM receptionists WHERE username = ? AND password = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } finally {
            if (conn != null) conn.close();
        }
    }

    public static void registerReceptionist(String name, String username, String password) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "INSERT INTO receptionists (Name, Username, Password) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, username);
            stmt.setString(3, password);

            stmt.executeUpdate();
        } finally {
            if (conn != null) conn.close();
        }
    }

    public static boolean isUsernameTaken(String username) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM receptionists WHERE username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } finally {
            if (conn != null) conn.close();
        }

        return false;
    }
}