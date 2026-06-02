package com.aastu.taskmanagersystem.backend.socket;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TaskServer {
    public static ArrayList<ClientHandler>
            clients = new ArrayList<>();

    public static void main(String[] args) {

        try{

            ServerSocket serverSocket =
                    new ServerSocket(5000);

            System.out.println("Server Started...");

            while(true){

                Socket socket =
                        serverSocket.accept();

                System.out.println(
                        "Client Connected!"
                );
                ClientHandler clientHandler =
                        new ClientHandler(socket);
                synchronized (clients) {
                    clients.add(clientHandler);
                }

                clientHandler.start();
            }


        }catch (Exception e){

            System.out.println(e.getMessage());
        }
    }
}