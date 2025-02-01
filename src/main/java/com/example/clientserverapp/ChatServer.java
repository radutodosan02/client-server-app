package com.example.clientserverapp;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final Set<PrintWriter> clientWriters = new HashSet<>();
    private static final Map<Socket, String> userNames = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Serverul este pornit...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String userName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                userName = in.readLine();  // Citim numele utilizatorului la conectare
                synchronized (userNames) {
                    userNames.put(socket, userName);
                }

                broadcastMessage(userName + " s-a alăturat chatului!");

                String message;
                while ((message = in.readLine()) != null) {
                    broadcastMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnectUser();
            }
        }

        private void disconnectUser() {
            if (userName != null) {
                broadcastMessage(userName + " s-a deconectat.");
            }

            synchronized (clientWriters) {
                clientWriters.remove(out);
            }

            synchronized (userNames) {
                userNames.remove(socket);
            }

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
