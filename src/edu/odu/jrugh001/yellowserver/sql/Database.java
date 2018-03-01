package edu.odu.jrugh001.yellowserver.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public class Database {
	
	private static Connection connection = null;
	
	// Database URL and credentials
	private static String DB_URL = "jdbc:mysql://localhost:3306/yellow?autoReconnect=true&useSSL=false";
	private static String USERNAME = "root";
	private static String PASSWORD = "vco9_2";
	
	public boolean initialize() {
		try {
			
			System.out.println("SQL database setup.");
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			
			connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
				
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException exe) {
			exe.printStackTrace();
			
			// Closes the SQL connection if it is not null
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException inexe) {
					inexe.printStackTrace();
				} 
			}
			return false;
		}
		return true;
	}
	
	public void insert(String table, String columns, final Object... data) {
		
		// Creating the query
		StringBuilder query = new StringBuilder("INSERT INTO " + table + " " + columns + " VALUES (");
		
		for (int i = 0; i < data.length; i++) {
			query.append("?,");
		}
		
 		query = new StringBuilder(query.substring(0, query.length() - 1));
 		query.append(")");
		
 		PreparedStatement stmt = null;
		try {
		
	 		// Making the prepared statement
	 		stmt = connection.prepareStatement(query.toString());
	 		
	 		for (int i = 0; i < data.length; i++) {
	 			stmt.setObject(i + 1, data[i]);
	 		}			
			
			// Sending the data to the database
			stmt.executeUpdate();
			
		} catch (SQLException exe) {
			exe.printStackTrace();
		} finally {
			// Closing the prepared statement connection
			if (stmt != null) { try { stmt.close(); } catch (SQLException exe) { exe.printStackTrace(); } }
		}
	}
	
	public boolean querySearch(String table, String column, Object data) {
		
		// Query for the prepared statement
		String query = "SELECT * FROM " + table + " WHERE " + column + " = ?";
		
		PreparedStatement stmt = null;
		ResultSet result = null;
	
		// Making a prepared statement for the query
		try {
			stmt = connection.prepareStatement(query);
			
			stmt.setObject(1, data); 
			// Executing the query's statement
			result = stmt.executeQuery();
			
			if (result.next()) {
				if (stmt != null) { stmt.close(); }
				if (result != null) { result.close(); }
				return true;
			}
			
		} catch (SQLException exe) {
			exe.printStackTrace();
		} finally {
			if (stmt != null) { try { stmt.close(); } catch (SQLException exe) { exe.printStackTrace(); }}
			if (result != null ) { try { result.close(); } catch (SQLException exe) { exe.printStackTrace(); }}
		}

		return false;
	}
	
	public void find(String table, String column, Object data, Consumer<ResultSet> findData) {
		
		String query = "SELECT * FROM " + table + " WHERE " + column + " = ?";
		
		PreparedStatement stmt = null;
		ResultSet result = null;
		
		try {
			
			stmt = connection.prepareStatement(query);
			
			stmt.setObject(1, data); 
			// Executing the query's statement
			result = stmt.executeQuery();
			
			findData.accept(result);
			
		} catch (SQLException exe) {
			exe.printStackTrace();
		} finally {
			if (stmt != null) { try { stmt.close(); } catch (SQLException exe) { exe.printStackTrace(); }}
			if (result != null ) { try { result.close(); } catch (SQLException exe) { exe.printStackTrace(); }}
		}
	}
	
	/**
     * column syntax: column1 column2 column3 
	 */
	public void update(String table, String columns, String updateWhere,
			final Object whereData, final Object... data) {
		
		StringBuilder update = new StringBuilder();
		update.append("UPDATE ").append(table).append(" SET ");
		String[] columnsArray = columns.split(" ");
		
		for (int i = 0; i < columnsArray.length; i++) {
			update.append(columnsArray[i]).append(" = ?,");
		}
		update = new StringBuilder(update.substring(0, update.length() - 1));
		
		// Includes where to update at
		update.append(" WHERE ").append(updateWhere).append(" = ?");
		
		PreparedStatement stmt = null;
 		try {
 			
 			// Making the prepared statement
			stmt = connection.prepareStatement(update.toString());
			
			int i = 0;
			for (; i < data.length; i++) {
	 			stmt.setObject(i + 1, data[i]);
	 		}	
			
			stmt.setObject(i + 1, whereData);
			stmt.executeUpdate();
			
		} catch (SQLException exe) {
			exe.printStackTrace();
		} finally {
			if (stmt != null) { try { stmt.close(); } catch (SQLException exe) { exe.printStackTrace(); }}
		}
	}
	
	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException exe) {
				exe.printStackTrace();
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		// double ensuring the database is closed.
		close();
	}
}
