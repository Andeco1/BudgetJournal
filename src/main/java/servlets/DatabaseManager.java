package servlets;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private static DatabaseManager instance;
    private HikariDataSource dataSource;
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
        this.schema = System.getenv("DB_SCHEMA");
        if (this.schema == null) {
            this.schema = "budget_journal";
        }
        logger.info("Initializing DatabaseManager with schema: " + this.schema);
        
        // Configure HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(30000);
        config.setAutoCommit(true);
        
        // Add schema to connection properties
        config.addDataSourceProperty("currentSchema", schema);
        
        // Create the data source
        dataSource = new HikariDataSource(config);
        
        // Initialize schema
        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                // Check if schema exists
                ResultSet rs = stmt.executeQuery("SELECT schema_name FROM information_schema.schemata WHERE schema_name = '" + schema + "'");
                if (!rs.next()) {
                    logger.warning("Schema " + schema + " does not exist, attempting to create it");
                    stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
                }
                
                // Set search path
                stmt.execute("SET search_path TO " + schema);
                logger.info("Search path set to: " + schema);
            }
        }
    }

    public static DatabaseManager getInstance(String url, String user, String password) throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager(url, user, password);
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        try {
            Connection conn = dataSource.getConnection();
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Failed to get connection from pool");
            }
            return conn;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get connection from pool: " + e.getMessage(), e);
            throw new SQLException("Failed to get connection from pool", e);
        }
    }

    public ArrayList<String> selectAllCategories() throws SQLException {
        logger.info("Fetching all categories from database");
        ArrayList<String> categories = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT category_name FROM " + schema + ".categories ORDER BY category_name")) {
            
            while (rs.next()) {
                String category = rs.getString("category_name");
                categories.add(category);
                logger.fine("Found category: " + category);
            }
            
            logger.info("Total categories found: " + categories.size());
            return categories;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching categories: " + e.getMessage(), e);
            throw e;
        }
    }

    public void addRecord(String category, String date, float total, boolean isExpense) throws SQLException {
        logger.info("Adding record: category=" + category + ", date=" + date + ", total=" + total + ", isExpense=" + isExpense);
        
        try (Connection conn = getConnection()) {
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

    public ResultSet selectRecords(String from_date, String to_date, String[] category_names, String operationType) throws SQLException {
        logger.info("Selecting records with filters: from=" + from_date + ", to=" + to_date + ", categories=" + Arrays.toString(category_names) + ", operation=" + operationType);
        
        try (Connection conn = getConnection()) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT r.*, c.category_name FROM ").append(schema).append(".records r ")
                 .append("INNER JOIN ").append(schema).append(".categories c ON r.id_category = c.id_category ")
                 .append("WHERE 1=1 ");

            if (from_date != null && !from_date.isEmpty()) {
                query.append("AND r.date_operation >= '").append(from_date).append("' ");
            }
            if (to_date != null && !to_date.isEmpty()) {
                query.append("AND r.date_operation <= '").append(to_date).append("' ");
            }
            if (category_names != null && category_names.length > 0) {
                query.append("AND c.category_name IN (");
                for (int i = 0; i < category_names.length; i++) {
                    query.append("'").append(category_names[i]).append("'");
                    if (i < category_names.length - 1) {
                        query.append(",");
                    }
                }
                query.append(") ");
            }
            if (operationType != null && !operationType.equals("any")) {
                query.append("AND r.operation = ").append(operationType.equals("-"));
            }

            query.append("ORDER BY r.date_operation DESC");

            logger.info("Executing query: " + query.toString());
            return conn.createStatement().executeQuery(query.toString());
        }
    }

    public static ArrayList<Record> toArrayList(ResultSet results) throws SQLException {
        ArrayList<Record> records = new ArrayList<>();
        while (results.next()) {
            boolean operation = results.getBoolean("operation");
            String category_name = results.getString("category_name");
            String date = results.getDate("date_operation").toString();
            float total = results.getFloat("total");
            records.add(new Record(operation, category_name, date, total));
        }
        return records;
    }

    public ArrayList<Integer> getPercentage(String from_date, String to_date, String[] category_names, String operationType) throws SQLException {
        logger.info("Calculating percentages for categories");
        ArrayList<Integer> percentages = new ArrayList<>();
        
        try (Connection conn = getConnection()) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT c.category_name, SUM(r.total) as total_amount ")
                 .append("FROM ").append(schema).append(".records r ")
                 .append("INNER JOIN ").append(schema).append(".categories c ON r.id_category = c.id_category ")
                 .append("WHERE 1=1 ");

            if (from_date != null && !from_date.isEmpty()) {
                query.append("AND r.date_operation >= '").append(from_date).append("' ");
            }
            if (to_date != null && !to_date.isEmpty()) {
                query.append("AND r.date_operation <= '").append(to_date).append("' ");
            }
            if (category_names != null && category_names.length > 0) {
                query.append("AND c.category_name IN (");
                for (int i = 0; i < category_names.length; i++) {
                    query.append("'").append(category_names[i]).append("'");
                    if (i < category_names.length - 1) {
                        query.append(",");
                    }
                }
                query.append(") ");
            }
            if (operationType != null && !operationType.equals("any")) {
                query.append("AND r.operation = ").append(operationType.equals("-"));
            }

            query.append("GROUP BY c.category_name");

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query.toString())) {
                
                float totalSum = 0;
                ArrayList<Float> categoryTotals = new ArrayList<>();
                
                while (rs.next()) {
                    float amount = rs.getFloat("total_amount");
                    totalSum += amount;
                    categoryTotals.add(amount);
                }
                
                // Calculate percentages
                for (Float amount : categoryTotals) {
                    int percentage = Math.round((amount / totalSum) * 100);
                    percentages.add(percentage);
                }
            }
        }
        
        return percentages;
    }

    public ArrayList<ArrayList<Float>> getStatistics(String from_date, String to_date, String[] category_names) throws SQLException {
        logger.info("Getting statistics for categories");
        ArrayList<ArrayList<Float>> stats = new ArrayList<>();
        
        try (Connection conn = getConnection()) {
            // Get all dates in range
            ArrayList<String> dates = selectAllDates(from_date, to_date);
            
            // For each category, get daily totals
            for (String category : category_names) {
                ArrayList<Float> categoryStats = new ArrayList<>(dates.size());
                for (int i = 0; i < dates.size(); i++) {
                    categoryStats.add(0f);
                }
                
                StringBuilder query = new StringBuilder();
                query.append("SELECT r.date_operation, SUM(r.total) as daily_total ")
                     .append("FROM ").append(schema).append(".records r ")
                     .append("INNER JOIN ").append(schema).append(".categories c ON r.id_category = c.id_category ")
                     .append("WHERE c.category_name = ? ")
                     .append("AND r.date_operation >= ? ")
                     .append("AND r.date_operation <= ? ")
                     .append("GROUP BY r.date_operation ")
                     .append("ORDER BY r.date_operation");

                try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
                    pstmt.setString(1, category);
                    pstmt.setDate(2, Date.valueOf(from_date));
                    pstmt.setDate(3, Date.valueOf(to_date));
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            String date = rs.getDate("date_operation").toString();
                            float total = rs.getFloat("daily_total");
                            int dateIndex = dates.indexOf(date);
                            if (dateIndex != -1) {
                                categoryStats.set(dateIndex, total);
                            }
                        }
                    }
                }
                
                stats.add(categoryStats);
            }
        }
        
        return stats;
    }

    public ArrayList<String> selectAllDates(String from_date, String to_date) throws SQLException {
        logger.info("Selecting all dates between " + from_date + " and " + to_date);
        ArrayList<String> dates = new ArrayList<>();
        
        try (Connection conn = getConnection()) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT DISTINCT date_operation ")
                 .append("FROM ").append(schema).append(".records ")
                 .append("WHERE date_operation >= ? ")
                 .append("AND date_operation <= ? ")
                 .append("ORDER BY date_operation");

            try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
                pstmt.setDate(1, Date.valueOf(from_date));
                pstmt.setDate(2, Date.valueOf(to_date));
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        dates.add(rs.getDate("date_operation").toString());
                    }
                }
            }
        }
        
        return dates;
    }

    public ArrayList<String> selectCategories(String[] category_names) throws SQLException {
        logger.info("Selecting specific categories");
        ArrayList<String> categories = new ArrayList<>();
        
        try (Connection conn = getConnection()) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT category_name FROM ").append(schema).append(".categories ");
            
            if (category_names != null && category_names.length > 0) {
                query.append("WHERE category_name IN (");
                for (int i = 0; i < category_names.length; i++) {
                    query.append("?");
                    if (i < category_names.length - 1) {
                        query.append(",");
                    }
                }
                query.append(")");
            }
            
            query.append(" ORDER BY category_name");

            try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
                if (category_names != null && category_names.length > 0) {
                    for (int i = 0; i < category_names.length; i++) {
                        pstmt.setString(i + 1, category_names[i]);
                    }
                }
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        categories.add(rs.getString("category_name"));
                    }
                }
            }
        }
        
        return categories;
    }
}
