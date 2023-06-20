package com.csumb.cst363;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/*
drug name + start date + end date ---> doctor + quantity

An FDA government official is looking for the quantity of drugs that each doctor has prescribed.
The report shows the doctor’s name and quantity prescribed.
Input is drug name (may be partial name) and a start and end date range.
 */

@Controller
public class ControllerQuantityOfPrescribed {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Connection getConnection () throws SQLException {
        Connection conn = jdbcTemplate.getDataSource().getConnection();
        return conn;
    }

    @GetMapping("/prescribed_report/new")
    public String prescribeReport(Model model){
        //blank model
        model.addAttribute("prescribed_quantity", new QuantityOfPrescribed());
        return "quantity_of_prescribed";
    }
    @PostMapping("/prescribed_report/new")
    public String prescribeReport(QuantityOfPrescribed q, Model model){
        // not-blank model
        String physicianName = "";
        String quantity = "";
        String datePart = "";
        int datePartInt = 0;
        List <QuantityOfPrescribed> qList = new ArrayList<>();
        //acquire user input
        model.addAttribute("prescribed_quantity", q);

        //if there's no entered name
        if (q.getDrugName().length() < 1) {
            model.addAttribute("message", "Enter a drug name.");
            return "quantity_of_prescribed";
        }

        //verifies date input
        if ((q.getStartDate().length() != 10) || (q.getStartDate().charAt(4) != '-') || (q.getStartDate().charAt(7) != '-')) {
            model.addAttribute("message", "Enter a start date in yyyy-MM-dd format.");
            return "quantity_of_prescribed";
        }
        datePart = q.getStartDate().substring(0, 4);
        try {
            datePartInt = Integer.parseInt(datePart);
        } catch (NumberFormatException nfe) {
            model.addAttribute("message", "Start date should be entered numerically.");
            return "quantity_of_prescribed";
        }
        datePart = q.getStartDate().substring(5, 7);
        try {
            datePartInt = Integer.parseInt(datePart);
        } catch (NumberFormatException nfe) {
            model.addAttribute("message", "Start date should be entered numerically.");
            return "quantity_of_prescribed";
        }
        datePart = q.getStartDate().substring(8, 10);
        try {
            datePartInt = Integer.parseInt(datePart);
        } catch (NumberFormatException nfe) {
            model.addAttribute("message", "Start date should be entered numerically.");
            return "quantity_of_prescribed";
        }
        if ((q.getEndDate().length() != 10) || (q.getEndDate().charAt(4) != '-') || (q.getEndDate().charAt(7) != '-')) {
            model.addAttribute("message", "Enter an end date in yyyy-MM-dd format.");
            return "quantity_of_prescribed";
        }
        datePart = q.getEndDate().substring(0, 4);
        try {
            datePartInt = Integer.parseInt(datePart);
        } catch (NumberFormatException nfe) {
            model.addAttribute("message", "End date should be entered numerically.");
            return "quantity_of_prescribed";
        }
        datePart = q.getEndDate().substring(5, 7);
        try {
            datePartInt = Integer.parseInt(datePart);
        } catch (NumberFormatException nfe) {
            model.addAttribute("message", "End date should be entered numerically.");
            return "quantity_of_prescribed";
        }
        datePart = q.getEndDate().substring(8, 10);
        try {
            datePartInt = Integer.parseInt(datePart);
        } catch (NumberFormatException nfe) {
            model.addAttribute("message", "End date should be entered numerically.");
            return "quantity_of_prescribed";
        }


        //try/catch for connection
        try {
            int rowNum = 0;
            Connection conn = getConnection();
            /*
            take user's input for drug name and start/end date
            idk how to have the input drug name be partial
            the syntax for that in sql is:
                AND drugname LIKE '%?%'
            @todo: find String query syntax for "and DrugName LIKE '%?%'"
             */
            String query = "select d.firstName, d.LastName, SUM(p.quantity)\n" +
                    "from Physician d, Prescription p\n" +
                    "where d.PhysicianID=p.PhysicianID\n" +
                    "and DrugName LIKE ?\n" +
                    "and p.PrescriptionDate BETWEEN ? and ? GROUP BY p.PhysicianID";
            //try/catch for adding SQL table values
            try {
                //connect to database
                PreparedStatement ps = conn.prepareStatement(query);
                //fill String query's ?'s
                ps.setString(1, q.getDrugName());
                ps.setString(2, q.getStartDate());
                ps.setString(3, q.getEndDate());
                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    //combine physician first and last names
                    physicianName = rs.getString(1) + " " + rs.getString(2);
                    quantity = rs.getString(3);
                    q.setPhysicianName(physicianName);
                    q.setQuantity(quantity);
                    q.setNo(Integer.toString(++rowNum));
                    QuantityOfPrescribed qTemp = new QuantityOfPrescribed();
                    qTemp.setDrugName(q.getDrugName());
                    qTemp.setStartDate(q.getStartDate());
                    qTemp.setEndDate(q.getEndDate());
                    qTemp.setQuantity(q.getQuantity());
                    qTemp.setNo(q.getNo());
                    qTemp.setPhysicianName(q.getPhysicianName());
                    qList.add(qTemp);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //verify list is not empty
        if(qList.size() < 1){
            model.addAttribute("message", "There is no record.");
            return "quantity_of_prescribed";
        }
        model.addAttribute("prescribed_quantity", qList);
        return "quantity_of_prescribed_show";
    }
}
