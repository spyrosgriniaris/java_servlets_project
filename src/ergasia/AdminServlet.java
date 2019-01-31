package ergasia;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Base64;

import javax.naming.InitialContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

@WebServlet("/AdminServlet")
public class AdminServlet extends HttpServlet{

	public AdminServlet() {
		// TODO Auto-generated constructor stub
	}
	
	
	
	public void InsertDoctor(HttpServletRequest request,HttpServletResponse response,PrintWriter out) throws ServletException, IOException
	{
		
			RequestDispatcher view = request.getRequestDispatcher("InsertDoctor.html");
			view.forward(request, response);	
		//out.println("<!DOCTYPE html<html><head><meta charset=UTF-8><title>Login Form</title></head><body><table><tr><th>Login Form</th></tr></table><p> Please fill the form in order to login in the system</p><p> Use your username to login only if you are an Admin</p><fieldset><form action = UserServlet method = POST>	AMKA / Username  :	<input type = text name = amka><br>	Password         :	<input type = password name = password><br>	Category :<br>	<input type = radio name = category value = Patient>Sign in as patient <br>	<input type = radio name = category value = Doctor>Sign in as doctor <br>	<input type = radio name = category value = Admin>Sign in as admin <br>	<input type = submit>	</form></fieldset></body></html>");
	}
	
	
	public void DeleteDoctor(HttpServletRequest request,HttpServletResponse response)
	{
		RequestDispatcher view = request.getRequestDispatcher("DeleteDoctor.html");
		try {
			view.forward(request, response);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void InsertPatient(HttpServletRequest request, HttpServletResponse response)
	{
		RequestDispatcher view = request.getRequestDispatcher("InsertPatient.html");
		try {
			view.forward(request, response);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				if(action.equals("InsertDoctor"))			
					InsertDoctor(request,response,out);		
				if(action.equals("DeleteDoctor"))
					DeleteDoctor(request, response);
				if(action.equals("InsertPatient"))
					InsertPatient(request, response);
				if(action.equals("Log out"))
					Logout(request,response,out);
			}
			catch(NullPointerException e)
			{
				out.println("<a href = 'AdminChoices.html'> Please select an option.</a>");
			}
		
				
			
		}
		
	
	}
	
	public static String get_SHA_512_SecurePassword(String passwordToHash, String   salt){
		String generatedPassword = null;
		byte bytes[]=null;
		    try {
		         MessageDigest md = MessageDigest.getInstance("SHA-512");
		         try {
					md.update(salt.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
		         byte[] bytes1 = null;
				try {
					bytes1 = md.digest(passwordToHash.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
		         StringBuilder sb = new StringBuilder();
		         for(int i=0; i< bytes1.length ;i++){
		            sb.append(Integer.toString((bytes1[i] & 0xff) + 0x100, 16).substring(1));
		         }
		         generatedPassword = sb.toString();
		        } 
		       catch (NoSuchAlgorithmException e){
		        e.printStackTrace();
		       }
		    return generatedPassword;
		}

	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		synchronized(session)
		{
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			String operation = request.getParameter("operation");
			try
			{
				if(operation.equals("insert"))
				{
					try
					{
						Connection con = datasource.getConnection();
						String pass = request.getParameter("password");
						//byte[] b = pass.getBytes();
						//pass=Base64.getEncoder().encodeToString(b);
						String password, salt;
						password = request.getParameter("password");
						salt="koefk";
						if(request.getParameter("amka")!="" && request.getParameter("username")!="" && pass!="" && request.getParameter("name")!=""
								&& request.getParameter("surname")!="" && request.getParameter("specialty")!=""){
							String query = "INSERT INTO doctor VALUES(?,?,?,?,?,?)";
							PreparedStatement ps = con.prepareStatement(query);
							ps.setString(1, request.getParameter("amka"));
							ps.setString(2, request.getParameter("username"));					
							ps.setString(3, get_SHA_512_SecurePassword(password, salt).toString());
							ps.setString(4, request.getParameter("name"));
							ps.setString(5, request.getParameter("surname"));
							ps.setInt(6, Integer.parseInt(request.getParameter("specialty")));
							
							int n = ps.executeUpdate();
							if(n > 0)
								out.println("<a href = AdminChoices.html>Operation was successfull. Go Back</a>");
							else
								out.println("<a href = InsertDoctor.html>Error occured. Try again !  </a>");
						}
						else{
							out.println("<a href = InsertDoctor.html>One or more attributes remained null. Try again!</a>");
						}
						
					}
					catch(Exception e)
					{					
						out.println("<a href = InsertDoctor.html>Error occured. Try again !</a>");
					}
				}
				else if(operation.equals("insertpatient")){
					try
					{
						Connection con = datasource.getConnection();
						String pass = request.getParameter("password");
						//byte[] b = pass.getBytes();
						//pass=Base64.getEncoder().encodeToString(b);
						String password, salt;
						password = request.getParameter("password");
						salt="koefk";
						if(request.getParameter("patientamka")!="" && request.getParameter("userid")!="" && pass!=null && request.getParameter("name")!=""
								&& request.getParameter("surname")!="" && request.getParameter("gender")!=""){
							String query = "INSERT INTO patient VALUES(?,?,?,?,?,?)";
							PreparedStatement ps = con.prepareStatement(query);
							ps.setString(1, request.getParameter("patientamka"));
							ps.setString(2, request.getParameter("userid"));					
							ps.setString(3, get_SHA_512_SecurePassword(password, salt).toString());
							ps.setString(4, request.getParameter("name"));
							ps.setString(5, request.getParameter("surname"));
							ps.setString(6, request.getParameter("gender"));
							
							int n = ps.executeUpdate();
							if(n > 0)
								out.println("<a href = AdminChoices.html>Operation was successfull. Go Back</a>");
							else
								out.println("<a href = InsertPatient.html>Error occured. Try again !  </a>");
						}
						else{
							out.println("<a href = InsertPatient.html>One or more attributes remained null. Try again!</a>");
						}
					}
					catch(Exception e)
					{
						out.println("<a href = InsertPatient.html>Error occured. Try again !</a>");
					}
				}else if(operation.equals("delete")){
					try
					{
						Connection con = datasource.getConnection();
						if(request.getParameter("amka")!=""){
							String query2 = "DELETE FROM appointments WHERE doctor_amka=(?)";
							String query1 = "DELETE FROM doctor WHERE doctor_amka=(?)";
							String query3 = "DELETE FROM availiability WHERE doctor_amka=(?)";
							PreparedStatement ps3 = con.prepareStatement(query3);
							PreparedStatement ps1 = con.prepareStatement(query1);
							PreparedStatement ps2 = con.prepareStatement(query2);
							ps1.setString(1, request.getParameter("amka"));
							ps2.setString(1, request.getParameter("amka"));
							ps3.setString(1, request.getParameter("amka"));
							int n1 = ps2.executeUpdate();
							int n = ps1.executeUpdate();
							int n2 = ps3.executeUpdate();
							if(n > 0)
								out.println("<a href = AdminChoices.html>Operation was successfull. Go Back</a>");
							else
								out.println("<a href = DeleteDoctor.html>Error occured. Try again !  </a>");
						}
						else{
							out.println("<a href = DeleteDoctor.html>Textbox must be filled. Try again!</a>");
						}
						
					}
					catch(Exception e)
					{
						out.println("<a href = DeleteDoctor.html>Error occured. Try again !  </a>");
					}
				}
			}
			catch(NullPointerException e)
			{
				out.println("<a href = 'AdminChoices.html'> Please select an option.</a>");
			}
			
			
		}
	}

}