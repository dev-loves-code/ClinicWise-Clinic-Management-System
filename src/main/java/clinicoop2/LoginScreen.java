package clinicoop2;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;

public class LoginScreen {
    private VBox view;

    public LoginScreen(Stage stage) {
        view = new VBox(15);
        view.setPadding(new Insets(20));

        Label titleLabel = new Label("ClinicWise");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        ComboBox<String> roleSelector = new ComboBox<>();
        roleSelector.getItems().addAll("Receptionist", "Doctor");
        roleSelector.setPromptText("Select Role");

        Hyperlink registerLink = new Hyperlink("Don't have an account? Register here.");
        registerLink.setOnAction(e -> {
            RegistrationScreen registrationScreen = new RegistrationScreen(stage);
            stage.setScene(new Scene(registrationScreen.getView(), 600, 400));
        });

        loginButton.setOnAction(e -> {

            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String role = roleSelector.getValue();


            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Login Error", "Username and password cannot be empty.");
                return;
            }

            if (role == null) {
                showAlert(Alert.AlertType.ERROR, "Login Error", "Please select a role.");
                return;
            }


            loginButton.setDisable(true);
            usernameField.setDisable(true);
            passwordField.setDisable(true);
            roleSelector.setDisable(true);
            progressIndicator.setVisible(true);


            Task<LoginResult> loginTask = new Task<>() {
                @Override
                protected LoginResult call() throws Exception {
                    try {

                        switch (role) {
                            case "Receptionist":
                                boolean isReceptionistValid = Receptionist.validateReceptionistLogin(username, password);
                                return isReceptionistValid
                                        ? LoginResult.success("receptionist")
                                        : LoginResult.failure("Invalid receptionist credentials");

                            case "Doctor":
                                int doctorId = Doctor.validateDoctorLogin(username, password);
                                return doctorId != -1
                                        ? LoginResult.success("doctor", doctorId)
                                        : LoginResult.failure("Invalid doctor credentials");

                            default:
                                return LoginResult.failure("Invalid role selected");
                        }
                    } catch (SQLException ex) {

                        ex.printStackTrace();
                        return LoginResult.failure("Database error: " + ex.getMessage());
                    } catch (Exception ex) {

                        ex.printStackTrace();
                        return LoginResult.failure("Unexpected error occurred");
                    }
                }
            };


            loginTask.setOnSucceeded(event -> {

                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    usernameField.setDisable(false);
                    passwordField.setDisable(false);
                    roleSelector.setDisable(false);
                    progressIndicator.setVisible(false);


                    LoginResult result = loginTask.getValue();
                    if (result.isSuccess()) {
                        try {
                            switch (result.getResultType()) {
                                case "receptionist":
                                    ReceptionistDashboard receptionistDashboard = new ReceptionistDashboard(stage);
                                    stage.setScene(new Scene(receptionistDashboard.getView(), 800, 600));
                                    break;
                                case "doctor":
                                    DoctorDashboard doctorDashboard = new DoctorDashboard(stage, result.getDoctorId());
                                    stage.setScene(new Scene(doctorDashboard.getView(), 800, 600));
                                    break;
                            }
                        } catch (Exception ex) {
                            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                                    "Could not open dashboard: " + ex.getMessage());
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Login Failed", result.getMessage());
                    }
                });
            });


            loginTask.setOnFailed(event -> {
                Platform.runLater(() -> {

                    loginButton.setDisable(false);
                    usernameField.setDisable(false);
                    passwordField.setDisable(false);
                    roleSelector.setDisable(false);
                    progressIndicator.setVisible(false);

                    showAlert(Alert.AlertType.ERROR, "Login Error",
                            "An unexpected error occurred during login process.");
                });
            });


            new Thread(loginTask).start();
        });


        view.getChildren().addAll(
                titleLabel,
                usernameField,
                passwordField,
                roleSelector,
                loginButton,
                progressIndicator,
                registerLink
        );
    }

    public VBox getView() {
        return view;
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }


    private static class LoginResult {
        private final boolean success;
        private final String resultType;
        private final int doctorId;
        private final String message;


        private LoginResult(boolean success, String resultType, int doctorId, String message) {
            this.success = success;
            this.resultType = resultType;
            this.doctorId = doctorId;
            this.message = message;
        }


        public static LoginResult success(String resultType) {
            return new LoginResult(true, resultType, -1, null);
        }


        public static LoginResult success(String resultType, int doctorId) {
            return new LoginResult(true, resultType, doctorId, null);
        }


        public static LoginResult failure(String message) {
            return new LoginResult(false, null, -1, message);
        }


        public boolean isSuccess() {
            return success;
        }

        public String getResultType() {
            return resultType;
        }

        public int getDoctorId() {
            return doctorId;
        }

        public String getMessage() {
            return message;
        }
    }
}