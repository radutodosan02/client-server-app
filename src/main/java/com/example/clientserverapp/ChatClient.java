package com.example.clientserverapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.*;
import java.net.*;

public class ChatClient extends Application {
    private String userName;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        showLoginOrSignup(primaryStage);
    }

    private void showLoginOrSignup(Stage primaryStage) {
        Stage authStage = new Stage();
        VBox root = new VBox(10);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Nume utilizator");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Parolă");
        Button loginButton = new Button("Autentificare");
        Button signupButton = new Button("Înregistrare");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (DatabaseHelper.authenticateUser(username, password)) {
                userName = username;
                authStage.close();
                try {
                    // Connect to the server after successful login
                    socket = new Socket("localhost", 12345); // Adjust port as needed
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out.println(userName); // Send username to the server
                    showChatWindow(primaryStage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Unable to connect to server!").show();
                }
            } else {
                new Alert(Alert.AlertType.ERROR, "Nume sau parolă incorectă!").show();
            }
        });

        signupButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (DatabaseHelper.registerUser(username, password)) {
                new Alert(Alert.AlertType.INFORMATION, "Cont creat cu succes!").show();
            } else {
                new Alert(Alert.AlertType.ERROR, "Numele de utilizator este deja folosit!").show();
            }
        });

        root.getChildren().addAll(new Label("Autentificare / Înregistrare"), usernameField, passwordField, loginButton, signupButton);
        authStage.setScene(new Scene(root, 300, 200));
        authStage.show();
    }

    private void showChatWindow(Stage primaryStage) {
        VBox chatRoot = new VBox(10);
        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);  // Prevent user from typing directly in the chat area
        TextField messageField = new TextField();
        Button sendButton = new Button("Trimite");

        // Set the title of the chat window to include the logged-in user's name
        primaryStage.setTitle("Chat - " + userName);

        sendButton.setOnAction(e -> sendMessage(messageField));

        // Allow sending the message by pressing "ENTER"
        messageField.setOnAction(e -> sendMessage(messageField));

        // Listen for incoming messages from the server
        new Thread(() -> {
            String receivedMessage;
            try {
                while ((receivedMessage = in.readLine()) != null) {
                    // Prepend the username to the message and update the chat area
                    String finalReceivedMessage = receivedMessage;
                    javafx.application.Platform.runLater(() -> chatArea.appendText(finalReceivedMessage + "\n"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        chatRoot.getChildren().addAll(chatArea, messageField, sendButton);
        primaryStage.setScene(new Scene(chatRoot, 400, 300));
        primaryStage.show();
    }

    // Separate function to send the message
    private void sendMessage(TextField messageField) {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            out.println(userName +  ": " + message);  // Send message to the server
            messageField.clear();  // Clear the input field after sending
        }
    }
}
