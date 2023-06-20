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
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/*
 * Controller class for patient interactions.
 *   register as a new patient.
 *   update patient profile.
 */
@Controller
public class ControllerPatient {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*
	 * Request blank patient registration form.
	 */
	@GetMapping("/patient/new")
	public String newPatient(Model model) {
		// return blank form for new patient registration
		model.addAttribute("patient", new Patient());
		return "patient_register";
	}
	
	/*
	 * Process new patient registration	 */
	@PostMapping("/patient/new")
	public String newPatient(Patient p, Model model) {
		String query = "";
		String patientId = "";
		String physicianID = "";
		String specialty = "";
		String firstYearOfPractice = "";
		Date birthDate = new Date();
		LocalDate localDate = LocalDate.now();
		String currDateStr = localDate.toString();
		Date currDate = new Date();
		PreparedStatement ps;
		model.addAttribute("patient", p);
		try {
			currDate = new SimpleDateFormat("yyyy-MM-dd").parse(currDateStr);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		Date minBirthDate = new Date();
		try {
			minBirthDate = new SimpleDateFormat("yyyy-MM-dd").parse("1900-01-01");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		Date maxBirthDate = new Date();
		try {
			maxBirthDate = new SimpleDateFormat("yyyy-MM-dd").parse("2022-12-31");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		String ssn = null;
		char stateChar = 0;
		String zipcode = null;
		int snnInt = 0;
		int stateInt = 0;
		int zip = 0;
/*		if ((p.getSsn().length() != 11) || (p.getSsn().charAt(3) != '-') || (p.getSsn().charAt(6) != '-')){
			model.addAttribute("message", "Enter SSN in format xxx-xx-xxxx.");
			return "patient_register";
		}
		else {
			ssn = p.getSsn().substring(0, 3);
			try {
				snnInt = Integer.parseInt(ssn);
			} catch (NumberFormatException nfe) {
				model.addAttribute("message", "SSN should be entered as numbers.");
				return "patient_register";
			}
			if ((ssn.charAt(0) == '0') || (ssn.charAt(0) == '9')) {
				model.addAttribute("message", "SSN should not begin with 0 or 9.");
				return "patient_register";
			}
			ssn = p.getSsn().substring(4, 6);
			try {
				snnInt = Integer.parseInt(ssn);
			} catch (NumberFormatException nfe) {
				model.addAttribute("message", "SSN should be entered as numbers.");
				return "patient_register";
			}
			if (ssn.equals("00")) {
				model.addAttribute("message", "The two middle digits of SSN cannot be 00.");
				return "patient_register";
			}
			ssn = p.getSsn().substring(7, 11);
			try {
				snnInt = Integer.parseInt(ssn);
			} catch (NumberFormatException nfe) {
				model.addAttribute("message", "SSN should be entered as numbers.");
				return "patient_register";
			}
			if (ssn.equals("0000")) {
				model.addAttribute("message", "The last four digits of SSN cannot be 0000.");
				return "patient_register";
			}
		}*/
		if (p.getSsn().length() != 9){
			model.addAttribute("message", "Enter 9 digits of social security number.");
			return "patient_register";
		}
		else {
			ssn = p.getSsn();
			try {
				snnInt = Integer.parseInt(ssn);
			} catch (NumberFormatException nfe) {
				model.addAttribute("message", "SSN should be entered as numbers.");
				return "patient_register";
			}
			if ((ssn.charAt(0) == '0') || (ssn.charAt(0) == '9')) {
				model.addAttribute("message", "SSN should not begin with 0 or 9.");
				return "patient_register";
			}
			ssn = p.getSsn().substring(3, 5);
			if (ssn.equals("00")) {
				model.addAttribute("message", "The two middle digits of SSN cannot be 00.");
				return "patient_register";
			}
			ssn = p.getSsn().substring(5, 9);
			if (ssn.equals("0000")) {
				model.addAttribute("message", "The last four digits of SSN cannot be 0000.");
				return "patient_register";
			}
		}
		if (p.getFirst_name().length() < 1) {
			model.addAttribute("message", "Enter your first name.");
			return "patient_register";
		}
		if (p.getLast_name().length() < 1) {
			model.addAttribute("message", "Enter your last name.");
			return "patient_register";
		}
		if (p.getBirthdate().length() < 1) {
			model.addAttribute("message", "Enter your birthdate.");
			return "patient_register";
		}
		try {
			birthDate = new SimpleDateFormat("yyyy-MM-dd").parse(p.getBirthdate());
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
/*		if (birthDate.compareTo(currDate) > 0) {
			model.addAttribute("message", "The birthdate cannot be in the future.");
			return "patient_show";
		}*/
		if ((birthDate.compareTo(minBirthDate) < 0) || (birthDate.compareTo(maxBirthDate) > 0)) {
			model.addAttribute("message", "The birthdate should be between 1900 to 2022.");
			return "patient_register";
		}
		if (p.getStreet().length() < 1) {
			model.addAttribute("message", "Enter your street address.");
			return "patient_register";
		}
		if (p.getCity().length() < 1) {
			model.addAttribute("message", "Enter the name of your city.");
			return "patient_register";
		}
		if (p.getState().length() != 2) {
			model.addAttribute("message", "Enter state in two characters.");
			return "patient_register";
		}
		else {
			stateChar = p.getState().charAt(0);
			stateInt = (int)(stateChar);
			if ((stateInt < 65) || (stateInt > 122)) {
				model.addAttribute("message", "State should be entered as two letters.");
				return "patient_register";
			}
			stateChar = p.getState().charAt(1);
			stateInt = (int)(stateChar);
			if ((stateInt < 65) || (stateInt > 122)) {
				model.addAttribute("message", "State should be entered as two letters.");
				return "patient_register";
			}
		}
/*		if (p.getZipcode().length() < 5) {
			model.addAttribute("message", "Zipcode should be entered as 5 or 9 digits.");
			return "patient_register";
		}
		else if (p.getZipcode().length() == 5) {
			zipcode = p.getZipcode().substring(0, 5);
			try {
				zip = Integer.parseInt(zipcode);
			} catch (NumberFormatException nfe) {
				model.addAttribute("message", "Zipcode should be entered as numbers.");
				return "patient_register";
			}
		}
		else if (p.getZipcode().length() > 5) {
			if ((p.getZipcode().charAt(5) != '-') || (p.getZipcode().length() != 10)) {
				model.addAttribute("message", "Enter zipcode in format xxxxx-xxxx.");
				return "patient_register";
			}
			if (p.getZipcode().length() == 10) {
				zipcode = p.getZipcode().substring(6, 10);
				try {
					zip = Integer.parseInt(zipcode);
				} catch (NumberFormatException nfe) {
					model.addAttribute("message", "Zipcode should be entered as numbers.");
					return "patient_register";
				}
			}
		}*/
		if (p.getZipcode().length() < 5) {
			model.addAttribute("message", "Zipcode should be entered as 5 or 9 digits.");
			return "patient_register";
		}
		else if (p.getZipcode().length() == 5) {
			zipcode = p.getZipcode();
			try {
				zip = Integer.parseInt(zipcode);
			} catch (NumberFormatException nfe) {
				model.addAttribute("message", "Zipcode should be entered as numbers.");
				return "patient_register";
			}
		}
		else if (p.getZipcode().length() > 5) {
			if (p.getZipcode().length() != 9) {
				model.addAttribute("message", "Zipcode should be entered as 5 or 9 digits.");
				return "patient_register";
			}
			else {
				zipcode = p.getZipcode();
				try {
					zip = Integer.parseInt(zipcode);
				} catch (NumberFormatException nfe) {
					model.addAttribute("message", "Zipcode should be entered as numbers.");
					return "patient_register";
				}
			}
		}
		if (p.getPrimaryName().length() < 1) {
			model.addAttribute("message", "Enter the name of your primary care.");
			return "patient_register";
		}
		query = "SELECT SSN FROM PATIENT WHERE SSN = ?";
		try {
			Connection con = getConnection();
			try {
				ps = con.prepareStatement(query);
				ps.setString(1, p.getSsn());
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					model.addAttribute("message", "This ssn has already been registered in the database.");
					return "patient_register";
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			if (p.getPrimaryName().length() >= 1) {
				query = "SELECT PhysicianID, Specialty, FirstYearOfPractice FROM PHYSICIAN WHERE CONCAT(FirstName, ' ', LastName) = ?";
				try {
					ps = con.prepareStatement(query);
					ps.setString(1, p.getPrimaryName());
					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
						physicianID = rs.getString("PhysicianID");
						specialty = rs.getString("Specialty");
						firstYearOfPractice = rs.getString("FirstYearOfPractice");
					}
					if (physicianID.length() < 1) {
						model.addAttribute("message", "This primary care has not registered yet in the database.");
						return "patient_register";
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				long diffDates = (currDate.getTime() - birthDate.getTime());
				long diffDays = TimeUnit.DAYS.convert(diffDates, TimeUnit.MILLISECONDS);
				if ((diffDays/365) >= 18) {
					if (!(specialty.equals("Family Medicine") || specialty.equals("Internal Medicine"))) {
						model.addAttribute("message", "The specialty of the primary care is not valid.");
						return "patient_register";
					}
				}
				else if (!(specialty.equals("Pediatrics"))) {
					model.addAttribute("message", "The specialty of the primary care is not valid.");
					return "patient_register";
				}
				p.setPrimaryID(Integer.parseInt(physicianID));
				p.setSpecialty(specialty);
				p.setYears(firstYearOfPractice);
			}
			String insert = "INSERT INTO PATIENT " +
					"(PatientID, FirstName, LastName, BirthDate, SSN, Street, City, State, Zipcode, PhysicianID) " +
					"VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			try {
				ps = con.prepareStatement(insert);
				ps.setString(1, p.getFirst_name());
				ps.setString(2, p.getLast_name());
				ps.setString(3, p.getBirthdate());
				ps.setString(4, p.getSsn());
				ps.setString(5, p.getStreet());
				ps.setString(6, p.getCity());
				ps.setString(7, p.getState());
				ps.setString(8, p.getZipcode());
				ps.setString(9, physicianID);
				int addedRecord = ps.executeUpdate();
				if (addedRecord == 1) {
					model.addAttribute("message", "Registration successful.");
				}
				else {
					model.addAttribute("message", "Registration failed.");
					return "patient_register";
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			query = "SELECT PatientID FROM PATIENT WHERE SSN = ?";
			try {
				ps = con.prepareStatement(query);
				ps.setString(1, p.getSsn());
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					patientId = rs.getString("PatientID");
				}
				p.setPatientId(patientId);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return "patient_show";
	}

	/*
	 * Request blank form to search for patient by patient id
	 */
	@GetMapping("/patient/edit")
	public String getPatientForm(Model model) {
		return "patient_get";
	}

	/*
	 * Perform search for patient by patient id and last name.
	 */
	@PostMapping("/patient/show")
	public String getPatientForm(@RequestParam("patientId") String patientId, @RequestParam("last_name") String last_name,
			Model model) {
		String query = "";
		String ssn = "";
		String first_name = "";
		String birth_date = "";
		String street = "";
		String city = "";
		String state = "";
		String zipcode = "";
		String physicianId = "";
		String physicianName = "";
		String specialty = "";
		String firstYearOfPractice = "";
		PreparedStatement ps;
		Patient p = new Patient();
		model.addAttribute("patient", p);
		if (patientId.length() < 1) {
			model.addAttribute("message", "Please enter a patient id.");
			return "patient_get";
		}
		if (patientId.length() == 1) {
			char pId = patientId.charAt(0);
			int pIdInt = (int)(pId);
			if ((pIdInt < 49) || (pIdInt > 57)) {
				model.addAttribute("message", "Patient id should be an integer.");
				return "patient_get";
			}
		}
		if (patientId.length() > 1) {
			try {
				int pIdInt = Integer.parseInt(patientId);
			} catch (NumberFormatException nfe) {
				model.addAttribute("message", "Patient id should be an integer.");
				return "patient_get";
			}
		}
		if (last_name.length() < 1) {
			model.addAttribute("message", "Please enter last name.");
			return "patient_get";
		}
		query = "SELECT SSN, FirstName, BirthDate, Street, City, State, Zipcode, PhysicianID FROM PATIENT WHERE PatientID = ? AND LastName = ?";
		try {
			Connection con = getConnection();
			try {
				ps = con.prepareStatement(query);
				ps.setString(1, patientId);
				ps.setString(2, last_name);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					ssn = rs.getString("SSN");
					first_name = rs.getString("FirstName");
					birth_date = rs.getString("BirthDate");
					street = rs.getString("Street");
					city = rs.getString("City");
					state = rs.getString("State");
					zipcode = rs.getString("Zipcode");
					physicianId = rs.getString("PhysicianID");
				}
				if (ssn.length() < 1) {
					model.addAttribute("message", "There is no record matched with your entries in the database.");
					return "patient_get";
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			query = "SELECT CONCAT(FirstName, ' ', LastName), Specialty, FirstYearOfPractice FROM PHYSICIAN WHERE PhysicianID = ?";
			try {
				ps = con.prepareStatement(query);
				ps.setString(1, physicianId);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					physicianName = rs.getString(1);
					specialty = rs.getString(2);
					firstYearOfPractice = rs.getString(3);
				}
				if (physicianName.length() < 1) {
					model.addAttribute("message", "The Physician id is not existed in the database.");
					return "patient_get";
				}
			} catch (SQLException e) {
			throw new RuntimeException(e);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		p.setPatientId(patientId);
		p.setSsn(ssn);
		p.setFirst_name(first_name);
		p.setLast_name(last_name);
		p.setBirthdate(birth_date);
		p.setStreet(street);
		p.setCity(city);
		p.setState(state);
		p.setZipcode(zipcode);
		p.setPrimaryID(Integer.parseInt(physicianId));
		p.setPrimaryName(physicianName);
		p.setSpecialty(specialty);
		p.setYears(firstYearOfPractice);
		return "patient_show";
	}

	/*
	 *  Display patient profile for patient id.
	 */
	@GetMapping("/patient/edit/{patientId}")
	public String updatePatient(@PathVariable String patientId, Model model) {
		String ssn = "";
		String first_name = "";
		String last_name = "";
		String birth_date = "";
		String street = "";
		String city = "";
		String state = "";
		String zipcode = "";
		String physicianId = "";
		String physicianName = "";
		String specialty = "";
		String firstYearOfPractice = "";
		PreparedStatement ps;
		Patient p = new Patient();
		model.addAttribute("patient", p);
		String query = "SELECT SSN, FirstName, LastName, BirthDate, Street, City, State, Zipcode, PhysicianID FROM PATIENT WHERE PatientID = ?";
		try {
			Connection con = getConnection();
			try {
				ps = con.prepareStatement(query);
				ps.setString(1, patientId);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					ssn = rs.getString("SSN");
					first_name = rs.getString("FirstName");
					last_name = rs.getString("LastName");
					birth_date = rs.getString("BirthDate");
					street = rs.getString("Street");
					city = rs.getString("City");
					state = rs.getString("State");
					zipcode = rs.getString("Zipcode");
					physicianId = rs.getString("PhysicianID");
				}
				if (ssn.length() < 1) {
					model.addAttribute("message", "There is no record matched with your entries in the database.");
					return "patient_edit";
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			query = "SELECT CONCAT(FirstName, ' ', LastName), Specialty, FirstYearOfPractice FROM PHYSICIAN WHERE PhysicianID = ?";
			try {
				ps = con.prepareStatement(query);
				ps.setString(1, physicianId);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					physicianName = rs.getString(1);
					specialty = rs.getString(2);
					firstYearOfPractice = rs.getString(3);
				}
				if (physicianName.length() < 1) {
					model.addAttribute("message", "The Physician id is not existed in the database.");
					return "patient_edit";
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		p.setPatientId(patientId);
		p.setSsn(ssn);
		p.setFirst_name(first_name);
		p.setLast_name(last_name);
		p.setBirthdate(birth_date);
		p.setStreet(street);
		p.setCity(city);
		p.setState(state);
		p.setZipcode(zipcode);
		p.setPrimaryID(Integer.parseInt(physicianId));
		p.setPrimaryName(physicianName);
		p.setSpecialty(specialty);
		p.setYears(firstYearOfPractice);
		return "patient_edit";
	}

	/*
	 * Process changes to patient profile.  
	 */
	@PostMapping("/patient/edit")
	public String updatePatient(Patient p, Model model) {
		String physicianID = "";
		String specialty = "";
		String firstYearOfPractice = "";
		String query = "";
		Date birthDate = new Date();
		LocalDate localDate = LocalDate.now();
		String currDateStr = localDate.toString();
		Date currDate = new Date();
		PreparedStatement ps;
		model.addAttribute("patient", p);
		try {
			currDate = new SimpleDateFormat("yyyy-MM-dd").parse(currDateStr);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		char stateChar = 0;
		String zipcode = null;
		int stateInt = 0;
		int zip = 0;
		try {
			birthDate = new SimpleDateFormat("yyyy-MM-dd").parse(p.getBirthdate());
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		if (p.getStreet().length() < 1) {
			model.addAttribute("message", "Enter your street address.");
			return "patient_edit";
		}
		if (p.getCity().length() < 1) {
			model.addAttribute("message", "Enter the name of your city.");
			return "patient_edit";
		}
		if (p.getState().length() != 2) {
			model.addAttribute("message", "Enter state in two characters.");
			return "patient_edit";
		}
		else {
			stateChar = p.getState().charAt(0);
			stateInt = (int)(stateChar);
			if ((stateInt < 65) || (stateInt > 122)) {
				model.addAttribute("message", "State should be entered as two letters.");
				return "patient_edit";
			}
			stateChar = p.getState().charAt(1);
			stateInt = (int)(stateChar);
			if ((stateInt < 65) || (stateInt > 122)) {
				model.addAttribute("message", "State should be entered as two letters.");
				return "patient_edit";
			}
		}
		if (p.getZipcode().length() < 5) {
			model.addAttribute("message", "Zipcode should be entered as 5 or 9 digits.");
			return "patient_edit";
		}
		else if (p.getZipcode().length() == 5) {
			zipcode = p.getZipcode();
			try {
				zip = Integer.parseInt(zipcode);
			} catch (NumberFormatException nfe) {
				model.addAttribute("message", "Zipcode should be entered as numbers.");
				return "patient_edit";
			}
		}
		else if (p.getZipcode().length() > 5) {
			if (p.getZipcode().length() != 9) {
				model.addAttribute("message", "Zipcode should be entered as 5 or 9 digits.");
				return "patient_edit";
			}
			else {
				zipcode = p.getZipcode();
				try {
					zip = Integer.parseInt(zipcode);
				} catch (NumberFormatException nfe) {
					model.addAttribute("message", "Zipcode should be entered as numbers.");
					return "patient_edit";
				}
			}
		}
		if (p.getPrimaryName().length() < 1) {
			model.addAttribute("message", "Enter the name of your primary care.");
			return "patient_edit";
		}
		try {
			Connection conn = getConnection();
			if (p.getPrimaryName().length() >= 1) {
				query = "SELECT PhysicianID, Specialty, FirstYearOfPractice FROM PHYSICIAN WHERE CONCAT(FirstName, ' ', LastName) = ?";
				try {
					ps = conn.prepareStatement(query);
					ps.setString(1, p.getPrimaryName());
					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
						physicianID = rs.getString("PhysicianID");
						specialty = rs.getString("Specialty");
						firstYearOfPractice = rs.getString("FirstYearOfPractice");
					}
					if (physicianID.length() < 1) {
						model.addAttribute("message", "This primary care has not registered yet in the database.");
						return "patient_edit";
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				long diffDates = (currDate.getTime() - birthDate.getTime());
				long diffDays = TimeUnit.DAYS.convert(diffDates, TimeUnit.MILLISECONDS);
				if ((diffDays / 365) >= 18) {
					if (!(specialty.equals("Family Medicine") || specialty.equals("Internal Medicine"))) {
						model.addAttribute("message", "The specialty of the primary care is not valid.");
						return "patient_edit";
					}
				} else if (!(specialty.equals("Pediatrics"))) {
					model.addAttribute("message", "The specialty of the primary care is not valid.");
					return "patient_edit";
				}
				p.setPrimaryID(Integer.parseInt(physicianID));
				p.setSpecialty(specialty);
				p.setYears(firstYearOfPractice);
			}
			query = "UPDATE PATIENT SET Street = ?, City = ?, State = ?, Zipcode = ?, PhysicianID = ? WHERE PatientID = ?";
			try {
				ps = conn.prepareStatement(query);
				ps.setString(1, p.getStreet());
				ps.setString(2, p.getCity());
				ps.setString(3, p.getState());
				ps.setString(4, p.getZipcode());
				ps.setInt(5, p.getPrimaryID());
				ps.setString(6, p.getPatientId());
				int updatedRow = ps.executeUpdate();
				if (updatedRow < 1) {
					model.addAttribute("message", "Update filed.");
					return "patient_edit";
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		model.addAttribute("message", "The profile updated successfully.");
		return "patient_show";
	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */
	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}
