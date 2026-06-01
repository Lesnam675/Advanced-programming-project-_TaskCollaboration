package com.aastu.taskmanagersystem.backend.database;
import com.aastu.taskmanagersystem.client.model.Task;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class DatabaseConnection {

    public static Connection connect() {

        try {

            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/task_system",
                    "root",
                    "root1234$A"
            );

            System.out.println("Database Connected!");

            return connection;

        } catch (Exception e) {

            System.out.println(e.getMessage());

            return null;
        }
    }

    public static void insertTask(String title, String status){

        try{

            Connection connection = connect();

            String query = "INSERT INTO tasks(title, status) VALUES(?, ?)";

            var preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, title);
            preparedStatement.setString(2, status);

            preparedStatement.executeUpdate();

            System.out.println("Task Saved!");

        }catch (Exception e){

            System.out.println(e.getMessage());
        }
    }

    public static ResultSet getTasks(){

        try{

            Connection connection = connect();

            String query = "SELECT * FROM tasks";

            var statement = connection.createStatement();

            return statement.executeQuery(query);

        }catch (Exception e){

            System.out.println(e.getMessage());

            return null;
        }
    }

    public static void updateTaskStatus(String title, String status){

        try{

            Connection connection = connect();

            String query =
                    "UPDATE tasks SET status=? WHERE title=?";

            var preparedStatement =
                    connection.prepareStatement(query);

            preparedStatement.setString(1, status);
            preparedStatement.setString(2, title);

            preparedStatement.executeUpdate();

        }catch (Exception e){

            System.out.println(e.getMessage());
        }
    }

    public static void deleteTask(String title){

        try{

            Connection connection = connect();

            String query =
                    "DELETE FROM tasks WHERE title=?";

            var preparedStatement =
                    connection.prepareStatement(query);

            preparedStatement.setString(1, title);

            preparedStatement.executeUpdate();

        }catch (Exception e){

            System.out.println(e.getMessage());
        }
    }
}