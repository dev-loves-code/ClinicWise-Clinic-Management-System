package clinicoop2;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.sql.*;


import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DoctorDashboard {
    private VBox view;
    private int doctorId;



       
    public DoctorDashboard(Stage stage, int doctorId) {
        this.doctorId = doctorId;

        view = new VBox(15);
        view.setPadding(new Insets(20));

           
        Label titleLabel = new Label("Doctor Dashboard");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button viewAppointmentsButton = new Button("View Appointments");
        Button patientHistoryButton = new Button("View Patient History");
        Button addDiagnosisButton = new Button("Manage Diagnosis");
        Button addVaccineButton = new Button("Manage Vaccination notes");
        Button managevisits = new Button("Manage Visits");
        Button viewVac = new Button("View Untaken Vaccinations");

        viewAppointmentsButton.setOnAction(e -> showViewAppointments());
        patientHistoryButton.setOnAction(e -> showPatientHistory());
        addDiagnosisButton.setOnAction(e -> showDiagnosisManagement());
        addVaccineButton.setOnAction(e->manageVaccinationNotes());
        managevisits.setOnAction(e->showUpdateVisit());
        viewVac.setOnAction(e->showUntakenVaccinationStage());



        view.getChildren().addAll(titleLabel, viewAppointmentsButton, patientHistoryButton, addDiagnosisButton, addVaccineButton, managevisits, viewVac);

           
        Scene scene = new Scene(view, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Doctor Dashboard");
        stage.show();
    }

    public VBox getView() {
        return view;
    }

       
    private void showViewAppointments() {
        Stage appointmentsStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label header = new Label("Appointments for Today:");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<String> appointmentsList = new ListView<>();

        try {
               
            Connection conn = DBConnection.getConnection();

               
            String sqlAppointments = "SELECT a.AppointmentID, p.Name, a.AppointmentDate, a.AppointmentType FROM appointments a " +
                    "JOIN patients p ON a.PatientID = p.PatientID WHERE a.DoctorID = ? AND a.AppointmentDate = CURDATE() ";
            PreparedStatement stmtAppointments = conn.prepareStatement(sqlAppointments);
            stmtAppointments.setInt(1, doctorId);     
            ResultSet rsAppointments = stmtAppointments.executeQuery();

               
            while (rsAppointments.next()) {
                String details = "Appointment ID: " + rsAppointments.getInt("AppointmentID") +
                        ", Patient: " + rsAppointments.getString("Name") +
                        ", Date: " + rsAppointments.getDate("AppointmentDate") +
                        ", Type: " + rsAppointments.getString("AppointmentType");
                appointmentsList.getItems().add(details);
            }

               
            String sqlVisits = "SELECT v.VisitID, p.Name, v.VisitDate FROM visits v " +
                    "JOIN patients p ON v.PatientID = p.PatientID WHERE v.DoctorID = ? AND v.VisitDate = CURDATE()";
            PreparedStatement stmtVisits = conn.prepareStatement(sqlVisits);
            stmtVisits.setInt(1, doctorId);     
            ResultSet rsVisits = stmtVisits.executeQuery();

               
            while (rsVisits.next()) {
                String details = "Visit ID: " + rsVisits.getInt("VisitID") +
                        ", Patient: " + rsVisits.getString("Name") +
                        ", Date: " + rsVisits.getDate("VisitDate") +
                        ", Type: Visit";     
                appointmentsList.getItems().add(details);
            }

               
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        layout.getChildren().addAll(header, appointmentsList);
        Scene scene = new Scene(layout, 400, 300);
        appointmentsStage.setScene(scene);
        appointmentsStage.setTitle("Appointments and Visits for Today");
        appointmentsStage.show();
    }




    private void showPatientHistory() {
        Stage historyStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label header = new Label("Patient History:");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ComboBox<String> patientComboBox = new ComboBox<>();
        patientComboBox.setPromptText("Select Patient");

        TextField patientIdField = new TextField();
        patientIdField.setPromptText("Enter Patient ID");

        ListView<String> historyList = new ListView<>();
        Button searchButton = new Button("Search");
        Button generatePdfButton = new Button("Generate PDF");
        generatePdfButton.setDisable(true);    

           
        loadPatients(patientComboBox);    

        searchButton.setOnAction(e -> {
            String selectedPatient = patientComboBox.getValue();
            String patientIdText = patientIdField.getText().trim();

               
            if (!patientIdText.isEmpty()) {
                try {
                    int patientId = Integer.parseInt(patientIdText);
                    loadPatientHistory(patientId, historyList);
                    generatePdfButton.setDisable(false);    
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a valid Patient ID.");
                    alert.show();
                }
            } else if (selectedPatient != null && !selectedPatient.isEmpty()) {
                   
                String[] parts = selectedPatient.split(" \\(");
                int patientId = Integer.parseInt(parts[0].trim());    
                loadPatientHistory(patientId, historyList);
                generatePdfButton.setDisable(false);    
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a patient or enter a valid Patient ID.");
                alert.show();
            }
        });

        generatePdfButton.setOnAction(e -> {
            String patientIdText = patientIdField.getText().trim();
            if (!patientIdText.isEmpty()) {
                int patientId = Integer.parseInt(patientIdText);
                generatePatientHistoryPDF(patientId, historyList);    
            } else if (patientComboBox.getValue() != null) {
                String[] parts = patientComboBox.getValue().split(" \\(");
                int patientId = Integer.parseInt(parts[0].trim());
                generatePatientHistoryPDF(patientId, historyList);    
            }
        });

        layout.getChildren().addAll(header, patientComboBox, patientIdField, searchButton, historyList, generatePdfButton);
        Scene scene = new Scene(layout, 500, 400);
        historyStage.setScene(scene);
        historyStage.setTitle("Patient History");
        historyStage.show();
    }


    private void generatePatientHistoryPDF(int patientId, ListView<String> historyList) {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);

        try {
            String patientName = getPatientNameById(patientId);
            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);

               
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Patient History for " + patientName + " (ID: " + patientId + ")");
            contentStream.endText();

               
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            float yPosition = 700;
            float pageWidth = PDRectangle.LETTER.getWidth();
            float leftMargin = 50;
            float rightMargin = pageWidth - 50;

               
            for (String history : historyList.getItems()) {
                   
                List<String> wrappedLines = wrapText(history, PDType1Font.HELVETICA, 10, rightMargin - leftMargin);

                for (String line : wrappedLines) {
                       
                    if (yPosition <= 50) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.LETTER);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
                        yPosition = 750;
                    }

                       
                    contentStream.beginText();
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText(line.trim());
                    contentStream.endText();

                       
                    yPosition -= 15;
                }

                   
                yPosition -= 10;
            }

            contentStream.close();
            document.save(new File("patient_history_" + patientId + ".pdf"));
            document.close();

            System.out.println("PDF Created Successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

       
    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            float width = font.getStringWidth(currentLine + " " + word) / 1000 * fontSize;

            if (width <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }



    public String getPatientNameById(int patientId) {
        String patientName = null;
        String query = "SELECT name FROM patients WHERE PatientId = ?";     

        try (PreparedStatement statement = DBConnection.getConnection().prepareStatement(query)) {
            statement.setInt(1, patientId);     
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                patientName = resultSet.getString("name");     
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return patientName;
    }





    private void loadPatients(ComboBox<String> patientComboBox) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT DISTINCT p.PatientID, p.Name FROM Patients p " +
                    "JOIN Appointments a ON p.PatientID = a.PatientID " +
                    "WHERE a.DoctorID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, doctorId);     
            ResultSet rs = stmt.executeQuery();

            patientComboBox.getItems().clear();     
            while (rs.next()) {
                   
                String patientDisplay = rs.getInt("PatientID") + " (" + rs.getString("Name") + ")";
                patientComboBox.getItems().add(patientDisplay);
            }

            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }





    private void loadPatientHistory(int patientId, ListView<String> historyList) {
        try {
            Connection conn = DBConnection.getConnection();

               
            String appointmentSql = "SELECT \n" +
                    "    a.AppointmentDate, \n" +
                    "    a.AppointmentType, \n" +
                    "    a.AppointmentTime, \n" +
                    "    v.VaccineName, \n" +
                    "    vac.Notes AS VaccinationNotes, \n" +
                    "    c.Symptoms, \n" +
                    "    c.Diagnosis, \n" +
                    "    c.Prescription \n" +
                    "FROM \n" +
                    "    patients p\n" +
                    "JOIN \n" +
                    "    appointments a ON p.PatientID = a.PatientID\n" +
                    "LEFT JOIN \n" +
                    "    vaccinations vac ON a.AppointmentID = vac.AppointmentID  -- LEFT JOIN Vaccinations\n" +
                    "LEFT JOIN \n" +
                    "    vaccines v ON vac.VaccineID = v.VaccineID  -- Join Vaccines to get VaccineName\n" +
                    "LEFT JOIN \n" +
                    "    checkups c ON a.AppointmentID = c.AppointmentID  -- LEFT JOIN Checkups\n" +
                    "WHERE \n" +
                    "    p.PatientID = ? \n" +
                    "    AND a.DoctorID = ?\n";

            PreparedStatement stmt = conn.prepareStatement(appointmentSql);
            stmt.setInt(1, patientId);           
            stmt.setInt(2, doctorId);            
            ResultSet rs = stmt.executeQuery();

               
            historyList.getItems().clear();

               
            while (rs.next()) {
                String appointmentDetails = "Appointment Date: " + rs.getTimestamp("AppointmentDate") +
                        ", Type: " + rs.getString("AppointmentType") +
                        ", Time: " + rs.getString("AppointmentTime");

                   
                if (rs.getString("VaccineName") != null) {
                    appointmentDetails += "\nVaccination: " + rs.getString("VaccineName") +
                            "\nNotes: " + rs.getString("VaccinationNotes");
                }

                   
                if (rs.getString("Diagnosis") != null) {
                    appointmentDetails += "\nDiagnosis: " + rs.getString("Diagnosis") +
                            "\nSymptoms: " + rs.getString("Symptoms") +
                            "\nPrescription: " + rs.getString("Prescription");
                }

                   
                historyList.getItems().add(appointmentDetails);
            }

               
            String visitSql = "SELECT v.VisitDate, v.Symptoms, v.Diagnosis, v.Prescription, v.VisitTime " +
                    "FROM visits v " +
                    "JOIN patients p ON v.PatientID = p.PatientID " +
                    "WHERE p.PatientID = ? AND v.DoctorID = ?";

            PreparedStatement visitStmt = conn.prepareStatement(visitSql);
            visitStmt.setInt(1, patientId);     
            visitStmt.setInt(2, doctorId);      
            ResultSet visitRs = visitStmt.executeQuery();

               
            while (visitRs.next()) {
                String visitDetails = "Visit Date: " + visitRs.getTimestamp("VisitDate") +
                        ", Type: Visit" + ", Time:" + visitRs.getString("VisitTime") ;

                   
                if (visitRs.getString("Symptoms") != null || visitRs.getString("Diagnosis") != null || visitRs.getString("Prescription") != null) {
                    visitDetails += "\nSymptoms: " + (visitRs.getString("Symptoms") != null ? visitRs.getString("Symptoms") : "N/A") +
                            "\nDiagnosis: " + (visitRs.getString("Diagnosis") != null ? visitRs.getString("Diagnosis") : "N/A") +
                            "\nPrescription: " + (visitRs.getString("Prescription") != null ? visitRs.getString("Prescription") : "N/A");
                }

                   
                historyList.getItems().add(visitDetails);
            }

               
            conn.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }





       
    private void showDiagnosisManagement() {
        Stage diagnosisStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label header = new Label("Diagnosis Management:");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

           
        ComboBox<String> appointmentIdComboBox = new ComboBox<>();
        appointmentIdComboBox.setPromptText("Select Appointment");

           
        TextArea symptomsArea = new TextArea();
        symptomsArea.setPromptText("Enter or Edit Symptoms");

        TextArea diagnosisArea = new TextArea();
        diagnosisArea.setPromptText("Enter or Edit Diagnosis Details");

        TextArea prescriptionArea = new TextArea();
        prescriptionArea.setPromptText("Enter or Edit Prescription Details");

           
        Button loadButton = new Button("Load Diagnosis Details");
        Button submitButton = new Button("Add New Diagnosis");
        Button updateButton = new Button("Update Existing Diagnosis");

           
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT AppointmentID FROM Appointments WHERE AppointmentType = 'Diagnosis' AND DoctorID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, doctorId);    
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                appointmentIdComboBox.getItems().add(String.valueOf(rs.getInt("AppointmentID")));
            }
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

           
        loadButton.setOnAction(e -> {
            String appointmentId = appointmentIdComboBox.getValue();
            if (appointmentId == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select an appointment.");
                alert.show();
                return;
            }

            try {
                Connection conn = DBConnection.getConnection();
                String sql = "SELECT Symptoms, Diagnosis, Prescription FROM Checkups WHERE AppointmentID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(appointmentId));
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    symptomsArea.setText(rs.getString("Symptoms"));
                    diagnosisArea.setText(rs.getString("Diagnosis"));
                    prescriptionArea.setText(rs.getString("Prescription"));
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "No diagnosis found for the selected appointment.");
                    alert.show();
                }

                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

           
        submitButton.setOnAction(e -> {
            String appointmentId = appointmentIdComboBox.getValue();
            String symptoms = symptomsArea.getText();
            String diagnosis = diagnosisArea.getText();
            String prescription = prescriptionArea.getText();

            if (appointmentId == null || symptoms.isEmpty() || diagnosis.isEmpty() || prescription.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please fill all fields.");
                alert.show();
                return;
            }

            try {
                Connection conn = DBConnection.getConnection();
                String sql = "INSERT INTO Checkups (AppointmentID, Symptoms, Diagnosis, Prescription) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(appointmentId));
                stmt.setString(2, symptoms);
                stmt.setString(3, diagnosis);
                stmt.setString(4, prescription);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Checkup Details Added Successfully!");
                    alert.show();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to Add Checkup Details.");
                    alert.show();
                }

                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

           
        updateButton.setOnAction(e -> {
            String appointmentId = appointmentIdComboBox.getValue();
            String symptoms = symptomsArea.getText();
            String diagnosis = diagnosisArea.getText();
            String prescription = prescriptionArea.getText();

            if (appointmentId == null || symptoms.isEmpty() || diagnosis.isEmpty() || prescription.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please fill all fields.");
                alert.show();
                return;
            }

            try {
                Connection conn = DBConnection.getConnection();
                String sql = "UPDATE Checkups SET Symptoms = ?, Diagnosis = ?, Prescription = ? WHERE AppointmentID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, symptoms);
                stmt.setString(2, diagnosis);
                stmt.setString(3, prescription);
                stmt.setInt(4, Integer.parseInt(appointmentId));

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Diagnosis Updated Successfully!");
                    alert.show();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to Update Diagnosis.");
                    alert.show();
                }

                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(header, appointmentIdComboBox, loadButton, symptomsArea, diagnosisArea, prescriptionArea, submitButton, updateButton);
        Scene scene = new Scene(layout, 400, 600);
        diagnosisStage.setScene(scene);
        diagnosisStage.setTitle("Diagnosis Management");
        diagnosisStage.show();
    }


    private void showUpdateVisit() {
        Stage visitStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label header = new Label("Update Visit Details:");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

           
        ComboBox<String> visitIdComboBox = new ComboBox<>();
        visitIdComboBox.setPromptText("Select Visit");

        TextField searchField = new TextField();
        searchField.setPromptText("Search Visit ID");

           
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT VisitID FROM visits WHERE DoctorID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, doctorId);    
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                visitIdComboBox.getItems().add(String.valueOf(rs.getInt("VisitID")));
            }
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

           
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            visitIdComboBox.getItems().clear();
            try {
                Connection conn = DBConnection.getConnection();
                String sql = "SELECT VisitID FROM visits WHERE DoctorID = ? AND VisitID LIKE ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, doctorId);    
                stmt.setString(2, "%" + newValue + "%");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    visitIdComboBox.getItems().add(String.valueOf(rs.getInt("VisitID")));
                }
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

           
        TextArea symptomsArea = new TextArea();
        symptomsArea.setPromptText("Enter Symptoms");

        TextArea diagnosisArea = new TextArea();
        diagnosisArea.setPromptText("Enter Diagnosis Details");

           
        Button loadButton = new Button("Load Visit Details");

        loadButton.setOnAction(e -> {
            String visitId = visitIdComboBox.getValue();
            if (visitId == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a visit.");
                alert.show();
                return;
            }

               
            try {
                Connection conn = DBConnection.getConnection();
                String sql = "SELECT Symptoms, Diagnosis FROM visits WHERE VisitID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(visitId));
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    symptomsArea.setText(rs.getString("Symptoms"));
                    diagnosisArea.setText(rs.getString("Diagnosis"));
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "No details found for the selected visit.");
                    alert.show();
                }

                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

           
        Button updateButton = new Button("Update Visit");

        updateButton.setOnAction(e -> {
            String visitId = visitIdComboBox.getValue();
            String symptoms = symptomsArea.getText();
            String diagnosis = diagnosisArea.getText();

            if (visitId == null || symptoms.isEmpty() || diagnosis.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please fill all fields.");
                alert.show();
                return;
            }

               
            try {
                Connection conn = DBConnection.getConnection();
                String sql = "UPDATE visits SET Symptoms = ?, Diagnosis = ? WHERE VisitID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, symptoms);
                stmt.setString(2, diagnosis);
                stmt.setInt(3, Integer.parseInt(visitId));

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Visit Details Updated Successfully!");
                    alert.show();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to Update Visit Details.");
                    alert.show();
                }

                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(header, searchField, visitIdComboBox, loadButton, symptomsArea, diagnosisArea, updateButton);
        Scene scene = new Scene(layout, 400, 500);
        visitStage.setScene(scene);
        visitStage.setTitle("Update Visit");
        visitStage.show();
    }



    private void manageVaccinationNotes() {
        Stage noteStage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

           
        ComboBox<String> appointmentIdComboBox = new ComboBox<>();
        appointmentIdComboBox.setPromptText("Select Appointment ID");

           
        TextArea notesField = new TextArea();
        notesField.setPromptText("Enter or Edit Notes for the Vaccination Appointment");
        notesField.setWrapText(true);
        notesField.setMaxHeight(100);

           
        Button loadButton = new Button("Load Notes");

           
        Button saveButton = new Button("Save Notes");

           
        populateAppointmentIds(appointmentIdComboBox);

           
        loadButton.setOnAction(e -> {
            String appointmentId = appointmentIdComboBox.getValue();
            if (appointmentId != null && !appointmentId.isEmpty()) {
                try {
                    Connection conn = DBConnection.getConnection();
                    String sql = "SELECT Notes FROM vaccinations WHERE AppointmentID = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, appointmentId);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String existingNotes = rs.getString("Notes");
                        notesField.setText(existingNotes != null ? existingNotes : "");
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "No data found for the selected appointment.");
                        alert.show();
                    }
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select an appointment.");
                alert.show();
            }
        });

           
        saveButton.setOnAction(e -> {
            String appointmentId = appointmentIdComboBox.getValue();
            String notes = notesField.getText();

            if (appointmentId == null || appointmentId.isEmpty() || notes.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select an appointment and fill in the notes.");
                alert.show();
                return;
            }

            try {
                Connection conn = DBConnection.getConnection();
                String sql = "UPDATE vaccinations SET Notes = ? WHERE AppointmentID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, notes);
                stmt.setString(2, appointmentId);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Notes saved successfully.");
                    alert.show();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Appointment not found.");
                    alert.show();
                }
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

           
        layout.getChildren().addAll(appointmentIdComboBox, loadButton, notesField, saveButton);

        Scene scene = new Scene(layout, 400, 450);
        noteStage.setScene(scene);
        noteStage.setTitle("Manage Notes for Vaccination Appointment");
        noteStage.show();
    }


       
    private void populateAppointmentIds(ComboBox<String> appointmentIdComboBox) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT v.AppointmentID FROM vaccinations v join appointments a on v.AppointmentID = a.AppointmentID where a.DoctorID=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1,doctorId) ;
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String appointmentId = rs.getString("AppointmentID");
                appointmentIdComboBox.getItems().add(appointmentId);    
            }
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }



    public void showUntakenVaccinationStage() {
           
        Stage vaccinationStage = new Stage();
        VBox layout = new VBox(10);     
        layout.setStyle("-fx-padding: 20px;");

           
        Label instructionLabel = new Label("Select a Patient to View Untaken Vaccination Details:");

           
        ComboBox<String> patientComboBox = new ComboBox<>();
        loadPatients(patientComboBox);     

           
        TextArea untakenVaccinationDetailsArea = new TextArea();
        untakenVaccinationDetailsArea.setEditable(false);     
        untakenVaccinationDetailsArea.setWrapText(true);      

           
        ScrollPane scrollPane = new ScrollPane(untakenVaccinationDetailsArea);
        scrollPane.setFitToWidth(true);      
        scrollPane.setMaxHeight(250);        

           
        patientComboBox.setOnAction(event -> {
            String selectedPatientId = patientComboBox.getValue();
            if (selectedPatientId != null) {
                   
                String untakenVaccinationInfo = getUntakenVaccinationInfo(selectedPatientId);
                untakenVaccinationDetailsArea.setText(untakenVaccinationInfo);
            }
        });

           
        layout.getChildren().addAll(
                instructionLabel,
                patientComboBox,
                scrollPane
        );

           
        Scene scene = new Scene(layout, 450, 400);
        vaccinationStage.setTitle("Untaken Vaccination Information");
        vaccinationStage.setScene(scene);
        vaccinationStage.show();
    }

       
    private String getUntakenVaccinationInfo(String patientId) {
        StringBuilder untakenVaccinationDetails = new StringBuilder();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/clinic2_db", "root","Nawar<<123")){
               
            String query = "SELECT v.VaccineID, v.VaccineName, v.AgeAtVaccination " +
                    "FROM Vaccines v " +
                    "LEFT JOIN vaccinations va ON v.VaccineID = va.VaccineID " +
                    "AND va.AppointmentID IN ( " +
                    "    SELECT AppointmentID FROM appointments WHERE PatientID = ? " +
                    ") " +
                    "CROSS JOIN Patients p " +
                    "WHERE va.VaccineID IS NULL " +
                    "AND v.AgeAtVaccination <= TIMESTAMPDIFF(MONTH, p.BirthDate, CURDATE()) " +
                    "AND p.PatientID = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, patientId);
                pstmt.setString(2, patientId);
                ResultSet rs = pstmt.executeQuery();
                   


                if (!rs.isBeforeFirst()) {
                    untakenVaccinationDetails.append("No untaken vaccinations found for this patient.");
                }

                while (rs.next()) {
                    untakenVaccinationDetails.append("Vaccine: ").append(rs.getString("VaccineName")).append("\n\n");

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error retrieving untaken vaccination data.";
        }

        return untakenVaccinationDetails.toString();
    }






}
