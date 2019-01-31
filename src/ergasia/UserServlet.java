package ergasia;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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
@WebServlet("/UserServlet")
public class UserServlet extends HttpServlet{

	
	public UserServlet() {
		// TODO Auto-generated constructor stub
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
	

	
	
	
	public void register(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try
		{
			Connection con = datasource.getConnection();
			String pass = request.getParameter("password");
			PrintWriter out = response.getWriter();
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
					out.println("<a href = index.html>Operation was successfull. Go Back</a>");
				else
					out.println("<a href = index.html>Error occured. Try again !  </a>");
			}
			else{
				out.println("<a href = index.html>One or more attributes remained null. Try again!</a>");
			}
		}
		catch(Exception e)
		{
			PrintWriter out = response.getWriter();
			out.println("<a href = InsertPatient.html>Error occured. Try again !</a>");
		}
	}
	
	

	public void login(HttpServletRequest request, HttpServletResponse response,String amka,String password,String category) throws IOException, ServletException
	{
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession();
		synchronized(session)
		{
			try {
				if(category.equals("Patient"))
				{
					session.setAttribute("patientamka", amka);
					String salt;
					password = request.getParameter("password");
					salt="koefk";
					password = get_SHA_512_SecurePassword(password, salt).toString();
					
					//byte[] b=password.getBytes();
					//password = Base64.getEncoder().encodeToString(b);
					
					String patientamka = (String) session.getAttribute("patientamka");
					
					try
					{
							Connection con = datasource.getConnection();
							Statement stmt = con.createStatement();
							ResultSet rs = stmt.executeQuery("SELECT * FROM patient WHERE patient_amka='" + patientamka + "' AND password = '" + password + "'");
							if(rs.next()==false)
							{															
									out.println("<a href = 'index.html'>Wrong data. Try again!</a>");
																					
							}
								
							else
							{
								session.setAttribute("password", password);
								RequestDispatcher view = request.getRequestDispatcher("PatientChoices.html");
								view.forward(request, response);
							}
								
					
							
					}
					catch(Exception e)
					{
						out.println("<a href = 'index.html'>Wrong data or Database Connection problem. Try again!</a>");
					}
					
				}
				else if(category.equals("Admin"))
				{
					
					session.setAttribute("adminusername", amka);
					//byte[] b=password.getBytes();
					String salt;
					password = request.getParameter("password");
					salt="koefk";
					password = get_SHA_512_SecurePassword(password, salt).toString();
					//session.setAttribute("password", password);
					String username = (String) session.getAttribute("adminusername");
					Connection con;
					try {
						con = datasource.getConnection();
						Statement stmt = con.createStatement();
						ResultSet rs = stmt.executeQuery("SELECT * FROM admin WHERE username = '" + username + "' AND  password = '" + password + "'");
						if(rs.next()==false)							
								out.println("<a href = 'index.html'>Wrong data. Try again!</a>");
						else
						{
							session.setAttribute("password",password);
							RequestDispatcher view = request.getRequestDispatcher("AdminChoices.html");
							view.forward(request, response);
						}
													
					
				
				}
							
					 catch(Exception e)
					{
						out.println("<a href = 'index.html'>Wrong data or Database Connection problem. Try again!</a>");
					}
					
				}
				else if(category.equals("Doctor"))
				{
					session.setAttribute("doctoramka", amka);
					//byte[] b=password.getBytes();
					//password = Base64.getEncoder().encodeToString(b);
					String salt;
					password = request.getParameter("password");
					salt="koefk";
					password = get_SHA_512_SecurePassword(password, salt).toString();
					//session.setAttribute("password", password);
					String doctoramka = (String) session.getAttribute("doctoramka");
					try
					{
						
							Connection con = datasource.getConnection();
							Statement stmt = con.createStatement();
							ResultSet rs = stmt.executeQuery("SELECT * FROM doctor WHERE doctor_amka='" + doctoramka + "' AND password = '" + password + "'");
							if(rs.next()==false)
							{								
								out.println("<a href = 'index.html'>Wrong data. Try again!</a>");
																		
							}
								
							else
							{
								session.setAttribute("password", password);
								session.setAttribute("doctorname", rs.getString("name"));
								session.setAttribute("doctorsurname", rs.getString("surname"));
								session.setAttribute("doctorspecialty", rs.getInt("specialty"));
								RequestDispatcher view = request.getRequestDispatcher("DoctorChoices.html");
								view.forward(request, response);
							}
								
					
							
					}
					catch(Exception e)
					{
						out.println("<a href = 'index.html'>Wrong data or Database Connection problem. Try again!</a>");
					}
				}
			}
			catch(NullPointerException e){
				out.println("<a href = 'index.html'>Pick specialty from the previous page. Go back.</a>");
			}

		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String operation = request.getParameter("operation");
		if(operation.equals("register")){
			
			register(request,response);
			}
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		synchronized(session)
		{
			
				String amka = request.getParameter("amka");
				String password=request.getParameter("password");
				String category = request.getParameter("category");
				try
				{
				if(category.equals("Sign up"))
				{
					RequestDispatcher view = request.getRequestDispatcher("Register.html");
					view.forward(request, response);
					
				}
				else
					login(request,response,amka,password,category);
				
				}
			
			catch(Exception e)
			{
				PrintWriter out = response.getWriter();
				out.println("<a href = 'index.html'>Please fill the appropriate fields</a>");
			}
		}
		}
	}
