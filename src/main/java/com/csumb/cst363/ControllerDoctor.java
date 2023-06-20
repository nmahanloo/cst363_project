package com.csumb.cst363;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.InputMismatchException;

import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/*
 * Controller class for doctor registration and profile update.
 */
@Controller
public class ControllerDoctor {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/*
	 * Request for new doctor registration form.
	 */
	@GetMapping("/doctor/register")
	public String newDoctor(Model model) {
		// return blank form for new patient registration
		model.addAttribute("doctor", new Doctor());
		return "doctor_register";
	}

	/*
	 * Process doctor registration.
	 */
	@PostMapping("/doctor/register")
	public String createDoctor(Doctor doctor, Model model) {
		String ssn = "";
		if (doctor.getSsn().length() != 9) {
			model.addAttribute("message", "Enter 9 digits of social security number.");
			return "doctor_register";
		}
		else {
			ssn = doctor.getSsn();
			try {
				int snnInt = Integer.parseInt(ssn);
			} catch (NumberFormatException nfe) {
				model.addAttribute("message", "SSN should be entered as numbers.");
				return "doctor_register";
			}
			if ((ssn.charAt(0) == '0') || (ssn.charAt(0) == '9')) {
				model.addAttribute("message", "SSN should not begin with 0 or 9.");
				return "doctor_register";
			}
			ssn = doctor.getSsn().substring(3, 5);
			if (ssn.equals("00")) {
				model.addAttribute("message", "The two middle digits of SSN cannot be 00.");
				return "doctor_register";
			}
			ssn = doctor.getSsn().substring(5, 9);
			if (ssn.equals("0000")) {
				model.addAttribute("message", "The last four digits of SSN cannot be 0000.");
				return "doctor_register";
			}
		}
		if (doctor.getFirst_name().length() < 1) {
			model.addAttribute("message", "Enter first name.");
			return "doctor_register";
		}
		if (doctor.getLast_name().length() < 1) {
			model.addAttribute("message", "Enter last name.");
			return "doctor_register";
		}
		if (doctor.getSpecialty().length() < 1) {
			model.addAttribute("message", "Enter specialty.");
			return "doctor_register";
		}
		if (doctor.getPractice_since_year().length() < 1) {
			model.addAttribute("message", "Enter a year.");
			return "doctor_register";
		}
		else if (doctor.getPractice_since_year().length() != 4) {
			model.addAttribute("message", "Enter a year in YYYY format.");
			return "doctor_register";
		}
		else {
			LocalDate localDate = LocalDate.now();
			int currYear = localDate.getYear();
			int yearInt;
			try {
				yearInt = Integer.parseInt(doctor.getPractice_since_year());
			} catch (NumberFormatException nfe) {
				model.addAttribute("message", "Year should be entered as numbers.");
				return "doctor_register";
			}
			if ((yearInt < 1901) || (yearInt > currYear)) {
				model.addAttribute("message", "Enter a valid year greater than 1900 until present.");
				return "doctor_register";
			}
		}
		//practice_since -> FirstYearOfPractice
		try (Connection con = getConnection();) {
			String query = "SELECT PhysicianID FROM PHYSICIAN WHERE SSN = ?";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, doctor.getSsn());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				model.addAttribute("message", "A doctor has already been registered with this ssn.");
				return "doctor_register";
			}
			ps = con.prepareStatement("insert into physician(LastName, FirstName, Specialty, FirstYearOfPractice,  SSN ) values(?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, doctor.getLast_name());
			ps.setString(2, doctor.getFirst_name());
			ps.setString(3, doctor.getSpecialty());
			ps.setString(4, doctor.getPractice_since_year());
			ps.setString(5, doctor.getSsn());

			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) doctor.setId((int)rs.getLong(1));

			// display message and patient information
			model.addAttribute("message", "Registration successful.");
			model.addAttribute("doctor", doctor);
			return "doctor_show";

		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_register";
		}
	}

	/*
	 * Request blank form for doctor search.
	 */
	@GetMapping("/doctor/get")
	public String getDoctor(Model model) {
		// return form to enter doctor id and name
		model.addAttribute("doctor", new Doctor());
		return "doctor_get";
	}

	/*
	 * Search for doctor by id and name.
	 */
	@PostMapping("/doctor/get")
	public String getDoctor(Doctor doctor, Model model) {
		if (doctor.getId() < 1) {
			model.addAttribute("message", "Enter ID.");
			return "doctor_get";
		}
		if (doctor.getLast_name().length() < 1) {
			model.addAttribute("message", "Enter last name.");
			return "doctor_get";
		}
		try (Connection con = getConnection();) {
			// for DEBUG
			System.out.println("start getDoctor "+doctor);
			PreparedStatement ps = con.prepareStatement("select LastName, FirstName, Specialty, FirstYearOfPractice from physician where PhysicianID=? and LastName=?");
			ps.setInt(1, doctor.getId());
			ps.setString(2, doctor.getLast_name());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				doctor.setLast_name(rs.getString(1));
				doctor.setFirst_name(rs.getString(2));
				doctor.setPractice_since_year(rs.getString(4));
				doctor.setSpecialty(rs.getString(3));
				model.addAttribute("doctor", doctor);
				// for DEBUG
				System.out.println("end getDoctor "+doctor);
				return "doctor_show";

			} else {
				model.addAttribute("message", "Doctor not found.");
				return "doctor_get";
			}

		} catch (SQLException e) {
			System.out.println("SQL error in getDoctor "+e.getMessage());
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_get";
		}
	}

	/*
	 * search for doctor by id.
	 */
	@GetMapping("/doctor/edit/{id}")
	public String getDoctor(@PathVariable int id, Model model) {
		Doctor doctor = new Doctor();
		doctor.setId(id);
		try (Connection con = getConnection();) {

			PreparedStatement ps = con.prepareStatement("select LastName, FirstName, Specialty, FirstYearOfPractice from physician where PhysicianID=?");
			ps.setInt(1,  id);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				doctor.setLast_name(rs.getString(1));
				doctor.setFirst_name(rs.getString(2));
				doctor.setPractice_since_year(rs.getString(4));
				doctor.setSpecialty(rs.getString(3));
				model.addAttribute("doctor", doctor);
				return "doctor_edit";
			} else {
				model.addAttribute("message", "Doctor not found.");
				model.addAttribute("doctor", doctor);
				return "doctor_get";
			}

		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_get";

		}
	}

	/*
	 * process profile update for doctor.  Change specialty or year of practice.
	 */
	@PostMapping("/doctor/edit")
	public String updateDoctor(Doctor doctor, Model model) {
		if (doctor.getSpecialty().length() < 1) {
			model.addAttribute("message", "Enter specialty.");
			return "doctor_edit";
		}
		if (doctor.getPractice_since_year().length() < 1) {
			model.addAttribute("message", "Enter a year.");
			return "doctor_edit";
		}
		else if (doctor.getPractice_since_year().length() != 4) {
			model.addAttribute("message", "Enter a year in YYYY format.");
			return "doctor_edit";
		}
		else {
			LocalDate localDate = LocalDate.now();
			int currYear = localDate.getYear();
			int yearInt;
			try {
				yearInt = Integer.parseInt(doctor.getPractice_since_year());
			} catch (NumberFormatException nfe) {
				model.addAttribute("message", "Year should be entered as numbers.");
				return "doctor_edit";
			}
			if ((yearInt < 1901) || (yearInt > currYear)) {
				model.addAttribute("message", "Enter a valid year greater than 1900 until present.");
				return "doctor_edit";
			}
		}
		try (Connection con = getConnection();) {

			PreparedStatement ps = con.prepareStatement("update physician set Specialty=?, FirstYearOfPractice=? where PhysicianID=?");
			ps.setString(1,  doctor.getSpecialty());
			ps.setString(2, doctor.getPractice_since_year());
			ps.setInt(3,  doctor.getId());

			int rc = ps.executeUpdate();
			if (rc==1) {
				model.addAttribute("message", "Update successful");
				model.addAttribute("doctor", doctor);
				return "doctor_show";

			}else {
				model.addAttribute("message", "Error. Update was not successful");
				model.addAttribute("doctor", doctor);
				return "doctor_edit";
			}

		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("doctor", doctor);
			return "doctor_edit";
		}
	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */
	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}
}
