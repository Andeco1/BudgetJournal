package servlets;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MainPageServlet extends HttpServlet {


    String url = "jdbc:postgresql://localhost:5432/Records";
    String user = "postgres";
    String password = "@Ynik2005";
    private DatabaseManager dbManager;
    private ArrayList<Record> records = new ArrayList<Record>();
    private  ArrayList<String> categories_names;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        try {
            Class.forName("org.postgresql.Driver");
            dbManager = DatabaseManager.getInstance(url,user,password);
            categories_names = dbManager.selectAllCategories();
        } catch (SQLException | ClassNotFoundException e) {
           throw new RuntimeException(e);
        }
//
        req.setAttribute("categories", categories_names);
        RequestDispatcher dispatcher = req.getRequestDispatcher("/show.jsp");
        dispatcher.forward(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fromDate = req.getParameter("from");
        String toDate = req.getParameter("to");
        String[] category_name = req.getParameterValues("categories");
        String operaionType = req.getParameter("operation");


        try {
            Class.forName("org.postgresql.Driver");
            dbManager = DatabaseManager.getInstance(url,user,password);
            records = DatabaseManager.toArrayList(dbManager.selectRecords(fromDate,toDate,category_name,operaionType));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        req.setAttribute("users", records);
        req.setAttribute("categories", categories_names);
        RequestDispatcher dispatcher = req.getRequestDispatcher("/show.jsp");
        dispatcher.forward(req, resp);


    }
}
