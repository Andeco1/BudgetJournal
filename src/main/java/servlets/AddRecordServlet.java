package servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AddRecordServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AddRecordServlet.class.getName());
    private String url;
    private String user;
    private String password;
    private DatabaseManager dbManager;

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
        
        try {
            dbManager = DatabaseManager.getInstance(url, user, password);
            logger.info("Database manager initialized successfully");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize database manager", e);
            throw new ServletException("Failed to initialize database manager", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String category = request.getParameter("category");
        String date = request.getParameter("date");
        float total = Float.parseFloat(request.getParameter("total"));
        boolean isExpense = request.getParameter("operation") != null && request.getParameter("operation").equals("expense");
        
        logger.info("Received record: category=" + category + ", date=" + date + ", total=" + total + ", isExpense=" + isExpense);
        
        try {
            dbManager.addRecord(category, date, total, isExpense);
            logger.info("Record added successfully");
            response.sendRedirect("main");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding record: " + e.getMessage(), e);
            throw new ServletException("Ошибка при добавлении записи", e);
        }
    }
} 