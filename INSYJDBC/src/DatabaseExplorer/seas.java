package DatabaseExplorer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;

import org.postgresql.jdbc.PgDatabaseMetaData;


public class seas {
    public static void main(String[] args) {
        String[] a = getDatabases("192.168.126.129:5432");
        for(int i = 0;i < a.length; i++){
            System.out.println(a[i]);
        }


        DatabaseExplorer b = new DatabaseExplorer();
    }

    public static String[] getDatabases(String ip){
        try {
            Properties connectionProperties = new Properties();
            connectionProperties.put("user", "jdbcuser");
            connectionProperties.put("password", "Pf14NZ3NW3RkS14T1");
            connectionProperties.put("useSSL", "false");
            Connection conn = DriverManager.getConnection("jdbc:postgresql://"+ip+"/postgres",connectionProperties);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT datname FROM pg_database WHERE datistemplate = false;");

            ArrayList<String> databaseList = new ArrayList<>();
            while(rs.next()) {
                databaseList.add(rs.getString(1));
            }
            return databaseList.toArray(new String[0]);
        }catch(SQLException e){
            System.out.println("SQL Exception: "+e.getMessage());
            System.out.println("SQL State: "+e.getSQLState());
            System.out.println("VendorError: "+e.getErrorCode());
            return null;
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
            String tablename = "Table_"+table;
            rs = md.getColumns(null, "public", table.toLowerCase(), null);
            while (rs.next()) {
                returnVal += rs.getString("COLUMN_NAME")+"\n";
            }
        } catch (SQLException e) {e.printStackTrace();}
        return returnVal.substring(0, returnVal.length() - 1);
    }
}
