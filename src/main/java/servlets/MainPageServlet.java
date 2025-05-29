package servlets;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MainPageServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(MainPageServlet.class.getName());
    private String url;
    private String user;
    private String password;
    private DatabaseManager dbManager;
    private ArrayList<Record> records;
    private ArrayList<String> categories_names;

    @Override
    public void init() throws ServletException {
        super.init();
        url = System.getenv("DB_URL");
        user = System.getenv("DB_USER");
        password = System.getenv("DB_PASSWORD");
        
        if (url == null || user == null || password == null) {
            logger.severe("Database configuration not found in environment variables");
            throw new ServletException("Database configuration not found in environment variables");
        }
        
        logger.info("Database configuration loaded successfully");
        logger.info("DB_URL: " + url);
        logger.info("DB_USER: " + user);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        logger.info("Processing GET request");

        try {
            dbManager = DatabaseManager.getInstance(url, user, password);
            categories_names = dbManager.selectAllCategories();
            logger.info("Categories loaded successfully. Count: " + categories_names.size());
            
            if (categories_names.isEmpty()) {
                logger.warning("No categories found in the database");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while loading categories: " + e.getMessage(), e);
            throw new ServletException("Database error while loading categories", e);
        }

        req.setAttribute("categories", categories_names);
        RequestDispatcher dispatcher = req.getRequestDispatcher("/show.jsp");
        dispatcher.forward(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("Processing POST request");
        String fromDate = req.getParameter("from");
        String toDate = req.getParameter("to");
        String[] category_name = req.getParameterValues("categories");
        
        try {
            if (category_name == null) {
                logger.info("No categories selected, loading all categories");
                category_name = dbManager.selectAllCategories().toArray(new String[0]);
            }
            
            String operationType = req.getParameter("operation");
            ArrayList<Integer> statistics_pie_chart;
            ArrayList<ArrayList<Float>> statistics_line_chart;
            ArrayList<Integer> categories_id = new ArrayList<>();
            ArrayList<String> dates_operation;
            ArrayList<String> categories;

            dbManager = DatabaseManager.getInstance(url, user, password);
            records = DatabaseManager.toArrayList(dbManager.selectRecords(fromDate, toDate, category_name, operationType));
            statistics_pie_chart = dbManager.getPercentage(fromDate, toDate, category_name, operationType);
            statistics_line_chart = dbManager.getStatistics(fromDate, toDate, category_name);
            dates_operation = dbManager.selectAllDates(fromDate, toDate);
            categories = dbManager.selectCategories(category_name);
            
            for (int i = 0; i < categories.size(); i++) {
                categories_id.add(i);
            }

            req.setAttribute("records", records);
            req.setAttribute("categories", categories_names);
            req.setAttribute("statistics_categories_names", Arrays.asList(category_name));
            req.setAttribute("statistics_percentage", statistics_pie_chart);
            req.setAttribute("categoryData", statistics_line_chart);
            req.setAttribute("categories_id", categories_id);
            req.setAttribute("statistics_dates", dates_operation);
            req.setAttribute("categories_names", categories);

            RequestDispatcher dispatcher = req.getRequestDispatcher("/show.jsp");
            dispatcher.forward(req, resp);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error in POST request: " + e.getMessage(), e);
            throw new ServletException("Database error", e);
        }
    }
}
