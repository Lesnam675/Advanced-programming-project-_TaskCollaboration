package com.aastu.taskmanagersystem.client.api;

import com.aastu.taskmanagersystem.backend.model.UserEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class UserApi {

    private static final String BASE_URL = "http://localhost:8080/users";

    public static void register(String username, String password) {
        try {
            URL url = new URL(BASE_URL + "/register");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            String params = "username=" + URLEncoder.encode(username, "UTF-8")
                    + "&password=" + URLEncoder.encode(password, "UTF-8");

            OutputStream os = connection.getOutputStream();
            os.write(params.getBytes());
            os.flush();
            os.close();

            connection.getResponseCode();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean login(String username, String password) {
        try {
            URL url = new URL(BASE_URL + "/login?username=" + URLEncoder.encode(username, "UTF-8")
                    + "&password=" + URLEncoder.encode(password, "UTF-8"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();
            return "SUCCESS".equals(response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public static List<UserEntity> getAllUsers() {
        try {
            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            Gson gson = new Gson();
            Type listType = new TypeToken<List<UserEntity>>() {}.getType();
            return gson.fromJson(response.toString(), listType);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ArrayList<>();
    }
}