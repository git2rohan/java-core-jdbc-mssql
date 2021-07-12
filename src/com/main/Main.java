package com.main;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Main {
	
	public static void main(String[] args) 
	{
		//Steps
		//STEP = 1 Driver Registration
		//STEP = 2 Get Connection
		//STEP = 3 Create Statement
		//STEP = 4 Execute query
		//STEP = 5 Close connection		
		try {
			
			Main main = new Main();
			
			//Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");	
			//in JDBC 4.0 no need to register driver explicitly
			
			//fetch server conf
			Map<String, String> conf = main.readSQLconfig();
			
			
			Connection conn = DriverManager.getConnection(conf.get("server"),conf.get("username"),conf.get("password"));
			System.out.println("tested successful..");
			
			
			//user input
			Scanner scanner = new Scanner(System.in);
			Map<String, String> map = main.register(scanner);
			
			//insert into db
			System.out.println("---------Insert data----------");
			int rowsAffected = main.insertData(map, conn);
			System.out.println("Rows affected.. "+rowsAffected);
			
			//return data
			String query = "SELECT * FROM [TestDB].[dbo].[User]";
			main.returnData(conn, query, main);
			
			//update 
			System.out.println("---------Update data----------");
			int rowsUpdated = main.updateRows(conn, scanner);
			System.out.println("Rows updated.. "+rowsUpdated);
			
			//select data again
			main.returnData(conn, query, main);
			
			//close scanner and connection
			conn.close();
			scanner.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	//take input from user
	public Map<String, String> register(Scanner scanner)
	{

		Map<String, String> map = new HashMap<>();
		System.out.print("Enter your name : ");
		String name = scanner.next(); 
		System.out.print("Enter your email: ");
		String email = scanner.next();
		System.out.print("Enter your pass : ");
		String pass = scanner.next();
		
		map.put("name", name);
		map.put("email", email);
		map.put("pass", pass);
		
		return map;
	}
	
	//insert query
	public int insertData(Map<String, String> map, Connection conn)
	{
		try 
		{
			String sql = "insert into [TestDB].[dbo].[User] values(?,?,?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, map.get("name"));
			ps.setString(2, map.get("email"));
			ps.setString(3, map.get("pass"));
			
			int i = ps.executeUpdate();
			
			return i;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return 0;
	}
	
	//print selected data
	public void returnData(Connection conn, String query, Main main)
	{
		System.out.println("---------select data----------");
		ResultSet rs = main.selectData(query, conn);
		try {
			while (rs.next()) {
				System.out.println(rs.getString("id")+" | "+ rs.getString("name") +" | "+ rs.getString("email") + " | " + rs.getString("password"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//select query
	public ResultSet selectData(String query, Connection con)
	{
		try 
		{
			PreparedStatement statement = con.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			return rs;
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	//update
	public int updateRows(Connection con,Scanner scanner)
	{
		try {
			System.out.print("Do you want to update records? (y/n)");
			String ans = scanner.next(); 
			
			if(ans.equals("y"))
			{
				System.out.print("Enter ID : ");
				int id = Integer.parseInt(scanner.next());
				
				Main main = new Main();
				Map<String, String> map = main.register(scanner);
				
				String query = "update [TestDB].[dbo].[User] set name=?, email=?, password=? where id=?";
				PreparedStatement ps = con.prepareStatement(query);
				ps.setString(1, map.get("name"));
				ps.setString(2, map.get("email"));
				ps.setString(3, map.get("pass"));
				ps.setInt(4, id);
				
				return ps.executeUpdate();
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public Map<String, String> readSQLconfig()
	{
		
//      create a config-server.json file in src with below blueprint
//		{
//			"server":"jdbc:sqlserver://pcname\\sql-instance-name",
//			"username" : "username",
//			"password" : "pass"
//		}
		JSONParser parser = new JSONParser();
		
		try {
			Object obj = parser.parse(new FileReader("src/config-server.json"));
			JSONObject json = (JSONObject)obj;
			String server = (String)json.get("server");
			String username = (String)json.get("username");
			String password = (String)json.get("password");
			
			Map<String, String> map = new HashMap<>();
			map.put("server", server);
			map.put("username", username);
			map.put("password", password);
			
			return map;
			
		} 
		catch(Exception ex) 
		{
			ex.printStackTrace();
		}
		
		return null;
	}
}
