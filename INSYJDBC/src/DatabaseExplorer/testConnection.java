package DatabaseExplorer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.swing.*;

import org.postgresql.jdbc.PgDatabaseMetaData;


public class testConnection {
	public static void main(String[] args) {
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		String address = "jdbc:postgresql://192.168.126.129:5432";
		String options = "/restaurant?user=jdbcuser&password=Pf14NZ3NW3RkS14T1&useSSL=false";
		String tableList = getTables(address+options);
		String table = (String) JOptionPane.showInputDialog(tableList+"\nEine Tabelle eingeben aus der Sie auslesen wollen.");
		try {
			conn = DriverManager.getConnection(address+options);
			String cols =getCols(address+options, table);
			String colToSelect = (String) JOptionPane.showInputDialog(cols+"\nGeben Sie die Reihen ein die Sie ausw?hlen wollen.\nOder * eingeben um alles auszuw?hlen");
			String[] SeletColsArray = colToSelect.split(",");
			
			if(colToSelect.equals("*")) {
				colToSelect = cols.replace('\n', ',');
			}
			
			SeletColsArray = colToSelect.split(",");
			
			
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT "+colToSelect+" FROM "+table);
			int i = 0;
			String returnVal = "";
			for(int j = 0; i < SeletColsArray.length; i++) {
				while(rs.next()) {
					returnVal += rs.getString(SeletColsArray[j])+"\n";
					i++;
				}
			}
			
			JOptionPane.showMessageDialog(null, returnVal, i+"rows", JOptionPane.PLAIN_MESSAGE);
		}catch(SQLException e) {
			//e.printStackTrace();
			System.out.println("SQL Exception: "+e.getMessage());
			System.out.println("SQL State: "+e.getSQLState());
			System.out.println("VendorError: "+e.getErrorCode());
		}
	}
	
	
	
	
	
	public static String getTables(String connStr) {
		Statement stmt = null;
		ResultSet rs = null;
		String returnVal = "";
		try {
			Connection conn = DriverManager.getConnection(connStr);
			
			PgDatabaseMetaData md = (PgDatabaseMetaData) conn.getMetaData();
			rs = md.getTables(null, "public", "%", new String[] {"TABLE"});
			while (rs.next()) {
			  returnVal += rs.getString(3)+"\n";
			}
		} catch (SQLException e) {e.printStackTrace();}
		
		return returnVal;
	}
	
	public static String getCols(String connStr, String table) {
		
		Statement stmt = null;
		ResultSet rs = null;
		String returnVal = "";
		try {
			Connection conn = DriverManager.getConnection(connStr);
			
			PgDatabaseMetaData md = (PgDatabaseMetaData) conn.getMetaData();
			rs = md.getColumns(null, "public", table.toLowerCase(), null);
			while (rs.next()) {
				returnVal += rs.getString("COLUMN_NAME")+"\n";
			}
		} catch (SQLException e) {e.printStackTrace();}
		return returnVal.substring(0, returnVal.length() - 1);
	}
}
