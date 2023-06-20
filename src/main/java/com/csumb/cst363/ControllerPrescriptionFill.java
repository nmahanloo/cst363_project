package com.csumb.cst363;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller   
public class ControllerPrescriptionFill {

	@Autowired
	private JdbcTemplate jdbcTemplate;


	/* 
	 * Patient requests form to search for prescription.
	 */
	@GetMapping("/prescription/fill")
	public String getfillForm(Model model) {
		model.addAttribute("prescription", new Prescription());
		return "prescription_fill";
	}


	/*
	 * Process the prescription fill request from a patient.
	 * 1.  Validate that Prescription p contains rxid, pharmacy name and pharmacy address
	 *     and uniquely identify a prescription and a pharmacy.
	 * 2.  update prescription with pharmacyid, name and address.
	 * 3.  update prescription with today's date.
	 * 4.  Display updated prescription 
	 * 5.  or if there is an error show the form with an error message.
	 */
	@PostMapping("/prescription/fill")
	public String processFillForm(Prescription p,  Model model) {
		String rx = "";
		if (p.getRxid().length() < 1) {
			model.addAttribute("message", "Enter a Rx number.");
			return "prescription_fill";
		}
		else {
			rx = p.getRxid();
			if (p.getRxid().length() > 1) {
				try {
					int rxInt = Integer.parseInt(rx);
				} catch (NumberFormatException nfe) {
					model.addAttribute("message", "Rx should be entered as numbers.");
					return "prescription_fill";
				}
			}
			else if (p.getRxid().length() == 1) {
				char rxChar = p.getRxid().charAt(0);
				int rxInt = (int)(rxChar);
				if ((rxInt < 0) || (rxInt > 9)) {
					model.addAttribute("message", "Rx should be entered as numbers.");
					return "prescription_fill";
				}
			}
		}
		if (p.getPatientLastName().length() < 1) {
			model.addAttribute("message", "Enter last name.");
			return "prescription_fill";
		}
		if (p.getPharmacyName().length() < 1) {
			model.addAttribute("message", "Enter pharmacy name.");
			return "prescription_fill";
		}
		if (p.getPharmacyAddress().length() < 1) {
			model.addAttribute("message", "Enter pharmacy address.");
			return "prescription_fill";
		}
		try {
			Connection connection = getConnection();
			String query = "SELECT P.PatientID FROM PRESCRIPTION P, PATIENT T " +
					"WHERE P.PatientID = T.PatientID AND T.LastName = ? AND RxID = ?";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setString(1, p.getPatientLastName());
			ps.setString(2, p.getRxid());
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				model.addAttribute("message", "Record not matched.");
				return "prescription_fill";
			}
			query = "SELECT PharmacyID FROM PHARMACY WHERE Name = ? AND Address = ?";
			ps = connection.prepareStatement(query);
			ps.setString(1, p.getPharmacyName());
			ps.setString(2, p.getPharmacyAddress());
			rs = ps.executeQuery();
			if (!rs.next()) {
				model.addAttribute("message", "Pharmacy not found.");
				return "prescription_fill";
			}
//			PreparedStatement ps = connection.prepareStatement("insert into prescription(patientid, drug_id, physicianid, pharmacyid, drugname, quantity, rxprice, prescriptiondate, prescriptionfilldate) values " +
//							"((select PatientID from PATIENT where SSN=?), (select Drug_ID from DRUG where Trade_Name = ? or Formula = ? limit 1), (select PhysicianID from PHYSICIAN where SSN = ?), (select PharmacyID from PHARMACY where Name = ?),?, ?, ?, ?, ?)",
			ps = connection.prepareStatement("Update PRESCRIPTION set PharmacyID = (select PharmacyID from PHARMACY where name = ? and Address=?), PrescriptionFillDate = now() where RxID = ? and PatientID in (select PatientID from PATIENT where lastname = ?)",

					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, p.getPharmacyName());
			ps.setString(2, p.getPharmacyAddress());
			ps.setString(3, p.getRxid());
			ps.setString(4, p.getPatientLastName());

			ps.execute();

			PreparedStatement ps2 = connection.prepareStatement("select doctor.FirstName, " +
					"doctor.LastName, " +
					"p.FirstName, " +
					"p.LastName, " +
					"Quantity, " +
					"pharm.Phone, PrescriptionFillDate, RxPrice, DrugName " +
					"from PRESCRIPTION " +
					"inner join DRUG D on PRESCRIPTION.Drug_ID = D.Drug_ID " +
					"inner join PATIENT P on PRESCRIPTION.PatientID = P.PatientID " +
					"inner join PHARMACY pharm on PRESCRIPTION.PharmacyID = pharm.PharmacyID " +
					"inner join PHYSICIAN doctor on PRESCRIPTION.PhysicianID = doctor.PhysicianID where RxID=?");

			ps2.setInt(1, Integer.parseInt(p.getRxid()));
			rs = ps2.executeQuery();

			while (rs.next()){
				p.setDoctorFirstName(rs.getString(1));
				p.setDoctorLastName(rs.getString(2));
				p.setPatientFirstName(rs.getString(3));
				p.setPatientLastName(rs.getString(4));
				p.setQuantity(rs.getInt(5));
				p.setPharmacyPhone(rs.getString(6));
				p.setDateFilled(rs.getString(7));
				p.setCost(rs.getString(8));
				p.setDrugName(rs.getString(9));
			}


		} catch (SQLException e) {
			throw new RuntimeException(e);
		}


		// TODO

		// temporary code to set fake data for now.
//		p.setPharmacyID("70012345");
//		p.setCost(String.format("%.2f", 12.5
//));
//		p.setDateFilled( new java.util.Date().toString() );

		// display the updated prescription

		model.addAttribute("message", "Prescription has been filled.");
		model.addAttribute("prescription", p);
		return "prescription_show";

	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}