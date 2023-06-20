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
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
public class ControllerQuantityOfFills {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/quantity_report/new")
    public String newReport(Model model) {
        // return blank form for new report
        model.addAttribute("filled_quantity", new QuantityOfFills());
        return "quantity_of_fills";
    }
    @PostMapping("/quantity_report/new")
    public String newReport(QuantityOfFills q, Model model) {
        String drugName = "";
        String quantity = "";
        String datePart = "";
        int datePartInt;
        int pharmacyIdInt;
        List <QuantityOfFills> qList = new ArrayList<>();
        model.addAttribute("filled_quantity", q);
        if (q.getPharmacyId().length() < 1) {
            model.addAttribute("message", "Enter a Pharmacy ID.");
            return "quantity_of_fills";
        }
        if (q.getPharmacyId().length() > 1) {
            try {
                pharmacyIdInt = Integer.parseInt(q.getPharmacyId());
            } catch (NumberFormatException nfe) {
                model.addAttribute("message", "Pharmacy ID should be entered numerically.");
                return "quantity_of_fills";
            }
        }
        if (q.getPharmacyId().length() == 1) {
            pharmacyIdInt = (int)(q.getPharmacyId().charAt(0));
            if ((pharmacyIdInt < 49) || (pharmacyIdInt > 57)) {
                model.addAttribute("message", "Pharmacy ID should be entered numerically.");
                return "quantity_of_fills";
            }
        }
        if ((q.getStartDate().length() != 10) || (q.getStartDate().charAt(4) != '-') || (q.getStartDate().charAt(7) != '-')) {
            model.addAttribute("message", "Enter a start date in yyyy-MM-dd format.");
            return "quantity_of_fills";
        }
        datePart = q.getStartDate().substring(0, 4);
        try {
            datePartInt = Integer.parseInt(datePart);
        } catch (NumberFormatException nfe) {
            model.addAttribute("message", "Start date should be entered numerically.");
            return "quantity_of_fills";
        }
        datePart = q.getStartDate().substring(5, 7);
        try {
            datePartInt = Integer.parseInt(datePart);
        } catch (NumberFormatException nfe) {
            model.addAttribute("message", "Start date should be entered numerically.");
            return "quantity_of_fills";
        }
        datePart = q.getStartDate().substring(8, 10);
        try {
            datePartInt = Integer.parseInt(datePart);
        } catch (NumberFormatException nfe) {
            model.addAttribute("message", "Start date should be entered numerically.");
            return "quantity_of_fills";
        }
        if ((q.getEndDate().length() != 10) || (q.getEndDate().charAt(4) != '-') || (q.getEndDate().charAt(7) != '-')) {
            model.addAttribute("message", "Enter an end date in yyyy-MM-dd format.");
            return "quantity_of_fills";
        }
        datePart = q.getEndDate().substring(0, 4);
        try {
            datePartInt = Integer.parseInt(datePart);
        } catch (NumberFormatException nfe) {
            model.addAttribute("message", "End date should be entered numerically.");
            return "quantity_of_fills";
        }
        datePart = q.getEndDate().substring(5, 7);
        try {
            datePartInt = Integer.parseInt(datePart);
        } catch (NumberFormatException nfe) {
            model.addAttribute("message", "End date should be entered numerically.");
            return "quantity_of_fills";
        }
        datePart = q.getEndDate().substring(8, 10);
        try {
            datePartInt = Integer.parseInt(datePart);
        } catch (NumberFormatException nfe) {
            model.addAttribute("message", "End date should be entered numerically.");
            return "quantity_of_fills";
        }
        try {
            int rowNum = 0;
            Connection conn = getConnection();
            String query = "SELECT D.Trade_Name, SUM(S.Quantity) FROM PHARMACY P, PRESCRIPTION S, DRUG D " +
                    "WHERE P.PharmacyID = ? AND P.PharmacyID = S.PharmacyID AND S.Drug_ID = D.Drug_ID " +
                    "AND PrescriptionFillDate >= ? AND PrescriptionFillDate <= ? " +
                    "GROUP BY D.Drug_ID ORDER BY D.Trade_Name ASC";
            try {
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, q.getPharmacyId());
                ps.setString(2, q.getStartDate());
                ps.setString(3, q.getEndDate());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    drugName = rs.getString(1);
                    quantity = rs.getString(2);
                    q.setTradeName(drugName);
                    q.setQuantity(quantity);
                    q.setNo(Integer.toString(++rowNum));
                    QuantityOfFills qTemp = new QuantityOfFills();
                    qTemp.setPharmacyId(q.getPharmacyId());
                    qTemp.setStartDate(q.getStartDate());
                    qTemp.setEndDate(q.getEndDate());
                    qTemp.setTradeName(q.getTradeName());
                    qTemp.setQuantity(q.getQuantity());
                    qTemp.setNo(q.getNo());
                    qList.add(qTemp);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (qList.size() < 1) {
            model.addAttribute("message", "There is no record.");
            return "quantity_of_fills";
        }
        model.addAttribute("filled_quantity", qList);
        return "quantity_of_fills_show";
    }
    private Connection getConnection () throws SQLException {
        Connection conn = jdbcTemplate.getDataSource().getConnection();
        return conn;
    }
}