package clinicoop2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Patient {
    private int id;
    private String name;
    private String birthDate;
    private String address;
    private String phone;

    private String email;

    private String emergencycontact;

    public Patient(int id, String name, String birthDate, String address, String phone, String email, String emergencycontact ) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.address = address;
        this.phone = phone;
        this.email=email;
        this.emergencycontact=emergencycontact;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getBirthDate() { return birthDate; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }

    public static List<Patient> searchPatients(String query) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT * FROM patients WHERE name LIKE ? OR phone LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, "%" + query + "%");
        stmt.setString(2, "%" + query + "%");
        ResultSet rs = stmt.executeQuery();

        List<Patient> results = new ArrayList<>();
        while (rs.next()) {
            results.add(new Patient(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("birth_date"),
                    rs.getString("address"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("emergencycontact")

            ));
        }
        conn.close();
        return results;
    }

    public static boolean addPatient(Patient patient) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "INSERT INTO patients (name, birth_date, address, phone) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, patient.getName());
        stmt.setString(2, patient.getBirthDate());
        stmt.setString(3, patient.getAddress());
        stmt.setString(4, patient.getPhone());
        int rows = stmt.executeUpdate();
        conn.close();
        return rows > 0;
    }
}
