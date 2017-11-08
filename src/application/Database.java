package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class Database {
	public int code;

	public String fname;
	public String Lname;
	public int reg;
	public int age;
	public String sec;

	public final String Database_name = "ghosteye";
	public final String Database_user = "root";
	public final String Database_pass = "";

	public Connection con;

	public boolean init() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			try {
				this.con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + Database_name, Database_user,
						Database_pass);
			} catch (SQLException e) {

				System.out.println("Error: Database Connection Failed ! Please check the connection Setting");

				return false;

			}

		} catch (ClassNotFoundException e) {

			e.printStackTrace();

			return false;
		}

		return true;
	}

	public void insert() {
		String sql = "INSERT INTO face_bio (code, first_name, last_name, reg, age , section) VALUES (?, ?, ?, ?,?,?)";

		PreparedStatement statement = null;
		try {
			statement = con.prepareStatement(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {

			statement.setInt(1, this.code);
			statement.setString(2, this.fname);

			statement.setString(3, this.Lname);
			statement.setInt(4, this.reg);
			statement.setInt(5, this.age);
			statement.setString(6, this.sec);

			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				System.out.println("A new face data was inserted successfully!");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public ArrayList<String> getUser(int inCode) throws SQLException {

		ArrayList<String> user = new ArrayList<String>();

		try {

			Database app = new Database();

			String sql = "select * from face_bio where code=" + inCode + " limit 1";

			Statement s = con.createStatement();

			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {

				/*
				 * app.setCode(rs.getInt(2)); app.setFname(rs.getString(3));
				 * app.setLname(rs.getString(4)); app.setReg(rs.getInt(5));
				 * app.setAge(rs.getInt(6)); app.setSec(rs.getString(7));
				 */

				user.add(0, Integer.toString(rs.getInt(2)));
				user.add(1, rs.getString(3));
				user.add(2, rs.getString(4));
				user.add(3, Integer.toString(rs.getInt(5)));
				user.add(4, Integer.toString(rs.getInt(6)));
				user.add(5, rs.getString(7));

				/*
				 * System.out.println(app.getCode());
				 * System.out.println(app.getFname());
				 * System.out.println(app.getLname());
				 * System.out.println(app.getReg());
				 * System.out.println(app.getAge());
				 * System.out.println(app.getSec());
				 */

				// nString="Name:" + rs.getString(3)+" "+rs.getString(4) +
				// "\nReg:" + app.getReg() +"\nAge:"+app.getAge() +"\nSection:"
				// +app.getSec() ;

				// System.out.println(nString);
			}

			con.close(); // closing connection
		} catch (Exception e) {
			e.getStackTrace();
		}
		return user;
	}

	public void db_close() throws SQLException
	{
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getLname() {
		return Lname;
	}

	public void setLname(String lname) {
		Lname = lname;
	}

	public int getReg() {
		return reg;
	}

	public void setReg(int reg) {
		this.reg = reg;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getSec() {
		return sec;
	}

	public void setSec(String sec) {
		this.sec = sec;
	}

}
