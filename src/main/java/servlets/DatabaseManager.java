package servlets;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private String URL;
    private String USER;
    private String PASSWORD;

    private DatabaseManager(String url, String user, String password) throws SQLException {
        this.URL = url;
        this.USER = user;
        this.PASSWORD = password;
        this.connection = DriverManager.getConnection(url, user, password);
    }

    public static DatabaseManager getInstance(String url, String user, String password) throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager(url, user, password);
        }

        return instance;
    }

    public ResultSet selectByInterval(Date from_date, Date to_date) throws SQLException {
        Statement statement = this.connection.createStatement();
        String var10001 = from_date.toString();
        ResultSet results = statement.executeQuery("SELECT * FROM public.records WHERE date_operation>='" + var10001 + "' AND date_operation<='" + to_date.toString() + "'");

//        while (results.next()) {
//            Integer id = results.getInt(1);
//            Boolean name = results.getBoolean(2);
//            Integer id_cat = results.getInt(3);
//            Date date = results.getDate(4);
//            Float total = results.getFloat(5);
//            System.out.println("" + id + " " + name + " " + id_cat + " " + String.valueOf(date) + " " + total);
//        }

        return results;
    }

    public ResultSet selectByCategory(ArrayList<Integer> category_names) throws SQLException {
        Statement statement = this.connection.createStatement();
        String var10001 = category_names.toString();
        int var10003 = category_names.toString().length();
        ResultSet results = statement.executeQuery("SELECT * FROM public.records WHERE id_category in (" + var10001.substring(1, var10003 - 1) + ")");

//        while (results.next()) {
//            Integer id = results.getInt(1);
//            Boolean name = results.getBoolean(2);
//            Integer id_cat = results.getInt(3);
//            Date date = results.getDate(4);
//            Float total = results.getFloat(5);
//            System.out.println("" + id + " " + name + " " + id_cat + " " + String.valueOf(date) + " " + total);
//        }

        return results;
    }

    public static ArrayList<Record> toArrayList(ResultSet results) throws SQLException {
        boolean operation;
        String date, id_cat;
        float total;
        ArrayList<Record> records = new ArrayList<Record>();
        while (results.next()) {
                    operation = results.getBoolean(2);
                    id_cat = Integer.toString(results.getInt(3));
                    date = results.getDate(4).toString();
                    total = results.getFloat(5);
                    records.add(new Record(operation,id_cat,date,total));
        }
        return records;
    }
}
