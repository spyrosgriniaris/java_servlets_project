package ergasia;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import javax.naming.InitialContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
@WebServlet("/PatientServlet")
public class PatientServlet extends HttpServlet{

	public PatientServlet() {
		// TODO Auto-generated constructor stub
	}
	
	public void Logout(HttpServletRequest request,HttpServletResponse response,PrintWriter out) throws ServletException, IOException
	{
		HttpSession session = request.getSession();
		synchronized(session)
		{
			session.invalidate();
			RequestDispatcher view = request.getRequestDispatcher("index.html");
			view.forward(request, response);
			
		}
	}
	
	public void setAppFromSpecialty(HttpServletRequest request,HttpServletResponse response,PrintWriter out,int specialty) throws ServletException, IOException
	{
		HttpSession session = request.getSession();
		synchronized(session)
		{
			try
			{
				Connection con = datasource.getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM availiability WHERE specialty = '"+ specialty+"'");
				int id = 0;
				String doctoramka = null,doctorname = null,doctorsurname = null,app_date = null,app_time = null;
				out.println("<!DOCTYPE html><html><head></head><body>");
				out.println("Please Insert the ID of the availiable appointment you want and press the button");
				out.println("<form action='/ergasia/InsertAppServlet' method='GET'>");
				//out.println("<input type = 'hidden' name = 'specialty_chosen' value = '"+Integer.toString(specialty)+"'>");
				out.println("<input type ='text' name ='set_app'><br><br>");
				out.println("<button type='submit' value='submit'> GET </button>");
				out.println("</form>");
				out.println("<table border=\"1\">");
				out.println("<tr>");
				out.println("<th>Id</th>");
				out.println("<th>Doctor AMKA</th>");
				out.println("<th>Doctor Name</th>");
				out.println("<th>Doctor Surname</th>");
				out.println("<th>Appointment Date</th>");
				out.println("<th>Appointment Time</th>");
				out.println("</tr>");
				while(rs.next())
				{
					id = rs.getInt("id");
					doctoramka = rs.getString("doctor_amka");
					doctorname = rs.getString("name");
					doctorsurname = rs.getString("surname");
					app_date = rs.getString("app_date");
					app_time = rs.getString("app_time");
					String htmlRowForApp = createHTMLRowForApp(id, doctoramka, doctorname, doctorsurname,app_date,app_time);
					out.println(htmlRowForApp);
				}
				out.println("<a href=PatientChoices.html>Go Back</a>");
				out.println("</body></html>");
				rs.close();
			}
			catch(Exception e)
			{
				out.println("<a href=PatientChoices.html>Something went Wrong OR there is now availiable dates. Try again</a>");
			}
		}
	}
	
	
	
	public void showPersonalInfo(HttpServletRequest request,PrintWriter out)
	{
		HttpSession session = request.getSession();
		synchronized(session)
		{
		try {
			String user_id = null, name = null, surname=null, gender = null;
			Connection con = datasource.getConnection();
			Statement stmt = con.createStatement();
			String patientamka = (String) session.getAttribute("patientamka");
			String password = (String) session.getAttribute("password");
			ResultSet rs = stmt.executeQuery("SELECT * FROM patient WHERE patient_amka = '" + patientamka + "' and password = '" +password+"'");
			while(rs.next()) {
				 user_id = rs.getString("user_id");
				 name = rs.getString("name");
				 surname = rs.getString("surname");
				 gender = rs.getString("gender");
				
			}
			rs.close();
			
				if(user_id.equals(null))
					out.println("<p>Wrong data</p>");
				else
					out.println("<table border=\"1\">");
					out.println("<tr>");
					out.println("<th>User_id</th>");
					out.println("<th>Name</th>");
					out.println("<th>Surname</th>");
					out.println("<th>Gender</th>");
					out.println("</tr>");
					String htmlRowForInfo = createHTMLRowforInfo(user_id, name, surname, gender);
					out.println(htmlRowForInfo);
					out.println("<a href = PatientChoices.html>GoBack</a>");
			
			

			con.close();
		} catch(Exception e) {
			out.println("<a href = PatientChoices.html>Wrong data or Database connection problem</a>");
		}
		out.println("</body>");
		out.println("</html>");
	}}
	
	public void show_History(HttpServletRequest request,PrintWriter out)
	{
		HttpSession session = request.getSession();
		synchronized(session)
		{
			String patientamka = (String)session.getAttribute("patientamka");
			String date = null, doctoramka = null, diagnosis = null, doctorname = null;
			//int c=0;
			try {
				Connection con = datasource.getConnection();
				Statement stmt = con.createStatement();

				out.println("<table border=\"1\">");
				out.println("<tr>");
				out.println("<th>Id</th>");
				out.println("<th>Date</th>");
				out.println("<th>Patient_amka</th>");
				out.println("<th>Doctor_amka</th>");
				out.println("<th>Doctor_name</th>");
				out.println("<th>Diagnosis</th>");
				out.println("</tr>");
				
				ResultSet rs = stmt.executeQuery("SELECT appointments.id,appointments.date,appointments.doctor_amka,appointments.diagnosis,doctor.name"
						+ " FROM appointments INNER JOIN doctor ON appointments.doctor_amka = doctor.doctor_amka WHERE patient_amka = '" + patientamka + "' ");
				while(rs.next()) {
					int id = rs.getInt("id");
					date = rs.getString("date");
					doctoramka = rs.getString("doctor_amka");
					doctorname = rs.getString("name");
					diagnosis = rs.getString("diagnosis");
					if(date.equals(null)){
						break;
					}	
					else{
						String htmlRow = createHTMLRow(id, date, patientamka, doctoramka,doctorname,diagnosis);
						out.println(htmlRow);
						
					}	

				}
				
				rs.close();
							
				out.println("<a href = PatientChoices.html>GoBack</a>");
				con.close();
			} catch(Exception e) {
				out.println("Wrong data or Database connection problem");
			}

			out.println("</body>");
			out.println("</html>");
		}
		
		

	}
	
	
	
	
	public void cancelApp(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
	{
		
		HttpSession session = request.getSession();
		String date = null,doctor_amka = null,doctor_name = null,doctor_surname=null;
		int id = 0,specialty=0,id_av=0;
		synchronized(session)
		{
			try
			{
				Connection con = datasource.getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM appointments WHERE id='"+Integer.parseInt(request.getParameter("id"))+"' ");
				while(rs.next())
				{
					doctor_amka = rs.getString("doctor_amka");
					date=rs.getString("date");
					
				}
				ResultSet rs1 = stmt.executeQuery("SELECT * FROM doctor WHERE doctor_amka = '"+doctor_amka+"'");
				while(rs1.next())
				{
					doctor_name = rs1.getString("name");
					doctor_surname = rs1.getString("surname");
					specialty = rs1.getInt("specialty");
					
				}
				ResultSet rs2 = stmt.executeQuery("SELECT * FROM availiability ORDER BY id DESC limit 1 ");
				while(rs2.next())
				{
					id_av = rs2.getInt("id");
				}
								
				id_av++;
				
				String[] parts = date.split("-");
				String year = parts[0];
				String month = parts[1]; // minas tou rantevou
				String day = parts[2].substring(0, 2); // imera tou rantevou
				java.sql.Date current_date=new java.sql.Date(System.currentTimeMillis());//pairnei tin imerominia tin twrini
				String current = current_date.toString();//metatrepei tin twrini imerominia se string
				String[] current_parts = current.split("-");//kanei split me vasi -
				String current_year = current_parts[0];
				String current_month = current_parts[1];
				String current_day = current_parts[2];
									
				PreparedStatement ps1 = con.prepareStatement("INSERT INTO availiability VALUES(?,?,?,?,?,?,?)");
				ps1.setInt(1, id_av);
				ps1.setString(2, doctor_amka);
				ps1.setString(3, doctor_name);
				ps1.setString(4, doctor_surname);
				ps1.setInt(5,specialty);
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");						
				java.util.Date utilDate = sf.parse(year+"-"+month+"-"+day);
				java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
				ps1.setDate(6, sqlDate);
				ps1.setString(7, parts[2].substring(3, 11));
				int n1 = ps1.executeUpdate();
				
						
				
				String query = "DELETE FROM appointments WHERE id=?";
				PreparedStatement ps = con.prepareStatement(query);	
				
				ps.setInt(1, Integer.parseInt(request.getParameter("id")));
				if(year.equals(current_year) & Integer.parseInt(current_month) <= Integer.parseInt(month) & Integer.parseInt(current_day) <= Integer.parseInt(day)-3)
				{
					int n = ps.executeUpdate();
					con.close();
					if(n > 0){
						out.println("<a href = PatientChoices.html>Operation was successfull. Go Back</a>");
					
					}
				}
				else
					out.println("<a href = PatientChoices.html>The operation cannot be performed due to date issues.</a>");
				
				
			}
			catch(Exception e)
			{
				out.println("<a href=PatientChoices.html>Something went wrong. Try again</a>");
			}
			
			
			
		}
		
		
		
	}
	
	

	
	private DataSource datasource = null;
	public void init() throws ServletException{
		try {
	
			InitialContext ctx = new InitialContext();
			datasource = (DataSource)ctx.lookup("java:comp/env/jdbc/postgres");
		} catch(Exception e) {
			throw new ServletException(e.toString());
		}

	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
		synchronized(session)
		{
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			String action = request.getParameter("action");
			try
			{
				if(action.equals("View your personal data"))
					showPersonalInfo(request,out);
				if(action.equals("View History of your appointments"))
					show_History(request,out);
				if(action.equals("Choose Specialty"))
				{
					RequestDispatcher view = request.getRequestDispatcher("ChooseSpecialty.html");
					view.forward(request, response);
				}
				if(action.equals("Cancel Appointment"))
				{
					out.println("<!DOCTYPE html><html><head><meta charset=UTF-8><title>Cancel Appointments</title></head><body><table><tr><th>Cancel Appointments</th></tr></table>");
					out.println("<fieldset><form action = PatientServlet  method =POST><input type='hidden' name='operation' value='Cancel Appointment'>");
					out.println("Enter the ID of the appointment you want to Cancel <br><br>");
					out.println("ID   :");
					out.println("<input type = 'text' name = 'id'><br>");
					out.println("<br><br><input type = 'submit'></form></fieldset></body></html>");
				}
				if(action.equals("Log out"))
					Logout(request,response,out);
			}
			catch(Exception e)
			{
				out.println("<a href = 'PatientChoices.html'>Please select an option. </a>");
				
			}
					
		}
		
		
	
	}
	private String createHTMLRow(int id, String date, String patientamka, String doctoramka,String doctorname,String diagnosis) {
		String row = "<tr>";
		row  += "<td>" + id + "</td>";
		row  += "<td>" + date + "</td>";
		row  += "<td>" + patientamka + "</td>";
		row  += "<td>" + doctoramka + "</td>";
		row  += "<td>" + doctorname + "</td>";
		row  += "<td>" + diagnosis + "</td>";
		row +="</tr>";
		return row;

	}
	
	private String createHTMLRowForApp(int id,String doctoramka,String doctorname,String doctorsurname,String app_date,String app_time) {
		String row = "<tr>";
		row  += "<td>" + id + "</td>";
		row  += "<td>" + doctoramka + "</td>";
		row  += "<td>" + doctorname + "</td>";
		row  += "<td>" + doctorsurname + "</td>";
		row  += "<td>" + app_date + "</td>";
		row  += "<td>" + app_time + "</td>";
		row +="</tr>";
		return row;

	}
	
	private String createHTMLRowforInfo(String user_id, String name, String surname, String gender) {
		String row = "<tr>";
		row  += "<td>" + user_id + "</td>";
		row  += "<td>" + name + "</td>";
		row  += "<td>" + surname + "</td>";
		row  += "<td>" + gender + "</td>";
		row +="</tr>";
		return row;

	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
		synchronized(session)
		{
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			String operation = request.getParameter("operation");
			if(operation.equals("Choose Specialty"))
			{
				int spec = Integer.parseInt(request.getParameter("specialty"));
				setAppFromSpecialty(request,response,out,spec);
			}
			if(operation.equals("Cancel Appointment"))
			{
				cancelApp(request,response,out);
			}
				
			
				
					
		
			
		}
	}

}
