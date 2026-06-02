package com.aastu.taskmanagersystem.backend.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.PrintWriter;

public class ClientHandler extends Thread {

    private PrintWriter writer;
    private Socket socket;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            while (true) {
                String message = reader.readLine();
                if (message == null) {
                    break;
                }

                System.out.println("Client Says: " + message);

                if (message.startsWith("PRESENCE:ONLINE:")) {
                    String user = message.substring("PRESENCE:ONLINE:".length()).trim();
                    this.username = user;

                    // Send the new online user to all clients
                    synchronized (TaskServer.clients) {
                        for (ClientHandler client : TaskServer.clients) {
                            client.writer.println(message);
                        }
                    }

                    // Send the current list of online users to the newly connected user
                    StringBuilder listMsg = new StringBuilder("PRESENCE:LIST:");
                    synchronized (TaskServer.clients) {
                        boolean first = true;
                        for (ClientHandler client : TaskServer.clients) {
                            if (client.username != null && !client.username.equals(user)) {
                                if (!first) listMsg.append(",");
                                listMsg.append(client.username);
                                first = false;
                            }
                        }
                    }
                    this.writer.println(listMsg.toString());

                } else {
                    // Regular broadcast
                    synchronized (TaskServer.clients) {
                        for (ClientHandler client : TaskServer.clients) {
                            client.writer.println(message);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Client handler exception: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (Exception e) {}

            synchronized (TaskServer.clients) {
                TaskServer.clients.remove(this);
            }

            if (this.username != null) {
                System.out.println("User offline: " + this.username);
                synchronized (TaskServer.clients) {
                    for (ClientHandler client : TaskServer.clients) {
                        client.writer.println("PRESENCE:OFFLINE:" + this.username);
                    }
                }
            }
        }
    }
}