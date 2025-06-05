package clinicoop2;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegistrationScreen {
    private VBox view;

    public RegistrationScreen(Stage stage) {
        view = new VBox(15);
        view.setPadding(new Insets(20));

        Label titleLabel = new Label("User Registration");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        ComboBox<String> roleSelector = new ComboBox<>();
        roleSelector.getItems().addAll("Receptionist", "Doctor");
        roleSelector.setPromptText("Select Role");


        TextField specializationField = new TextField();
        specializationField.setPromptText("Specialization (Doctor only)");
        specializationField.setVisible(false);

        TextField phoneNumberField = new TextField();
        phoneNumberField.setPromptText("Phone Number (Doctor only)");
        phoneNumberField.setVisible(false);

        TextField emailField = new TextField();
        emailField.setPromptText("Email (Doctor only)");
        emailField.setVisible(false);


        roleSelector.setOnAction(e -> {
            boolean isDoctor = "Doctor".equals(roleSelector.getValue());
            specializationField.setVisible(isDoctor);
            phoneNumberField.setVisible(isDoctor);
            emailField.setVisible(isDoctor);
        });

        Button registerButton = new Button("Register");

        Hyperlink loginLink = new Hyperlink("Already have an account? Login here.");
        loginLink.setOnAction(e -> {
            LoginScreen loginScreen = new LoginScreen(stage);
            stage.setScene(new Scene(loginScreen.getView(), 600, 400));
        });

        registerButton.setOnAction(e -> {
            String name = nameField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();
            String role = roleSelector.getValue();

            try {
                // Validate input fields
                if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
                    return;
                }

                if (role == null) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please select a role.");
                    return;
                }


                if ("Doctor".equals(role)) {
                    if (Doctor.isUsernameTaken(username)) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Username already exists for doctors.");
                        return;
                    }


                    String specialization = specializationField.getText();
                    String phoneNumber = phoneNumberField.getText();
                    String email = emailField.getText();

                    if (specialization.isEmpty() || phoneNumber.isEmpty() || email.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Error", "All doctor fields are required.");
                        return;
                    }

                    Doctor.registerDoctor(name, username, password, specialization, phoneNumber, email);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Doctor registered successfully.");

                } else if ("Receptionist".equals(role)) {
                    if (Receptionist.isUsernameTaken(username)) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Username already exists for receptionists.");
                        return;
                    }

                    Receptionist.registerReceptionist(name, username, password);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Receptionist registered successfully.");
                }


                LoginScreen loginScreen = new LoginScreen(stage);
                stage.setScene(new Scene(loginScreen.getView(), 400, 300));

            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + ex.getMessage());
            }
        });

        view.getChildren().addAll(
                titleLabel, nameField, usernameField, passwordField,
                roleSelector, specializationField, phoneNumberField,
                emailField, registerButton, loginLink
        );
    }

    public VBox getView() {
        return view;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}