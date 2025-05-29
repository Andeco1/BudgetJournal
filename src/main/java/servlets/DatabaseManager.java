package servlets;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private static DatabaseManager instance;
    private Connection connection;
    private String url;
    private String user;
    private String password;
    private String schema;

    static {
        try {
            Class.forName("org.postgresql.Driver");
            logger.info("PostgreSQL driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.severe("Failed to load PostgreSQL driver: " + e.getMessage());
            throw new RuntimeException("Failed to load PostgreSQL driver", e);
        }
    }

    private DatabaseManager(String url, String user, String password) throws SQLException {
        this.url = url;
        this.user = user;
        this.password = password;
        this.schema = System.getenv("DB_SCHEMA");
        if (this.schema == null) {
            this.schema = "budget_journal";
        }
        logger.info("Initializing DatabaseManager with schema: " + this.schema);
        connect();
    }

    public static DatabaseManager getInstance(String url, String user, String password) throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager(url, user, password);
        }
        return instance;
    }

    private Connection connect() throws SQLException {
        try {
            logger.info("Connecting to database: " + url);
            connection = DriverManager.getConnection(url, user, password);
            
            // Verify connection
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Failed to establish database connection");
            }
            
            // Set the search path to our schema
            try (Statement stmt = connection.createStatement()) {
                // First check if schema exists
                ResultSet rs = stmt.executeQuery("SELECT schema_name FROM information_schema.schemata WHERE schema_name = '" + schema + "'");
                if (!rs.next()) {
                    logger.warning("Schema " + schema + " does not exist, attempting to create it");
                    stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
                }
                
                // Set search path
                stmt.execute("SET search_path TO " + schema);
                logger.info("Search path set to: " + schema);
                
                // Verify we can access the categories table
                rs = stmt.executeQuery("SELECT COUNT(*) FROM " + schema + ".categories");
                if (rs.next()) {
                    int count = rs.getInt(1);
                    logger.info("Successfully accessed categories table. Found " + count + " categories.");
                }
            }
            
            return connection;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database: " + e.getMessage(), e);
            throw new SQLException("Failed to connect to database", e);
        }
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
        if(Objects.equals(from_date, "")){
            from_date = "1999-01-01";
        }
        if(Objects.equals(to_date, "")){
            to_date = "2024-08-11";
        }
        if(category_names == null){
            category_names = this.selectAllCategories().toArray(new String[0]);
        }
        StringBuilder category_namesString = new StringBuilder("");
        for(int i = 0; i<category_names.length;i++){
            category_namesString.append("'").append(category_names[i]).append("',");
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
        return statement.executeQuery("SELECT * FROM budget_journal.records INNER JOIN budget_journal.categories ON records.id_category = categories.id_category" +
                " WHERE date_operation>='" + from_date + "' AND date_operation<='" + to_date + "'" +"AND category_name in (" + category_namesString.toString() +")"+operaion);
    }

    public ArrayList<Integer> getPercentage(String from_date, String to_date,String[] category_names, String operationType) throws SQLException {
        ResultSet resultSet = selectByCategory(from_date, to_date, category_names, operationType);
        ArrayList<String> categories_names = new ArrayList<>();
        ArrayList<Integer> total_by_categories = new ArrayList<>();
        Integer total_amount = 0;
        Integer current_amount = 0;
        StringBuilder output = new StringBuilder();
        output.append("[");
        while(resultSet.next()){
            categories_names.add(resultSet.getString(1));
            current_amount = Math.round(resultSet.getFloat(2));
            total_amount += current_amount;
            total_by_categories.add(current_amount);
        }
        return total_by_categories;
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
        return statement.executeQuery("SELECT * FROM budget_journal.records INNER JOIN budget_journal.categories ON records.id_category = categories.id_category" +
                " WHERE date_operation>='" + from_date + "' AND date_operation<='" + to_date + "'");
    }

    public ResultSet selectByCategory(String from_date, String to_date,String[] category_names, String operationType) throws SQLException {
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
        if(Objects.equals(from_date, "")){
            from_date = "1999-01-01";
        }
        if(Objects.equals(to_date, "")){
            to_date = "2024-08-11";
        }
        if(category_names == null){
            category_names = this.selectAllCategories().toArray(new String[0]);
        }
        StringBuilder category_namesString = new StringBuilder("");
        for(int i = 0; i<category_names.length;i++){
            category_namesString.append("'").append(category_names[i]).append("',");
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
        return statement.executeQuery("SELECT category_name, sum(total) from budget_journal.records inner join budget_journal.categories on records.id_category = "+
                "categories.id_category"+
                " WHERE date_operation>='" + from_date + "' AND date_operation<='" + to_date + "'" +
                "AND category_name in (" + category_namesString.toString() +")"+operaion+
                "Group by category_name having category_name in ("+ category_namesString.toString() + ")");
    }

    public ArrayList<ArrayList<Float>> getStatistics(String from_date, String to_date,String[] category_names) throws SQLException {
        Statement statement = this.connection.createStatement();
        ArrayList<ArrayList<Float>> stats = new ArrayList<>();
        if(Objects.equals(from_date, "")){
            from_date = "1999-01-01";
        }
        if(Objects.equals(to_date, "")){
            to_date = "2024-08-11";
        }
        if(category_names == null){
            category_names = this.selectAllCategories().toArray(new String[0]);
        }
        ArrayList<String> dates = new ArrayList<>();
        dates = selectAllDates(from_date,to_date);
        for(int i = 0; i<category_names.length; i++){
            ArrayList<Float>  new_category = new ArrayList<Float>(dates.size());
            { for (int p = 0; p < dates.size(); p++) new_category.add(0f);}
            ResultSet resultSet = statement.executeQuery("SELECT date_operation, total FROM budget_journal.records INNER JOIN budget_journal.categories ON records.id_category = categories.id_category" +
                    " WHERE date_operation>='" + from_date + "' AND date_operation<='" + to_date + "'" +"AND category_name in ('" + category_names[i] +"')");
            Float intotal;
            while(resultSet.next()){
                String date = resultSet.getString(1);
                Float tot = resultSet.getFloat(2);
                new_category.set(dates.indexOf(date), tot);
            }
            stats.add(new_category);
        }
        return stats;
    }
    public  ArrayList<String> selectAllDates(String from_date, String to_date) throws SQLException {
        Statement statement = this.connection.createStatement();
        if(Objects.equals(from_date, "")){
            from_date = "1999-01-01";
        }
        if(Objects.equals(to_date, "")){
            to_date = "2024-08-11";
        }
        ArrayList<String> date_operatiions = new ArrayList<>();
        ResultSet resultSet = statement.executeQuery("SELECT date_operation FROM budget_journal.records INNER JOIN budget_journal.categories ON records.id_category = categories.id_category" +
                " WHERE date_operation>='" + from_date + "' AND date_operation<='" + to_date + "' order by date_operation");
        while(resultSet.next()){
            date_operatiions.add(resultSet.getString(1));
        }
        return date_operatiions;
    }
    public ArrayList<String> selectAllCategories() throws SQLException {
        logger.info("Fetching all categories from database");
        try (Statement statement = this.connection.createStatement()) {
            ResultSet results = statement.executeQuery("SELECT category_name FROM " + schema + ".categories ORDER BY category_name");
            ArrayList<String> categories_names = new ArrayList<>();
            while (results.next()) {
                String category = results.getString(1);
                categories_names.add(category);
                logger.fine("Found category: " + category);
            }
            logger.info("Total categories found: " + categories_names.size());
            return categories_names;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching categories: " + e.getMessage(), e);
            throw e;
        }
    }
    public ArrayList<String> selectCategories(String[] category_names) throws SQLException {
        Statement statement = this.connection.createStatement();
        StringBuilder category_namesString = new StringBuilder("");
        for(int i = 0; i<category_names.length;i++){
            category_namesString.append("'").append(category_names[i]).append("',");
        }
        category_namesString.deleteCharAt(category_namesString.length()-1);
        ResultSet results = statement.executeQuery("SELECT category_name FROM budget_journal.categories"+" where category_name in (" + category_namesString.toString() +")");
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

    public void addRecord(String category, String date, float total, boolean isExpense) throws SQLException {
        logger.info("Adding record: category=" + category + ", date=" + date + ", total=" + total + ", isExpense=" + isExpense);
        
        try (Connection conn = connect()) {
            // First, get the category ID
            int categoryId;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT id_category FROM " + schema + ".categories WHERE category_name = ?")) {
                pstmt.setString(1, category);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        logger.severe("Category not found: " + category);
                        throw new SQLException("Category not found: " + category);
                    }
                    categoryId = rs.getInt("id_category");
                }
            }

            // Then insert the record
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO " + schema + ".records (operation, id_category, date_operation, total) VALUES (?, ?, ?, ?)")) {
                pstmt.setBoolean(1, isExpense);
                pstmt.setInt(2, categoryId);
                pstmt.setDate(3, Date.valueOf(date));
                pstmt.setFloat(4, total);
                pstmt.executeUpdate();
                logger.info("Record added successfully");
            }
        }
    }
}
