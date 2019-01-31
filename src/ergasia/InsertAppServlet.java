package ergasia;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;


@WebServlet("/InsertAppServlet")
public class InsertAppServlet extends HttpServlet {
	//private static final long serialVersionUID = 1L;
       
    
    public InsertAppServlet() {
        //super();
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
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		HttpSession session = request.getSession();
		synchronized(session){
		String code = request.getParameter("set_app");
		int id = 0;
		Date date_of_app =null;
		String time_of_app = null,doctor_amka = null;
		try{
		id = Integer.parseInt(code);}
		catch(Exception e)
		{
			
		}
		
		try
		{
			int app_id = 0;
			Connection con = datasource.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs2 = stmt.executeQuery("SELECT * FROM availiability WHERE id = '"+ id +"'");
			while(rs2.next())
			{
				date_of_app = rs2.getDate("app_date");
				time_of_app = rs2.getString("app_time");
				doctor_amka = rs2.getString("doctor_amka");
			}
			int rs = stmt.executeUpdate("DELETE FROM availiability WHERE id = '"+ id +"'");
			ResultSet rs3 = stmt.executeQuery("SELECT * FROM appointments ORDER BY id DESC limit 1 ");
		
			
			while(rs3.next())
			{
				app_id = rs3.getInt("id");
				app_id++;
			}
			String query = "INSERT INTO appointments VALUES(?,?,?,?,?)";
			PreparedStatement ps = con.prepareStatement(query);				
			ps.setInt(1, app_id);
			ps.setString(2,date_of_app.toString()+" "+time_of_app);
			ps.setString(3, (String)session.getAttribute("patientamka"));
			ps.setString(4, doctor_amka);
			ps.setString(5, "-");
			int n = ps.executeUpdate();
			PrintWriter out = response.getWriter();
			if(n > 0)
				out.println("<a href = PatientChoices.html>Operation was successfull. Go Back</a>");
			else
				out.println("<a href = PatientChoices.html>Error occured. Try again !  </a>");
				
				
		}
		catch(Exception e)
		{
			PrintWriter out = response.getWriter();
			out.println("<a href=PatientChoices.html>Error occured. Go Back</a>");
		}
		
		
		
		
		
		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response,PrintWriter out) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		
	}

}
