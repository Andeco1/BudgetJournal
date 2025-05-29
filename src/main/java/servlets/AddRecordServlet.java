package servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class AddRecordServlet extends HttpServlet {
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
            throw new ServletException("Не найдена конфигурация базы данных в переменных окружения");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // Get form data
            String categoryName = request.getParameter("category");
            String date = request.getParameter("date");
            float total = Float.parseFloat(request.getParameter("total"));
            boolean isExpense = request.getParameter("operation").equals("expense");

            // Get database connection
            dbManager = DatabaseManager.getInstance(url, user, password);

            // Add record to database
            dbManager.addRecord(categoryName, date, total, isExpense);

            // Redirect back to main page
            response.sendRedirect(request.getContextPath() + "/");
        } catch (SQLException e) {
            throw new ServletException("Ошибка при добавлении записи", e);
        } catch (NumberFormatException e) {
            throw new ServletException("Неверный формат суммы", e);
        }
    }
} 