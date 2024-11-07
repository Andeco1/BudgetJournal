package servlets;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

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

    public ResultSet selectRecords(String from_date, String to_date,String[] category_names, String operationType) throws SQLException {
        Statement statement = this.connection.createStatement();
        String operaion="";
        switch (operationType){
            case "any":
                operaion = "";
                break;
            case "+":
                operaion = " AND operation = false";
                break;
            case "-":
                operaion = " AND operation = true";
                break;
        }
        if(from_date == ""){
            from_date = "1999-01-01";
        }
        if(to_date == ""){
            to_date = "2024-08-11";
        }
        if(category_names == null){
            category_names = this.selectAllCategories().toArray(new String[0]);
        }
        StringBuilder category_namesString = new StringBuilder("");
        for(int i = 0; i<category_names.length;i++){
            category_namesString.append("'"+category_names[i]+"',");
        }
        category_namesString.deleteCharAt(category_namesString.length()-1);

//        while (results.next()) {
//            Integer id = results.getInt(1);
//            Boolean name = results.getBoolean(2);
//            Integer id_cat = results.getInt(3);
//            Date date = results.getDate(4);
//            Float total = results.getFloat(5);
//            System.out.println("" + id + " " + name + " " + id_cat + " " + String.valueOf(date) + " " + total);
//        }
        return statement.executeQuery("SELECT * FROM public.records INNER JOIN public.categories ON records.id_category = categories.id_category" +
                " WHERE date_operation>='" + from_date + "' AND date_operation<='" + to_date + "'" +"AND category_name in (" + category_namesString.toString() +")"+operaion);
    }

    public ResultSet selectByInterval(String from_date, String to_date) throws SQLException {
        Statement statement = this.connection.createStatement();
//        while (results.next()) {
//            Integer id = results.getInt(1);
//            Boolean name = results.getBoolean(2);
//            Integer id_cat = results.getInt(3);
//            Date date = results.getDate(4);
//            Float total = results.getFloat(5);
//            System.out.println("" + id + " " + name + " " + id_cat + " " + String.valueOf(date) + " " + total);
//        }
        return statement.executeQuery("SELECT * FROM public.records INNER JOIN public.categories ON records.id_category = categories.id_category" +
                " WHERE date_operation>='" + from_date + "' AND date_operation<='" + to_date + "'");
    }

    public ResultSet selectByCategory(ArrayList<Integer> category_names) throws SQLException {
        Statement statement = this.connection.createStatement();
        String category_namesString = category_names.toString();
        int endOfString = category_namesString.length();
//        while (results.next()) {
//            Integer id = results.getInt(1);
//            Boolean name = results.getBoolean(2);
//            Integer id_cat = results.getInt(3);
//            Date date = results.getDate(4);
//            Float total = results.getFloat(5);
//            System.out.println("" + id + " " + name + " " + id_cat + " " + String.valueOf(date) + " " + total);
//        }
        return statement.executeQuery("SELECT * FROM public.records INNER JOIN public.categories ON records.id_category = categories.id_category " +
                "WHERE category_name in (" + category_namesString.substring(1, endOfString - 1) + ")");
    }


    public ArrayList<String> selectAllCategories() throws SQLException {
        Statement statement = this.connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT category_name FROM public.categories");
        ArrayList<String> categories_names = new ArrayList<>();
        while (results.next()) {
            categories_names.add(results.getString(1));
        }
        return categories_names;
    }
    public static ArrayList<Record> toArrayList(ResultSet results) throws SQLException {
        boolean operation;
        String date, id_cat;
        float total;
        ArrayList<Record> records = new ArrayList<Record>();
        while (results.next()) {
                    operation = results.getBoolean(2);
                    id_cat = results.getString(7);
                    date = results.getDate(4).toString();
                    total = results.getFloat(5);
                    records.add(new Record(operation,id_cat,date,total));
        }
        return records;
    }
}
