package servlets;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class MainPageServlet extends HttpServlet {


    String url = "jdbc:postgresql://localhost:5432/Records?currentSchema=budget_journal";
    String user = "postgres";
    String password = "postgres";
    private DatabaseManager dbManager;
    private ArrayList<Record> records;
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
        if(category_name == null){
            try {
                category_name = dbManager.selectAllCategories().toArray(new String[0]);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            };
        }
        String operationType = req.getParameter("operation");
        ArrayList<Integer> statistics_pie_chart;
        ArrayList<ArrayList<Float>> statistics_line_chart;
        ArrayList<Integer> categories_id = new ArrayList<>();
        ArrayList<String> dates_operation;
        ArrayList<String> categories;
        try {
            Class.forName("org.postgresql.Driver");
            dbManager = DatabaseManager.getInstance(url,user,password);
            records = DatabaseManager.toArrayList(dbManager.selectRecords(fromDate,toDate,category_name,operationType));
            statistics_pie_chart = dbManager.getPercentage(fromDate,toDate,category_name,operationType);
            statistics_line_chart = dbManager.getStatistics(fromDate,toDate,category_name);
            dates_operation = dbManager.selectAllDates(fromDate,toDate);
            categories = dbManager.selectCategories(category_name);
            for(int i = 0; i< categories.size();i++){
                categories_id.add(i);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        req.setAttribute("records", records);
        req.setAttribute("categories", categories_names);
        req.setAttribute("statistics_categories_names", Arrays.asList(category_name));
        req.setAttribute("statistics_percentage", statistics_pie_chart);
        req.setAttribute("categoryData",statistics_line_chart);
        req.setAttribute("categories_id", categories_id);
        req.setAttribute("statistics_dates", dates_operation);
        req.setAttribute("categories_names", categories);



        RequestDispatcher dispatcher = req.getRequestDispatcher("/show.jsp");
        dispatcher.forward(req, resp);


    }
}
