package ergasia;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.text.DateFormat;
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
@WebServlet("/DoctorServlet")
public class DoctorServlet extends HttpServlet{
	private DataSource datasource = null;
	public void init() throws ServletException
	{
		
		try {
			
			InitialContext ctx = new InitialContext();
			datasource = (DataSource)ctx.lookup("java:comp/env/jdbc/postgres");
		} catch(Exception e) {
			throw new ServletException(e.toString());
		}
	}
	
	
		
	public DoctorServlet() {
		// TODO Auto-generated constructor stub
	}
	
	
	
	public void setAvailiability(HttpServletRequest request, HttpServletResponse response,PrintWriter out) throws ServletException, IOException
	{
		HttpSession session = request.getSession();
		int id = 0;
		synchronized(session)
		{
		try
		{
			Connection con = datasource.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM availiability ORDER BY id DESC limit 1 ");
			while(rs.next())
			{
				id = rs.getInt("id");
			}
							
			id++;
			String query = "INSERT INTO availiability VALUES(?,?,?,?,?,?,?)";
			PreparedStatement ps = con.prepareStatement(query);				
			ps.setInt(1, id);
			ps.setString(2, (String) session.getAttribute("doctoramka"));					
			ps.setString(3, (String) session.getAttribute("doctorname"));
			ps.setString(4, (String)session.getAttribute("doctorsurname"));
			ps.setInt(5, (int)session.getAttribute("doctorspecialty"));	
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");						
			java.util.Date utilDate = sf.parse(request.getParameter("date"));
			java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
			ps.setDate(6, sqlDate);
			String app_time = request.getParameter("h")+":"+request.getParameter("m")+":"+request.getParameter("s");
			ps.setString(7, app_time );
			java.sql.Date current_date=new java.sql.Date(System.currentTimeMillis());//pairnei tin imerominia tin twrini
			String current = current_date.toString();//metatrepei tin twrini imerominia se string
			String[] current_parts = current.split("-");//kanei split me vasi -
			String current_year = current_parts[0];
			String current_month = current_parts[1];
			String current_day = current_parts[2];
			String date = request.getParameter("date");//gia na elegxei tin imerominia pou edwse o xristis me tin twrini imerominia
			String[] parts = date.split("-");
			String year = parts[0];
			String month = parts[1];
			String day = parts[2];
			if(current_year.equals(year) & (month.equals(current_month) || Integer.parseInt(month) == Integer.parseInt(current_month)+1))
				if(month.equals(current_month) & Integer.parseInt(day) >= Integer.parseInt(current_day))
				{
					int n = ps.executeUpdate();
					con.close();
					if(n > 0)
						out.println("<a href = DoctorChoices.html>Operation was successfull. Go Back</a>");
				}
				else
					out.println("<a href =DoctorChoices.html>Wrong Data. Try again.</a> ");
			else
				out.println("<a href =DoctorChoices.html>Wrong Data. Try again.</a> ");
			
			
		}
		catch(Exception e)
		{
			out.println("<a href = EnterAvailiability.html>Wrong date format. Try again. !</a>");
		}}
	}
	
	
	public void viewAppointments(HttpServletRequest request, HttpServletResponse response,PrintWriter out)
	{
		HttpSession session = request.getSession();
		synchronized(session)
		{
			String doctoramka = (String) session.getAttribute("doctoramka");
			String date = null, diagnosis = null,patientamka = null;
			int id = 0;
			try {
				Connection con = datasource.getConnection();
				Statement stmt = con.createStatement();

				out.println("<table border=\"1\">");
				out.println("<tr>");
				out.println("<th>Id</th>");
				out.println("<th>Date</th>");
				out.println("<th>Patient_amka</th>");
				out.println("<th>Doctor_amka</th>");
				out.println("<th>Diagnosis</th>");
				out.println("</tr>");
				
				ResultSet rs = stmt.executeQuery("SELECT * FROM appointments WHERE doctor_amka = '" + doctoramka + "' ");
				while(rs.next()) {
					id = rs.getInt("id");
					date = rs.getString("date");
					patientamka = rs.getString("patient_amka");
					diagnosis = rs.getString("diagnosis");
					if(rs.wasNull())
						diagnosis = "-";
					if(date.equals(null)){
						break;
					}	
					else{
						String htmlRow = createHTMLRowforApp(id, date, patientamka, doctoramka,diagnosis);
						out.println(htmlRow);
						
					}	

				}
				
				rs.close();
				
				
				out.println("<a href = 'DoctorChoices.html'>GoBack</a>");
				con.close();
			} catch(Exception e) {
				out.println("Wrong data or Database connection problem");
			}

			out.println("</body>");
			out.println("</html>");
		}
	}
	
	private String createHTMLRowforApp(int id, String date, String patientamka,String doctoramka, String diagnosis) {
		String row = "<tr>";
		row  += "<td>" + id + "</td>";
		row  += "<td>" +date + "</td>";
		row  += "<td>" + patientamka + "</td>";
		row  += "<td>" + doctoramka + "</td>";
		row  += "<td>" + diagnosis + "</td>";
		row +="</tr>";
		return row;

	}
	
	public void cancelAppointment(HttpServletRequest request, HttpServletResponse response,PrintWriter out) throws ServletException, IOException
	{
		HttpSession session = request.getSession();
		String date = null;
		synchronized(session)
		{
			try
			{
				Connection con = datasource.getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM appointments WHERE id='"+Integer.parseInt(request.getParameter("id"))+"' ");
				while(rs.next())
				{
					date=rs.getString("date");
				}
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
				String query = "DELETE FROM appointments WHERE id=?";
				PreparedStatement ps = con.prepareStatement(query);	
				
				ps.setInt(1, Integer.parseInt(request.getParameter("id")));
				if(year.equals(current_year) & Integer.parseInt(current_month) <= Integer.parseInt(month) & Integer.parseInt(current_day) <= Integer.parseInt(day)-3)
				{
					int n = ps.executeUpdate();
					con.close();
					if(n > 0)
						out.println("<a href = DoctorChoices.html>Operation was successfull. Go Back</a>");
				}
				else
					out.println("<a href = DoctorChoices.html>The operation cannot be performed due to date issues.</a>");
				
				
			}
			catch(Exception e)
			{
				out.println("<a href = DoctorChoices.html>Something went wrong. Try again</a>");
			}
			
			
			
		}
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
				if(action.equals("Enter availiability"))
				{
					RequestDispatcher view = request.getRequestDispatcher("EnterAvailiability.html");
					view.forward(request, response);
				}
					
				if(action.equals("View your appointments"))
					viewAppointments(request,response,out);
				if(action.equals("Cancel an appointment"))
				{
					RequestDispatcher view = request.getRequestDispatcher("CancelAppointments.html");
					view.forward(request, response);
				}
					
				if(action.equals("Log out"))
					Logout(request,response,out);
			}
			catch(Exception e)
			{
				out.println("<a href = 'DoctorChoices.html'> Please select an option.</a>");
			}
			
		}
		
		
	
	}
	
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
		synchronized(session)
		{
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			String operation = request.getParameter("operation");
			try
			{
				if(operation.equals("Enter Availiability"))
					setAvailiability(request,response,out);
				if(operation.equals("Cancel Appointment"))
					cancelAppointment(request,response,out);
			}
			catch(Exception e)
			{
				out.println("<a href = 'DoctorChoices.html'> Please select an option.</a>");
			}
			
				
					
		
			
		}
	}




}
