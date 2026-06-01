package com.aastu.taskmanagersystem.client.ui;
import com.aastu.taskmanagersystem.client.model.Task;


import com.aastu.taskmanagersystem.client.api.UserApi;
import com.aastu.taskmanagersystem.backend.model.UserEntity;
import com.aastu.taskmanagersystem.client.ui.styles.UiStyles;
import com.aastu.taskmanagersystem.client.ui.styles.UiWindowHelper;
import com.aastu.taskmanagersystem.backend.database.DatabaseConnection;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginScreen extends Application {

    @Override
    public void start(Stage stage) {

        Label title = new Label("Task Manager");
        title.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 26px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT + ";"
        );

        Label subtitle = new Label("Sign in to your workspace");
        subtitle.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 13px;"
                        + "-fx-text-fill: " + UiStyles.COLOR_TEXT_MUTED + ";"
        );

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        UiStyles.applyFieldEffects(usernameField);
        usernameField.setMaxWidth(Double.MAX_VALUE);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        UiStyles.applyFieldEffects(passwordField);
        passwordField.setMaxWidth(Double.MAX_VALUE);

        Label statusLabel = new Label();
        statusLabel.setStyle(
                UiStyles.FONT_FAMILY
                        + "-fx-font-size: 12px;"
                        + "-fx-text-fill: " + UiStyles.PRIORITY_HIGH + ";"
        );
        statusLabel.setWrapText(true);

        Button loginButton = new Button("Sign in");
        UiStyles.applyButtonEffects(loginButton, true);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(38);

        Button registerButton = new Button("Create account");
        UiStyles.applyButtonEffects(registerButton, false);
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setPrefHeight(38);

        loginButton.setOnAction(e -> {

            boolean result =
                    UserApi.login(
                            usernameField.getText(),
                            passwordField.getText()
                    );

            if (result) {
                UserEntity user = new UserEntity();
                user.setUsername(usernameField.getText().trim());

                Main.setCurrentUser(user);
                System.out.println("[LOGIN] user='" + user.getUsername() + "'");

                Platform.runLater(() -> {
                    try {
                        new Main().start(new Stage());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                stage.close();

            } else {
                statusLabel.setText("Invalid username or password");
            }
        });

        registerButton.setOnAction(e -> {

            UserApi.register(
                    usernameField.getText(),
                    passwordField.getText()
            );

            statusLabel.setText("Account created — you can sign in now");
            statusLabel.setStyle(
                    UiStyles.FONT_FAMILY
                            + "-fx-font-size: 12px;"
                            + "-fx-text-fill: " + UiStyles.ACCENT_DONE + ";"
            );
        });

        VBox form = new VBox(16);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(40, 45, 40, 45));
        form.setMaxWidth(400);
        form.setStyle(
                "-fx-background-color: " + UiStyles.COLOR_SURFACE + ";"
                        + "-fx-background-radius: 16;"
                        + "-fx-border-color: " + UiStyles.COLOR_BORDER + ";"
                        + "-fx-border-radius: 16;"
                        + "-fx-border-width: 1;"
                        + "-fx-effect: dropshadow(gaussian, rgba(9,30,66,0.12), 24, 0, 0, 8);"
        );
        form.getChildren().addAll(
                title,
                subtitle,
                usernameField,
                passwordField,
                loginButton,
                registerButton,
                statusLabel
        );

        StackPane root = new StackPane(form);
        root.setStyle(UiStyles.BG_APP);
        StackPane.setAlignment(form, Pos.CENTER);

        UiWindowHelper.configureLoginStage(stage, root);
        stage.setTitle("Task Manager — Sign in");
        stage.show();
    }

    public static void main(String[] args) {
        DatabaseConnection.connect();
        launch(args);
    }
}
