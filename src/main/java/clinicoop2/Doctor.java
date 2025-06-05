package clinicoop2;

import java.sql.*;

public class Doctor {
    private int id;
    private String name;
    private String username;
    private String password;
    private String specialization;
    private String phoneNumber;
    private String email;

    public Doctor(int id, String name, String username, String password,
                  String specialization, String phoneNumber, String email) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.specialization = specialization;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }


    public int getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getSpecialization() { return specialization; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }

    public static int validateDoctorLogin(String username, String password) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT DoctorID FROM doctors WHERE username = ? AND password = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("DoctorID") : -1;
            }
        } finally {
            if (conn != null) conn.close();
        }
    }

    public static void registerDoctor(String name, String username, String password,
                                      String specialization, String phoneNumber, String email) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "INSERT INTO doctors (Name, Username, Password, Specialization, PhoneNumber, Email) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, specialization);
            stmt.setString(5, phoneNumber);
            stmt.setString(6, email);

            stmt.executeUpdate();
        } finally {
            if (conn != null) conn.close();
        }
    }

    public static boolean isUsernameTaken(String username) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM doctors WHERE username = ?";

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