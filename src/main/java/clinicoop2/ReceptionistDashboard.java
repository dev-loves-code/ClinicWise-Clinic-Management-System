package clinicoop2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.File;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import java.sql.*;
import java.time.LocalDate;

public class ReceptionistDashboard {
    private VBox view;

    public ReceptionistDashboard(Stage stage) {
        view = new VBox(15);
        view.setPadding(new Insets(20));

        Label titleLabel = new Label("Receptionist Dashboard");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button addPatientButton = new Button("Add New Patient");
        Button manageAppointmentsButton = new Button("Manage and Check-In Appointments");
        Button manageVisits = new Button("Manage Patients Visits");
        Button financialsButton = new Button("View Financial Records");
        Button patientsListButton = new Button("View Patients List");

        Button openAttachWindowButton = new Button("Attach file to patient");
        Button openAttachedWindow = new Button("View Attached");

        openAttachWindowButton.setOnAction(e -> showAttachFileWindow());
        openAttachedWindow.setOnAction(e->showAttachedFilesWindow());

        addPatientButton.setOnAction(e -> showAddPatient());
        manageAppointmentsButton.setOnAction(e -> showSearchAppointments());
        financialsButton.setOnAction(e -> showFinancialRecords());
        patientsListButton.setOnAction(e -> showPatientsList());
        manageVisits.setOnAction(e -> addVisit());

        view.getChildren().addAll(titleLabel, addPatientButton, manageAppointmentsButton, financialsButton, patientsListButton,manageVisits,openAttachWindowButton,openAttachedWindow);




        Scene scene = new Scene(view, 400, 350);
        stage.setScene(scene);
        stage.setTitle("Receptionist Dashboard");
        stage.show();
    }

    private void showAddPatient() {
        Stage addPatientStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label header = new Label("Add New Patient:");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter Patient Name");

        TextField birthdateField = new TextField();
        birthdateField.setPromptText("Enter Birthdate (YYYY-MM-DD)");

        TextField addressField = new TextField();
        addressField.setPromptText("Enter Address");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Enter Phone Number");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email");

        TextField emergencyContactField = new TextField();
        emergencyContactField.setPromptText("Enter Emergency Contact");

        Button submitButton = new Button("Submit");

        submitButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String birthdate = birthdateField.getText().trim();
            String address = addressField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String emergencyContact = emergencyContactField.getText().trim();

              
            if (name.isEmpty() || birthdate.isEmpty() || address.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "All fields are required!");
                return;
            }

              
            if (!isValidDate(birthdate)) {
                showAlert(Alert.AlertType.ERROR, "Invalid birthdate format! Use YYYY-MM-DD.");
                return;
            }

              
            if (!phone.matches("\\d{10,15}")) {
                showAlert(Alert.AlertType.ERROR, "Invalid phone number! Enter a 10-15 digit number.");
                return;
            }

              
            if (!email.matches("[^@]+@[^@]+\\.[^@]+")) {
                showAlert(Alert.AlertType.ERROR, "Invalid email format!");
                return;
            }

              
            if (!emergencyContact.isEmpty() && !emergencyContact.matches("\\d{10,15}")) {
                showAlert(Alert.AlertType.ERROR, "Invalid emergency contact! Enter a 10-15 digit number.");
                return;
            }

            try {
                Connection conn = DBConnection.getConnection();
                String sql = "INSERT INTO Patients (Name, BirthDate, Address, PhoneNumber, Email, EmergencyContact) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, birthdate);
                stmt.setString(3, address);
                stmt.setString(4, phone);
                stmt.setString(5, email);
                stmt.setString(6, emergencyContact.isEmpty() ? null : emergencyContact);   

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Patient Added Successfully!");
                }

                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(header, nameField, birthdateField, addressField, phoneField, emailField, emergencyContactField, submitButton);
        Scene scene = new Scene(layout, 400, 450);
        addPatientStage.setScene(scene);
        addPatientStage.setTitle("Add Patient");
        addPatientStage.show();
    }

    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private boolean isValidDate(String date) {
          
        String regex = "^\\d{4}-\\d{2}-\\d{2}$";
        if (!date.matches(regex)) {
            return false;
        }

        try {
            String[] parts = date.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

              
            if (month < 1 || month > 12) return false;
            if (day < 1 || day > 31) return false;

              
            if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) return false;
            if (month == 2 && day > 28) return false;

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }



    private void showSearchAppointments() {

        Stage appointmentStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));


        Label header = new Label("Manage Appointments");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

          
        TextField patientNameField = new TextField();
        patientNameField.setPromptText("Enter Patient Name");

        TextField dateField = new TextField();
        dateField.setPromptText("Enter Appointment Date (YYYY-MM-DD)");

        ComboBox<String> typeDropdown = new ComboBox<>();
        typeDropdown.getItems().addAll("Diagnosis", "Vaccination");
        typeDropdown.setPromptText("Select Appointment Type");

        Button searchButton = new Button("Search");
        ListView<String> resultsList = new ListView<>();

        searchButton.setOnAction(e -> {
            resultsList.getItems().clear();
            String patientName = patientNameField.getText();
            String date = dateField.getText();
            String type = typeDropdown.getValue();
            searchAppointments(patientName, date, type, resultsList);
        });

          
        Button addButton = new Button("Add Appointment");
        Button editButton = new Button("Edit Appointment");
        Button deleteButton = new Button("Delete Appointment");
        Button checkInButton = new Button("Check-In Patient");

        addButton.setOnAction(e -> addAppointment());
        editButton.setOnAction(e -> editAppointment1());
        deleteButton.setOnAction(e -> deleteAppointment());
        checkInButton.setOnAction(e -> checkInPatient());

        HBox actionButtons = new HBox(10, addButton, editButton, deleteButton, checkInButton);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));

          
        layout.getChildren().addAll(
                header,
                patientNameField,
                dateField,
                typeDropdown,
                searchButton,
                resultsList,
                actionButtons
        );

        Scene scene = new Scene(layout, 400, 450);
        appointmentStage.setScene(scene);
        appointmentStage.setTitle("Add Patient");
        appointmentStage.show();

    }

    private void searchAppointments(String patientName, String date, String type, ListView<String> resultsList) {
        try {
            Connection conn = DBConnection.getConnection();
            StringBuilder sql = new StringBuilder("SELECT AppointmentID, PatientID, DoctorID, AppointmentType, AppointmentDate FROM appointments WHERE 1=1");

              
            if (!patientName.isEmpty()) {
                sql.append(" AND PatientID IN (SELECT PatientID FROM patients WHERE Name LIKE ?)");
            }
            if (!date.isEmpty()) {
                sql.append(" AND AppointmentDate = ?");
            }
            if (type != null) {
                sql.append(" AND AppointmentType = ?");
            }

            PreparedStatement stmt = conn.prepareStatement(sql.toString());

              
            int paramIndex = 1;
            if (!patientName.isEmpty()) {
                stmt.setString(paramIndex++, "%" + patientName + "%");
            }
            if (!date.isEmpty()) {
                stmt.setString(paramIndex++, date);
            }
            if (type != null) {
                stmt.setString(paramIndex++, type);
            }

            ResultSet rs = stmt.executeQuery();
            resultsList.getItems().clear();

            while (rs.next()) {
                String appointment = "ID: " + rs.getInt("AppointmentID") + " | Patient ID: " + rs.getString("PatientID") + " | Type: " + rs.getString("AppointmentType") + " | Date: " + rs.getString("AppointmentDate");
                resultsList.getItems().add(appointment);
            }
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addAppointment() {
        Stage addAppointmentStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

          
        ComboBox<String> patientIdComboBox = new ComboBox<>();
        patientIdComboBox.setPromptText("Select Patient ID");

          
        ComboBox<String> doctorIdComboBox = new ComboBox<>();
        doctorIdComboBox.setPromptText("Select Doctor ID");

          
        ComboBox<String> vaccineComboBox = new ComboBox<>();
        vaccineComboBox.setPromptText("Select Vaccine");
        vaccineComboBox.setVisible(false);   

          
        try (Connection conn = DBConnection.getConnection()) {
              
            String patientQuery = "SELECT PatientID FROM Patients";
            try (PreparedStatement patientStmt = conn.prepareStatement(patientQuery);
                 ResultSet patientResultSet = patientStmt.executeQuery()) {
                while (patientResultSet.next()) {
                    patientIdComboBox.getItems().add(patientResultSet.getString("PatientID"));
                }
            }

              
            String doctorQuery = "SELECT DoctorID FROM Doctors";
            try (PreparedStatement doctorStmt = conn.prepareStatement(doctorQuery);
                 ResultSet doctorResultSet = doctorStmt.executeQuery()) {
                while (doctorResultSet.next()) {
                    doctorIdComboBox.getItems().add(doctorResultSet.getString("DoctorID"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

          
        patientIdComboBox.setOnAction(event -> {
            String selectedPatientId = patientIdComboBox.getValue();
            if (selectedPatientId != null) {
                try (Connection conn = DBConnection.getConnection()) {
                      
                    String ageQuery = "SELECT BirthDate FROM Patients WHERE PatientID = ?";
                    try (PreparedStatement ageStmt = conn.prepareStatement(ageQuery)) {
                        ageStmt.setString(1, selectedPatientId);
                        try (ResultSet ageResultSet = ageStmt.executeQuery()) {
                            if (ageResultSet.next()) {
                                LocalDate dob = ageResultSet.getDate("BirthDate").toLocalDate();
                                LocalDate currentDate = LocalDate.now();

                                  
                                int patientAgeMonths = Period.between(dob, currentDate).getYears() * 12
                                        + Period.between(dob, currentDate).getMonths();

                                  
                                System.out.println("Patient Age (in months): " + patientAgeMonths);

                                  
                                String vaccineQuery = "SELECT VaccineID, VaccineName, AgeAtVaccination FROM Vaccines WHERE AgeAtVaccination <= ?";
                                try (PreparedStatement vaccineStmt = conn.prepareStatement(vaccineQuery)) {
                                    vaccineStmt.setInt(1, patientAgeMonths);
                                    try (ResultSet vaccineResultSet = vaccineStmt.executeQuery()) {
                                        vaccineComboBox.getItems().clear();   
                                        while (vaccineResultSet.next()) {
                                            int vaccineId = vaccineResultSet.getInt("VaccineID");
                                            String vaccineName = vaccineResultSet.getString("VaccineName");
                                            int ageAtVaccination = vaccineResultSet.getInt("AgeAtVaccination");

                                              
                                            System.out.println("Vaccine ID: " + vaccineId + ", Name: " + vaccineName + ", AgeAtVaccination: " + ageAtVaccination);

                                            vaccineComboBox.getItems().add(vaccineId + " - " + vaccineName);
                                        }

                                          
                                        if (vaccineComboBox.getItems().isEmpty()) {
                                            vaccineComboBox.setPromptText("No vaccines available for this age");
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        ComboBox<String> appointmentTypeDropdown = new ComboBox<>();
        appointmentTypeDropdown.getItems().addAll("Diagnosis", "Vaccination");
        appointmentTypeDropdown.setPromptText("Select Appointment Type");

        TextField dateField = new TextField();
        dateField.setPromptText("Enter Appointment Date (YYYY-MM-DD)");

        ComboBox<String> timeDropdown = new ComboBox<>();
        timeDropdown.setPromptText("Select Time Slot");

          
        List<String> allSlots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        while (startTime.isBefore(endTime)) {
            allSlots.add(startTime.toString());
            startTime = startTime.plusMinutes(25);   
        }

          
        appointmentTypeDropdown.setOnAction(e -> {
            String selectedType = appointmentTypeDropdown.getValue();
            vaccineComboBox.setVisible("Vaccination".equals(selectedType));   
        });

        Button submitButton = new Button("Submit Appointment");

        submitButton.setOnAction(e -> {
            String patientId = patientIdComboBox.getValue();
            String doctorId = doctorIdComboBox.getValue();
            String appointmentType = appointmentTypeDropdown.getValue();
            String date = dateField.getText();
            String time = timeDropdown.getValue();
            String selectedVaccine = vaccineComboBox.getValue();

            String vaccineName = null;
            int vaccineId = -1;
            if (selectedVaccine != null && !selectedVaccine.isEmpty()) {
                String[] vaccineDetails = selectedVaccine.split(" - ");
                vaccineId = Integer.parseInt(vaccineDetails[0]);
                vaccineName = vaccineDetails[1];
            }

            if (patientId == null || doctorId == null || appointmentType == null || date.isEmpty() || time == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill in all fields.");
                alert.show();
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                  
                String checkSlotSql = "SELECT COUNT(*) FROM Appointments WHERE AppointmentDate = ? AND AppointmentTime = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSlotSql)) {
                    checkStmt.setString(1, date);
                    checkStmt.setString(2, time);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Selected time slot is already booked.");
                            alert.show();
                            return;
                        }
                    }
                }

                  
                String sql = "INSERT INTO Appointments (PatientID, DoctorID, AppointmentType, AppointmentDate, AppointmentTime) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, patientId);
                    stmt.setString(2, doctorId);
                    stmt.setString(3, appointmentType);
                    stmt.setString(4, date);
                    stmt.setString(5, time);
                    stmt.executeUpdate();

                      
                    if ("Vaccination".equals(appointmentType) && vaccineId > 0) {
                        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int appointmentId = generatedKeys.getInt(1);
                                String vaccineSql = "INSERT INTO Vaccinations (AppointmentID, VaccineID) VALUES (?, ?)";
                                try (PreparedStatement vaccineStmt = conn.prepareStatement(vaccineSql)) {
                                    vaccineStmt.setInt(1, appointmentId);
                                    vaccineStmt.setInt(2, vaccineId);

                                    vaccineStmt.executeUpdate();
                                }
                            }
                        }
                    }
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Appointment added successfully.");
                alert.show();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

          
        layout.getChildren().addAll(
                patientIdComboBox,
                doctorIdComboBox,
                appointmentTypeDropdown,
                dateField,
                timeDropdown,
                vaccineComboBox,
                submitButton
        );

          
        dateField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                try {
                      
                    LocalDate selectedDate = LocalDate.parse(newValue);
                    LocalDate today = LocalDate.now();

                    timeDropdown.getItems().clear();

                      
                    if (selectedDate.equals(today)) {
                          
                        LocalTime now = LocalTime.now();
                        for (String slot : allSlots) {
                            if (LocalTime.parse(slot).isAfter(now)) {
                                timeDropdown.getItems().add(slot);
                            }
                        }
                    } else {
                          
                        timeDropdown.getItems().addAll(allSlots);
                    }
                } catch (DateTimeParseException ex) {
                      
                    timeDropdown.getItems().clear();
                }
            }
        });

        Scene scene = new Scene(layout, 400, 500);
        addAppointmentStage.setScene(scene);
        addAppointmentStage.setTitle("Add Appointment");
        addAppointmentStage.show();
    }


    private void editAppointment1() {
        Stage editAppointmentStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

          
        ComboBox<String> patientIdComboBox = new ComboBox<>();
        patientIdComboBox.setPromptText("Select Patient ID");

          
        ComboBox<String> doctorIdComboBox = new ComboBox<>();
        doctorIdComboBox.setPromptText("Select Doctor ID");

          
        ComboBox<String> vaccineComboBox = new ComboBox<>();
        vaccineComboBox.setPromptText("Select Vaccine");
        vaccineComboBox.setVisible(false);   

          
        try (Connection conn = DBConnection.getConnection()) {
              
            String patientQuery = "SELECT PatientID FROM Patients";
            try (PreparedStatement patientStmt = conn.prepareStatement(patientQuery);
                 ResultSet patientResultSet = patientStmt.executeQuery()) {
                while (patientResultSet.next()) {
                    patientIdComboBox.getItems().add(patientResultSet.getString("PatientID"));
                }
            }

              
            String doctorQuery = "SELECT DoctorID FROM Doctors";
            try (PreparedStatement doctorStmt = conn.prepareStatement(doctorQuery);
                 ResultSet doctorResultSet = doctorStmt.executeQuery()) {
                while (doctorResultSet.next()) {
                    doctorIdComboBox.getItems().add(doctorResultSet.getString("DoctorID"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

          
        ComboBox<String> appointmentIdComboBox = new ComboBox<>();
        appointmentIdComboBox.setPromptText("Select Appointment ID");

          
        try (Connection conn = DBConnection.getConnection()) {
            String appointmentQuery = "SELECT AppointmentID FROM Appointments";
            try (PreparedStatement appointmentStmt = conn.prepareStatement(appointmentQuery);
                 ResultSet appointmentResultSet = appointmentStmt.executeQuery()) {
                while (appointmentResultSet.next()) {
                    appointmentIdComboBox.getItems().add(appointmentResultSet.getString("AppointmentID"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

          
        TextField dateField = new TextField();
        dateField.setPromptText("Enter Appointment Date (YYYY-MM-DD)");

        ComboBox<String> timeDropdown = new ComboBox<>();
        timeDropdown.setPromptText("Select Time Slot");

          
        List<String> allSlots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        while (startTime.isBefore(endTime)) {
            allSlots.add(startTime.toString());
            startTime = startTime.plusMinutes(25);   
        }

          
        appointmentIdComboBox.setOnAction(event -> {
            String selectedAppointmentId = appointmentIdComboBox.getValue();
            if (selectedAppointmentId != null) {
                try (Connection conn = DBConnection.getConnection()) {
                      
                    String appointmentDetailsQuery = "SELECT * FROM Appointments WHERE AppointmentID = ?";
                    try (PreparedStatement appointmentStmt = conn.prepareStatement(appointmentDetailsQuery)) {
                        appointmentStmt.setString(1, selectedAppointmentId);
                        try (ResultSet appointmentResultSet = appointmentStmt.executeQuery()) {
                            if (appointmentResultSet.next()) {
                                  
                                String patientId = appointmentResultSet.getString("PatientID");
                                String doctorId = appointmentResultSet.getString("DoctorID");
                                String appointmentType = appointmentResultSet.getString("AppointmentType");
                                String appointmentDate = appointmentResultSet.getString("AppointmentDate");
                                String appointmentTime = appointmentResultSet.getString("AppointmentTime");

                                patientIdComboBox.setValue(patientId);
                                doctorIdComboBox.setValue(doctorId);
                                dateField.setText(appointmentDate);
                                timeDropdown.setValue(appointmentTime);

                                  
                                if ("Vaccination".equals(appointmentType)) {
                                    vaccineComboBox.setVisible(true);    

                                      
                                    String vaccinationQuery = "SELECT VaccineID FROM Vaccinations WHERE AppointmentID = ?";
                                    try (PreparedStatement vaccinationStmt = conn.prepareStatement(vaccinationQuery)) {
                                        vaccinationStmt.setString(1, selectedAppointmentId);
                                        try (ResultSet vaccinationResultSet = vaccinationStmt.executeQuery()) {
                                            if (vaccinationResultSet.next()) {
                                                String currentVaccineId = vaccinationResultSet.getString("VaccineID");

                                                  
                                                String vaccineQuery = "SELECT VaccineName, VaccineID FROM Vaccines";
                                                try (PreparedStatement vaccineStmt = conn.prepareStatement(vaccineQuery);
                                                     ResultSet vaccineResultSet = vaccineStmt.executeQuery()) {
                                                    while (vaccineResultSet.next()) {
                                                        String vaccineName = vaccineResultSet.getString("VaccineName");
                                                        String vaccineId = vaccineResultSet.getString("VaccineID");

                                                        vaccineComboBox.getItems().add(vaccineName);

                                                          
                                                        if (vaccineId.equals(currentVaccineId)) {
                                                            vaccineComboBox.setValue(vaccineName);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    vaccineComboBox.setVisible(false);   
                                }
                            }
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button submitButton = new Button("Update Appointment");

        submitButton.setOnAction(e -> {
            String appointmentId = appointmentIdComboBox.getValue();
            String patientId = patientIdComboBox.getValue();
            String doctorId = doctorIdComboBox.getValue();
            String date = dateField.getText();
            String time = timeDropdown.getValue();
            String vaccineName = vaccineComboBox.getValue();   
            String vaccineId = null;

              
            if (vaccineName != null) {
                try (Connection conn = DBConnection.getConnection()) {
                    String vaccineQuery = "SELECT VaccineID FROM Vaccines WHERE VaccineName = ?";
                    try (PreparedStatement vaccineStmt = conn.prepareStatement(vaccineQuery)) {
                        vaccineStmt.setString(1, vaccineName);
                        try (ResultSet vaccineResultSet = vaccineStmt.executeQuery()) {
                            if (vaccineResultSet.next()) {
                                vaccineId = vaccineResultSet.getString("VaccineID");
                            }
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            if (appointmentId == null || patientId == null || doctorId == null || date.isEmpty() || time == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill in all fields.");
                alert.show();
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                  
                String updateSql = "UPDATE Appointments SET PatientID = ?, DoctorID = ?, AppointmentDate = ?, AppointmentTime = ? WHERE AppointmentID = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, patientId);
                    updateStmt.setString(2, doctorId);
                    updateStmt.setString(3, date);
                    updateStmt.setString(4, time);
                    updateStmt.setString(5, appointmentId);

                    int rowsAffected = updateStmt.executeUpdate();
                    if (rowsAffected > 0) {
                          
                        if (vaccineId != null) {
                            String updateVaccinationSql = "UPDATE Vaccinations SET VaccineID = ? WHERE AppointmentID = ?";
                            try (PreparedStatement vaccinationStmt = conn.prepareStatement(updateVaccinationSql)) {
                                vaccinationStmt.setString(1, vaccineId);
                                vaccinationStmt.setString(2, appointmentId);
                                vaccinationStmt.executeUpdate();
                            }
                        }

                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Appointment updated successfully.");
                        alert.show();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update appointment.");
                        alert.show();
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

          
        layout.getChildren().addAll(
                appointmentIdComboBox,
                patientIdComboBox,
                doctorIdComboBox,
                dateField,
                timeDropdown,
                vaccineComboBox,    
                submitButton
        );

        Scene scene = new Scene(layout, 400, 500);
        editAppointmentStage.setScene(scene);
        editAppointmentStage.setTitle("Edit Appointment");
        editAppointmentStage.show();
    }










    private void editAppointment() {
        Stage editAppointmentStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        ComboBox<String> appointmentIdDropdown = new ComboBox<>();
        appointmentIdDropdown.setPromptText("Select or Search Appointment ID");

        Button refreshButton = new Button("Refresh");

        TextField doctorIdField = new TextField();
        doctorIdField.setPromptText("Enter Doctor ID");

        ComboBox<String> appointmentTypeDropdown = new ComboBox<>();
        appointmentTypeDropdown.getItems().addAll("Diagnosis", "Vaccination");
        appointmentTypeDropdown.setPromptText("Select Appointment Type");

        TextField dateField = new TextField();
        dateField.setPromptText("Enter Appointment Date (YYYY-MM-DD)");

        ComboBox<String> timeDropdown = new ComboBox<>();
        timeDropdown.setPromptText("Select Time Slot");

        List<String> allSlots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        while (startTime.isBefore(endTime)) {
            allSlots.add(startTime.toString());
            startTime = startTime.plusMinutes(25);   
        }

        TextField vaccineNameField = new TextField();
        vaccineNameField.setPromptText("Enter Vaccine Name");
        vaccineNameField.setVisible(false);

          
        appointmentTypeDropdown.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ("Vaccination".equals(newValue)) {
                vaccineNameField.setVisible(true);
            } else {
                vaccineNameField.setVisible(false);
                vaccineNameField.clear();   
            }
        });


        Button loadButton = new Button("Load Appointment");
        Button updateButton = new Button("Update Appointment");

          
        Runnable fetchAppointmentIds = () -> {
            try {
                Connection conn = DBConnection.getConnection();
                String sql = "SELECT AppointmentID FROM Appointments";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();

                ObservableList<String> appointmentIds = FXCollections.observableArrayList();
                while (rs.next()) {
                    appointmentIds.add(rs.getString("AppointmentID"));
                }

                  
                appointmentIdDropdown.setItems(appointmentIds);
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        };

          
        fetchAppointmentIds.run();

          
        refreshButton.setOnAction(e -> fetchAppointmentIds.run());

          
        loadButton.setOnAction(e -> {
            String appointmentId = appointmentIdDropdown.getValue();
            if (appointmentId == null || appointmentId.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select an Appointment ID.");
                alert.show();
                return;
            }
            loadAppointmentDetails(appointmentId, doctorIdField, appointmentTypeDropdown, dateField, timeDropdown, vaccineNameField);
        });

          
        updateButton.setOnAction(e -> {
            String appointmentId = appointmentIdDropdown.getValue();
            String doctorId = doctorIdField.getText();
            String appointmentType = appointmentTypeDropdown.getValue();
            String date = dateField.getText();
            String time = timeDropdown.getValue();
            String vaccineName = vaccineNameField.isVisible() ? vaccineNameField.getText() : null;

            if (appointmentId == null || doctorId.isEmpty() || appointmentType == null || date.isEmpty() || time == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill in all fields.");
                alert.show();
                return;
            }

            try {
                Connection conn = DBConnection.getConnection();

                String checkSlotSql = "SELECT COUNT(*) FROM Appointments WHERE AppointmentDate = ? AND AppointmentTime = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSlotSql);
                checkStmt.setString(1, date);
                checkStmt.setString(2, time);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Selected time slot is already booked.");
                    alert.show();
                    conn.close();
                    return;
                }

                  
                String sql = "UPDATE appointments SET DoctorID = ?, AppointmentType = ?, AppointmentDate = ?, AppointmentTime = ? WHERE AppointmentID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, doctorId);
                stmt.setString(2, appointmentType);
                stmt.setString(3, date);
                stmt.setString(4, time);
                stmt.setString(5, appointmentId);
                stmt.executeUpdate();

                  
                if ("Vaccination".equals(appointmentType)) {
                    String vaccineSql = "INSERT INTO Vaccinations (AppointmentID, VaccineName) VALUES (?, ?)";
                    PreparedStatement vaccineStmt = conn.prepareStatement(vaccineSql);
                    vaccineStmt.setString(1, appointmentId);
                    vaccineStmt.setString(2, vaccineName);
                    vaccineStmt.executeUpdate();
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Appointment updated successfully.");
                alert.show();
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

          
        layout.getChildren().addAll(
                appointmentIdDropdown,
                refreshButton,
                doctorIdField,
                appointmentTypeDropdown,
                dateField,
                timeDropdown,
                vaccineNameField,
                loadButton,
                updateButton
        );

        Scene scene = new Scene(layout, 400, 500);
        editAppointmentStage.setScene(scene);
        editAppointmentStage.setTitle("Edit Appointment");
        editAppointmentStage.show();
    }

    private void loadAppointmentDetails(String appointmentId, TextField doctorIdField, ComboBox<String> appointmentTypeDropdown,
                                        TextField dateField, ComboBox<String> timeDropdown, TextField vaccineNameField) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT * FROM Appointments WHERE AppointmentID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, appointmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String doctorId = rs.getString("DoctorID");
                String appointmentType = rs.getString("AppointmentType");
                String appointmentDate = rs.getString("AppointmentDate");
                String appointmentTime = rs.getString("AppointmentTime");

                doctorIdField.setText(doctorId);
                appointmentTypeDropdown.setValue(appointmentType);
                dateField.setText(appointmentDate);
                timeDropdown.setValue(appointmentTime);    

                  
                populateTimeSlots(timeDropdown);

                if ("Vaccination".equals(appointmentType)) {
                    String vaccineSql = "SELECT VaccineName FROM Vaccinations WHERE AppointmentID = ?";
                    PreparedStatement vaccineStmt = conn.prepareStatement(vaccineSql);
                    vaccineStmt.setString(1, appointmentId);
                    ResultSet vaccineRs = vaccineStmt.executeQuery();
                    if (vaccineRs.next()) {
                        vaccineNameField.setText(vaccineRs.getString("VaccineName"));
                        vaccineNameField.setVisible(true);
                    }
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Appointment not found.");
                alert.show();
            }
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }



    private void populateTimeSlots(ComboBox<String> timeComboBox) {

        timeComboBox.getItems().clear();

        List<String> availableTimes = new ArrayList<>();
        LocalTime startTime = LocalTime.of(9, 0);   
        LocalTime endTime = LocalTime.of(17, 0);   

          
        while (startTime.isBefore(endTime)) {
            availableTimes.add(startTime.toString());
            startTime = startTime.plusMinutes(25);    
        }

        timeComboBox.getItems().setAll(availableTimes);    
    }



    private void deleteAppointment() {
        Stage deleteAppointmentStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

          
        ComboBox<String> appointmentIdComboBox = new ComboBox<>();
        appointmentIdComboBox.setPromptText("Select Appointment ID");

          
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            loadAppointmentIds(appointmentIdComboBox);
        });

          
        loadAppointmentIds(appointmentIdComboBox);

          
        Button deleteButton = new Button("Delete Appointment");
        deleteButton.setOnAction(e -> {
            String appointmentId = appointmentIdComboBox.getValue();
            if (appointmentId == null || appointmentId.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select an appointment ID.");
                alert.show();
                return;
            }

            try {
                  
                Connection conn = DBConnection.getConnection();
                String selectSql = "SELECT AppointmentType FROM appointments WHERE AppointmentID = ?";
                PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                selectStmt.setString(1, appointmentId);
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    String appointmentType = rs.getString("AppointmentType");

                      
                    if ("Vaccination".equals(appointmentType) || "Checkup".equals(appointmentType)) {
                          
                        String deleteSql = "DELETE FROM appointments WHERE AppointmentID = ?";
                        PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                        deleteStmt.setString(1, appointmentId);

                        int rows = deleteStmt.executeUpdate();
                        if (rows > 0) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Appointment deleted successfully.");
                            alert.show();
                            loadAppointmentIds(appointmentIdComboBox);   
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Appointment not found.");
                            alert.show();
                        }
                    } else {
                          
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Appointment type is neither 'Vaccination' nor 'Checkup'.");
                        alert.show();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Appointment not found.");
                    alert.show();
                }
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(appointmentIdComboBox, refreshButton, deleteButton);
        Scene scene = new Scene(layout, 400, 450);
        deleteAppointmentStage.setScene(scene);
        deleteAppointmentStage.setTitle("Delete Appointment");
        deleteAppointmentStage.show();
    }

    private void loadAppointmentIds(ComboBox<String> appointmentIdComboBox) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT AppointmentID FROM appointments";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            appointmentIdComboBox.getItems().clear();   
            while (rs.next()) {
                appointmentIdComboBox.getItems().add(rs.getString("AppointmentID"));
            }
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load appointment IDs.");
            alert.show();
        }
    }


    private void checkInPatient() {
        Stage checkAppointmentStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

          
        ComboBox<String> appointmentIdComboBox = new ComboBox<>();
        appointmentIdComboBox.setPromptText("Select Appointment ID");

          
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            loadAppointmentIds(appointmentIdComboBox);
        });

          
        loadAppointmentIds(appointmentIdComboBox);

          
        Button checkInButton = new Button("Check-In Patient");

          
        Button undoButton = new Button("Undo Last Check-In");
        undoButton.setDisable(true);   

          
        final String[] lastCheckedInAppointment = {null};

        checkInButton.setOnAction(e -> {
            String appointmentId = appointmentIdComboBox.getValue();
            if (appointmentId == null || appointmentId.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select an appointment ID.");
                alert.show();
                return;
            }

            try {
                Connection conn = DBConnection.getConnection();
                String sql = "UPDATE appointments SET CheckedIn = 1 WHERE AppointmentID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, appointmentId);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Patient checked in successfully.");
                    alert.show();
                    lastCheckedInAppointment[0] = appointmentId;   
                    undoButton.setDisable(false);   
                    loadAppointmentIds(appointmentIdComboBox);   
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Appointment not found.");
                    alert.show();
                }
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

          
        undoButton.setOnAction(e -> {
            if (lastCheckedInAppointment[0] == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "No recent check-in to undo.");
                alert.show();
                return;
            }

            try {
                Connection conn = DBConnection.getConnection();
                String sql = "UPDATE appointments SET CheckedIn = 0 WHERE AppointmentID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, lastCheckedInAppointment[0]);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Undo successful. Patient check-in reverted.");
                    alert.show();
                    lastCheckedInAppointment[0] = null;   
                    undoButton.setDisable(true);   
                    loadAppointmentIds(appointmentIdComboBox);   
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Undo operation failed.");
                    alert.show();
                }
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(appointmentIdComboBox, refreshButton, checkInButton, undoButton);
        Scene scene = new Scene(layout, 400, 450);
        checkAppointmentStage.setScene(scene);
        checkAppointmentStage.setTitle("Check-In Patient");
        checkAppointmentStage.show();
    }

    private void addVisit() {
        Stage addVisitStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

          
        ComboBox<String> patientIdComboBox = new ComboBox<>();
        patientIdComboBox.setPromptText("Select Patient ID");

        ComboBox<String> doctorIdComboBox = new ComboBox<>();
        doctorIdComboBox.setPromptText("Select Doctor ID");

          
        refreshComboBoxData(patientIdComboBox, doctorIdComboBox);

          
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            refreshComboBoxData(patientIdComboBox, doctorIdComboBox);
        });

          
        Button submitButton = new Button("Submit Visit");

          
        Button undoButton = new Button("Undo Last Visit");
        undoButton.setDisable(true);   

          
        final int[] lastAddedVisitId = {0};

        submitButton.setOnAction(e -> {
            String patientId = patientIdComboBox.getValue();
            String doctorId = doctorIdComboBox.getValue();

            if (patientId == null || doctorId == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select both a patient and a doctor.");
                alert.show();
                return;
            }

            try {
                Connection conn = DBConnection.getConnection();

                String sql = "INSERT INTO Visits (PatientID, DoctorID, VisitDate, VisitTime) VALUES (?, ?, CURDATE(), CURTIME())";
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, patientId);
                stmt.setString(2, doctorId);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                      
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        lastAddedVisitId[0] = rs.getInt(1);   
                    }

                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Visit added successfully.");
                    alert.show();
                    undoButton.setDisable(false);   
                }
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

          
        undoButton.setOnAction(e -> {
            if (lastAddedVisitId[0] == 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "No recent visit to undo.");
                alert.show();
                return;
            }

            try {
                Connection conn = DBConnection.getConnection();
                String sql = "DELETE FROM Visits WHERE VisitID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, lastAddedVisitId[0]);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Undo successful. Visit removed.");
                    alert.show();
                    lastAddedVisitId[0] = 0;   
                    undoButton.setDisable(true);   
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Undo operation failed.");
                    alert.show();
                }
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

          
        layout.getChildren().addAll(
                patientIdComboBox,
                doctorIdComboBox,
                refreshButton,
                submitButton,
                undoButton
        );

        Scene scene = new Scene(layout, 400, 500);
        addVisitStage.setScene(scene);
        addVisitStage.setTitle("Add Visit");
        addVisitStage.show();
    }


    private void refreshComboBoxData(ComboBox<String> patientIdComboBox, ComboBox<String> doctorIdComboBox) {
          
        patientIdComboBox.getItems().clear();
        doctorIdComboBox.getItems().clear();

        try {
            Connection conn = DBConnection.getConnection();

              
            String patientSql = "SELECT PatientID FROM patients";
            PreparedStatement patientStmt = conn.prepareStatement(patientSql);
            ResultSet patientRs = patientStmt.executeQuery();

            while (patientRs.next()) {
                patientIdComboBox.getItems().add(patientRs.getString("PatientID"));
            }

              
            String doctorSql = "SELECT DoctorID FROM doctors";
            PreparedStatement doctorStmt = conn.prepareStatement(doctorSql);
            ResultSet doctorRs = doctorStmt.executeQuery();

            while (doctorRs.next()) {
                doctorIdComboBox.getItems().add(doctorRs.getString("DoctorID"));
            }

            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }





    private void showFinancialRecords() {
        Stage financialsStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label header = new Label("Financial Records:");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<String> recordsList = new ListView<>();
        ComboBox<String> appointmentIdComboBox = new ComboBox<>();
        appointmentIdComboBox.setPromptText("Select Appointment ID");
        Button refreshComboBoxButton = new Button("Refresh IDs");
        refreshComboBoxButton.setOnAction(e -> loadPendingAppointments(appointmentIdComboBox));

        TextField amountField = new TextField();
        amountField.setPromptText("Enter Payment Amount");

        Button addPaymentButton = new Button("Add Payment");
        addPaymentButton.setOnAction(e -> {
            String selectedAppointmentId = appointmentIdComboBox.getValue();
            String amount = amountField.getText();

            if (selectedAppointmentId == null || amount.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select an Appointment ID and enter a Payment Amount.");
                alert.show();
                return;
            }

            try {
                addPaymentToRecord(selectedAppointmentId, amount, recordsList);
                loadPendingAppointments(appointmentIdComboBox);
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Payment Amount must be a valid number.");
                alert.show();
            }
        });

          
        Button editPaymentButton = new Button("Edit Payment");
        editPaymentButton.setOnAction(e -> {
            String selectedRecord = recordsList.getSelectionModel().getSelectedItem();
            if (selectedRecord != null) {
                String[] parts = selectedRecord.split(", ");
                int recordId = Integer.parseInt(parts[0].split(": ")[1]);   

                String newAmount = amountField.getText();
                if (newAmount.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a new Payment Amount.");
                    alert.show();
                    return;
                }
                try {
                    updatePaymentAmount(recordId, newAmount);
                    refreshFinancialRecords(recordsList);
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Payment Amount.");
                    alert.show();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a payment record to edit.");
                alert.show();
            }
        });

          
        Button deletePaymentButton = new Button("Delete Payment");
        deletePaymentButton.setOnAction(e -> {
            String selectedRecord = recordsList.getSelectionModel().getSelectedItem();
            if (selectedRecord != null) {
                String[] parts = selectedRecord.split(", ");
                int recordId = Integer.parseInt(parts[0].split(": ")[1]);   

                try {
                    deletePayment(recordId, recordsList);
                    loadPendingAppointments(appointmentIdComboBox);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error deleting payment.");
                    alert.show();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a payment record to delete.");
                alert.show();
            }
        });

          
        refreshFinancialRecords(recordsList);
        loadPendingAppointments(appointmentIdComboBox);

        layout.getChildren().addAll(
                header,
                recordsList,
                appointmentIdComboBox,
                refreshComboBoxButton,
                amountField,
                addPaymentButton,
                editPaymentButton,   
                deletePaymentButton   
        );

        Scene scene = new Scene(layout, 500, 400);
        financialsStage.setScene(scene);
        financialsStage.setTitle("Financial Records");
        financialsStage.show();
    }

      
    private void updatePaymentAmount(int recordId, String newAmount) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

              
            String updatePaymentSQL = "UPDATE FinancialRecords SET Amount = ? WHERE RecordID = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updatePaymentSQL);
            updateStmt.setDouble(1, Double.parseDouble(newAmount));
            updateStmt.setInt(2, recordId);

            int rowsUpdated = updateStmt.executeUpdate();
            if (rowsUpdated > 0) {
                conn.commit();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Payment amount updated successfully!");
                alert.show();
            } else {
                conn.rollback();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update payment amount.");
                alert.show();
            }
        }
    }

      
    private void deletePayment(int recordId, ListView<String> recordsList) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

              
            String selectAppointmentSQL = "SELECT AppointmentID FROM FinancialRecords WHERE RecordID = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectAppointmentSQL);
            selectStmt.setInt(1, recordId);
            ResultSet rs = selectStmt.executeQuery();
            int appointmentId = 0;
            if (rs.next()) {
                appointmentId = rs.getInt("AppointmentID");
            }

              
            String deletePaymentSQL = "DELETE FROM FinancialRecords WHERE RecordID = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deletePaymentSQL);
            deleteStmt.setInt(1, recordId);

            int rowsDeleted = deleteStmt.executeUpdate();
            if (rowsDeleted > 0) {
                  
                String updateStatusSQL = "UPDATE Appointments SET PaymentStatus = FALSE WHERE AppointmentID = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateStatusSQL);
                updateStmt.setInt(1, appointmentId);
                updateStmt.executeUpdate();

                conn.commit();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Payment deleted and status reverted successfully.");
                alert.show();
                refreshFinancialRecords(recordsList);
            } else {
                conn.rollback();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to delete payment.");
                alert.show();
            }
        }
    }
    private void refreshFinancialRecords(ListView<String> recordsList) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = """
        SELECT fr.RecordID, fr.AppointmentID, fr.Amount, fr.PaymentDate, p.name AS patient_name
        FROM FinancialRecords fr
        JOIN Appointments a ON fr.AppointmentID = a.AppointmentID
        JOIN Patients p ON a.PatientID = p.PatientID
        """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> records = FXCollections.observableArrayList();
            while (rs.next()) {
                String record = "Record ID: " + rs.getInt("RecordID") +
                        ", Appointment ID: " + rs.getInt("AppointmentID") +
                        ", Patient: " + rs.getString("patient_name") +
                        ", Amount: $" + rs.getDouble("Amount") +
                        ", Date: " + rs.getTimestamp("PaymentDate");
                records.add(record);
            }
            recordsList.setItems(records);
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



      
    private void loadPendingAppointments(ComboBox<String> appointmentIdComboBox) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = """
            SELECT AppointmentID 
            FROM Appointments 
            WHERE PaymentStatus IS NULL OR PaymentStatus = FALSE
        """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> pendingAppointments = FXCollections.observableArrayList();
            while (rs.next()) {
                pendingAppointments.add(String.valueOf(rs.getInt("AppointmentID")));
            }
            appointmentIdComboBox.setItems(pendingAppointments);
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

      
    private void addPaymentToRecord(String appointmentId, String amount, ListView<String> recordsList) {
        try {
            int appointmentIdInt = Integer.parseInt(appointmentId);
            double amountDouble = Double.parseDouble(amount);

            Connection conn = DBConnection.getConnection();
            try {
                conn.setAutoCommit(false);

                  
                String insertPaymentSQL = "INSERT INTO FinancialRecords (AppointmentID, Amount) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertPaymentSQL);
                insertStmt.setInt(1, appointmentIdInt);
                insertStmt.setDouble(2, amountDouble);

                int rowsInserted = insertStmt.executeUpdate();
                if (rowsInserted > 0) {
                      
                    String updatePaymentStatusSQL = "UPDATE Appointments SET PaymentStatus = TRUE WHERE AppointmentID = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updatePaymentStatusSQL);
                    updateStmt.setInt(1, appointmentIdInt);

                    int rowsUpdated = updateStmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        conn.commit();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Payment added and status updated successfully!");
                        alert.show();
                        refreshFinancialRecords(recordsList);
                    } else {
                        conn.rollback();
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update payment status.");
                        alert.show();
                    }
                } else {
                    conn.rollback();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to add payment.");
                    alert.show();
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "An error occurred while processing the payment.");
                alert.show();
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid input. Please enter valid numbers for Appointment ID and Amount.");
            alert.show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    private void showPatientsList() {
        Stage patientsStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label header = new Label("Patients List:");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<String> patientsList = new ListView<>();

          
        TextField nameField = new TextField();
        nameField.setPromptText("Search by Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Search by Email");

        DatePicker birthDatePicker = new DatePicker();
        birthDatePicker.setPromptText("Search by Birthdate");

        TextField minAgeField = new TextField();
        minAgeField.setPromptText("Min Age");

        TextField maxAgeField = new TextField();
        maxAgeField.setPromptText("Max Age");

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> {
            patientsList.getItems().clear();   
            searchPatients(nameField.getText(), emailField.getText(), birthDatePicker.getValue(),
                    minAgeField.getText(), maxAgeField.getText(), patientsList);
        });

          
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            patientsList.getItems().clear();   
            searchPatients("", "", null, "", "", patientsList);   
        });

          
        Button editButton = new Button("Edit Selected Patient");
        editButton.setOnAction(e -> {
            String selectedPatient = patientsList.getSelectionModel().getSelectedItem();
            if (selectedPatient != null) {
                  
                int patientId = Integer.parseInt(selectedPatient.split(",")[0].split(":")[1].trim());
                openEditPatientForm(patientId);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a patient to edit.");
                alert.show();
            }
        });

        layout.getChildren().addAll(header, nameField, emailField, birthDatePicker, minAgeField, maxAgeField,
                searchButton, refreshButton, patientsList, editButton);

        Scene scene = new Scene(layout, 600, 500);
        patientsStage.setScene(scene);
        patientsStage.setTitle("Patients List");
        patientsStage.show();
    }

      
    private void openEditPatientForm(int patientId) {
        Stage editStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label header = new Label("Edit Patient");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Patient Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Patient Email");

        TextField addressField = new TextField();
        addressField.setPromptText("Patient Address");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Patient Phone");

        DatePicker birthDatePicker = new DatePicker();
        birthDatePicker.setPromptText("Patient Birthdate");

          
        loadPatientDetails(patientId, nameField, emailField, addressField, phoneField, birthDatePicker);

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {
            updatePatient(patientId, nameField.getText(), emailField.getText(),
                    addressField.getText(), phoneField.getText(), birthDatePicker.getValue());
            editStage.close();   
        });

        layout.getChildren().addAll(header, nameField, emailField, addressField, phoneField, birthDatePicker, saveButton);

        Scene scene = new Scene(layout, 400, 400);
        editStage.setScene(scene);
        editStage.setTitle("Edit Patient");
        editStage.show();
    }

      
    private void loadPatientDetails(int patientId, TextField nameField, TextField emailField,
                                    TextField addressField, TextField phoneField, DatePicker birthDatePicker) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT Name, Email, Address, PhoneNumber, BirthDate FROM patients WHERE PatientID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("Name"));
                emailField.setText(rs.getString("Email"));
                addressField.setText(rs.getString("Address"));
                phoneField.setText(rs.getString("PhoneNumber"));
                birthDatePicker.setValue(rs.getDate("BirthDate").toLocalDate());
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

      
    private void updatePatient(int patientId, String name, String email, String address, String phone, LocalDate birthDate) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "UPDATE patients SET Name = ?, Email = ?, Address = ?, PhoneNumber = ?, BirthDate = ? WHERE PatientID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, address);
            stmt.setString(4, phone);
            stmt.setDate(5, java.sql.Date.valueOf(birthDate));
            stmt.setInt(6, patientId);
            stmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchPatients(String name, String email, LocalDate birthDate,
                                String minAge, String maxAge, ListView<String> patientsList) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT PatientID, Name, BirthDate, Address, PhoneNumber, Email, " +
                    "TIMESTAMPDIFF(YEAR, BirthDate, CURDATE()) AS Age FROM patients WHERE 1=1";

              
            if (!name.isEmpty()) sql += " AND Name LIKE ?";
            if (!email.isEmpty()) sql += " AND Email LIKE ?";
            if (birthDate != null) sql += " AND BirthDate = ?";
            if (!minAge.isEmpty()) sql += " AND TIMESTAMPDIFF(YEAR, BirthDate, CURDATE()) >= ?";
            if (!maxAge.isEmpty()) sql += " AND TIMESTAMPDIFF(YEAR, BirthDate, CURDATE()) <= ?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            int paramIndex = 1;
            if (!name.isEmpty()) stmt.setString(paramIndex++, "%" + name + "%");
            if (!email.isEmpty()) stmt.setString(paramIndex++, "%" + email + "%");
            if (birthDate != null) stmt.setDate(paramIndex++, java.sql.Date.valueOf(birthDate));
            if (!minAge.isEmpty()) stmt.setInt(paramIndex++, Integer.parseInt(minAge));
            if (!maxAge.isEmpty()) stmt.setInt(paramIndex++, Integer.parseInt(maxAge));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String patientInfo = "ID: " + rs.getInt("PatientID") +
                        ", Name: " + rs.getString("Name") +
                        ", BirthDate: " + rs.getDate("BirthDate") +
                        ", Age: " + rs.getInt("Age") +
                        ", Address: " + rs.getString("Address") +
                        ", Phone: " + rs.getString("PhoneNumber") +
                        ", Email: " + rs.getString("Email");
                patientsList.getItems().add(patientInfo);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    public void showAttachFileWindow() {
        Stage attachStage = new Stage();
        ComboBox<String> patientComboBox = new ComboBox<>();
        loadPatientsIDs(patientComboBox);

        Button attachButton = new Button("Attach File");
        attachButton.setOnAction(e -> attachFileToPatient(attachStage, patientComboBox));

        VBox layout = new VBox(10, patientComboBox, attachButton);
        Scene scene = new Scene(layout, 300, 200);

        attachStage.setTitle("Attach File to Patient");
        attachStage.setScene(scene);
        attachStage.show();
    }

    public static void attachFileToPatient(Stage stage, ComboBox<String> patientComboBox) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Attach");
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {

            String selectedPatientId = patientComboBox.getValue();
            if (selectedPatientId == null || selectedPatientId.isEmpty()) {
                System.out.println("Please select a patient ID before attaching a file.");
                return;
            }


            LocalDate currentDate = LocalDate.now();
            String uploadDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));


            try (Connection connection = DBConnection.getConnection()) {
                String insertQuery = "INSERT INTO attachments (PatientID, FilePath, UploadDate) VALUES (?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setString(1, selectedPatientId);
                    preparedStatement.setString(2, selectedFile.getAbsolutePath());
                    preparedStatement.setString(3, uploadDate);

                    int rowsInserted = preparedStatement.executeUpdate();
                    if (rowsInserted > 0) {
                        System.out.println("File attached successfully to patient ID: " + selectedPatientId);
                    } else {
                        System.out.println("Failed to attach file.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Database error: " + e.getMessage());
            }
        } else {
            System.out.println("No file selected.");
        }
    }

    public void showAttachedFilesWindow() {
        Stage viewStage = new Stage();
        ComboBox<String> patientComboBox = new ComboBox<>();
        loadPatientsIDs(patientComboBox); // Assuming this method loads patient IDs

        Button viewFilesButton = new Button("View Attached Files");
        ListView<String> filesListView = new ListView<>();

        viewFilesButton.setOnAction(e -> {
            String selectedPatientId = patientComboBox.getValue();
            if (selectedPatientId != null && !selectedPatientId.isEmpty()) {
                loadPatientFiles(selectedPatientId, filesListView);
            } else {
                System.out.println("Please select a patient ID to view files.");
            }
        });

        VBox layout = new VBox(10, patientComboBox, viewFilesButton, filesListView);
        Scene scene = new Scene(layout, 400, 300);

        viewStage.setTitle("View Attached Files");
        viewStage.setScene(scene);
        viewStage.show();
    }

    private void loadPatientFiles(String patientId, ListView<String> filesListView) {
        filesListView.getItems().clear(); // Clear the list first

        try (Connection connection = DBConnection.getConnection()) {
            String selectQuery = "SELECT FilePath, UploadDate FROM attachments WHERE PatientID = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, patientId);

                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String filePath = resultSet.getString("FilePath");
                    String uploadDate = resultSet.getString("UploadDate");
                    filesListView.getItems().add("File: " + filePath + " | Date: " + uploadDate);
                }

                if (filesListView.getItems().isEmpty()) {
                    filesListView.getItems().add("No files attached for this patient.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading files: " + e.getMessage());
        }
    }

    private void loadPatientsIDs(ComboBox<String> patientComboBox) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT DISTINCT p.PatientID FROM Patients p " ;
            PreparedStatement stmt = conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            patientComboBox.getItems().clear();
            while (rs.next()) {

                String patientDisplay = String.valueOf(rs.getInt("PatientID"));
                patientComboBox.getItems().add(patientDisplay);
            }

            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public Parent getView() {
        return view;
    }
}
