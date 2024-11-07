package servlets;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MainPageServlet extends HttpServlet {


    String url = "jdbc:postgresql://localhost:5432/Records";
    String user = "postgres";
    String password = "@Ynik2005";
    private DatabaseManager dbManager;
    private ArrayList<Record> records = new ArrayList<Record>();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        ArrayList<Integer> cats = new ArrayList<>();
        cats.add(9);cats.add(4);
        try {
            Class.forName("org.postgresql.Driver");
            dbManager = DatabaseManager.getInstance(url,user,password);
            records = DatabaseManager.toArrayList(dbManager.selectByCategory(cats));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        request.setAttribute("users", records);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/show.jsp");
        dispatcher.forward(request, response);
    }
}
