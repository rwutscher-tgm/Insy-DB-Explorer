package DatabaseExplorer;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.postgresql.jdbc.PgDatabaseMetaData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class WindowController {

    //PSQL Variables
    @FXML
    private TextField psqlIP;

    @FXML
    private TextField psqlUser;

    @FXML
    private TextField psqlPassword;

    @FXML
    private Label psqlErrorMessage;

    @FXML
    private TreeView psqlDatabaseTree;

    @FXML
    private TableView psqlMainTable;

    @FXML
    private TextArea psqlQueryText;

    @FXML
    private ChoiceBox<String> psqlDatabaseSelector;

    //MYSQL Variables
    @FXML
    private TextField mySqlIP;

    @FXML
    private TextField mySqlUser;

    @FXML
    private TextField mySqlPassword;

    private String ip;
    private String userName;
    private String password;

    private Properties connProps;

    @FXML
    private void connectPostgres() {
        this.ip = psqlIP.getText();
        this.userName = psqlUser.getText();
        this.password = psqlPassword.getText();

        this.connProps = new Properties();
        this.connProps.put("user", this.userName);
        this.connProps.put("password", this.password);
        this.connProps.put("useSSL", "false");

        if (!userName.equals("") && !password.equals("") && !ip.equals("")) {
            ArrayList<TreeItem> databases = psqlGetDatabases();
            this.psqlDatabaseSelector.getItems().addAll(convertTreeItem(databases));

            TreeItem root = new TreeItem();
            root.setExpanded(true);
            root.getChildren().addAll(databases);

            psqlDatabaseTree.setRoot(root);
            psqlDatabaseTree.setShowRoot(false);
            psqlDatabaseTree.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> {
                if (newValue != null) {
                    if (((TreeItem) newValue).getChildren().size() == 0) {
                        String database = (String) ((TreeItem) newValue).getParent().getValue();
                        String table = (String) ((TreeItem) newValue).getValue();
                        this.psqlDisplayTable(database, table);
                    }
                }
            });

        } else {
            System.out.println("Please Enter Data");
        }
    }

    @FXML
    private void psqlSendQuery(){
        String database = psqlDatabaseSelector.getValue();
        String query = psqlQueryText.getText().replace("\n", " ");

        ArrayList<String> titles = new ArrayList<>();
        ArrayList<ArrayList<String>> data = new ArrayList();

        try {

            Connection conn = DriverManager.getConnection("jdbc:postgresql://" + ip + ":5432/" + database, connProps);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int colnumber = rs.getMetaData().getColumnCount();

            for(int i = 1; i < colnumber; i++){
                titles.add(rs.getMetaData().getColumnName(i));
            }
            while (rs.next()) {
                ArrayList<String> row = new ArrayList<String>();
                for (int i = 1; i <= colnumber; i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        psqlDisplayData(titles, data);
    }


    /*
        PSQL - Display Functions
     */
    public void psqlDisplayTable(String database, String table){

        ArrayList columnTitles = psqlGetCols(database, table);
        ArrayList<ArrayList<String>> columnData = psqlGetTable(database, table);

        this.psqlDisplayData(columnTitles,columnData);
    }

    public void psqlDisplayData(ArrayList columnTitles, ArrayList<ArrayList<String>> columnData){
        psqlMainTable.getColumns().clear();
        psqlMainTable.getItems().clear();

        for(int i = 0; i < columnTitles.size(); i++){
            TableColumn<List<String>, String> col = new TableColumn((String) columnTitles.get(i));
            int colIndex = i;
            col.setCellValueFactory(data ->{
                List<String> rowValues = data.getValue();
                String cellValue;
                if(colIndex < rowValues.size()){
                    cellValue = rowValues.get(colIndex);
                }else{
                    cellValue = "";
                }
                return new ReadOnlyStringWrapper(cellValue);
            });
            psqlMainTable.getColumns().add(col);
        }
        for(int i = 0; i < columnData.size(); i++){
            ArrayList entry = new ArrayList();
            for(int j = 0; j < columnData.get(i).size(); j++){
                entry.add(columnData.get(i).get(j));
            }
            psqlMainTable.getItems().add(entry);
        }
    }


    /*
        PSQL - Querying Functions
     */
    public ArrayList<TreeItem> psqlGetDatabases() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://" + this.ip + ":5432/postgres", this.connProps);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT datname FROM pg_database WHERE datistemplate = false;");

            ArrayList<TreeItem> databaseList = new ArrayList<>();
            while (rs.next()) {
                TreeItem database = new TreeItem(rs.getString(1));
                database.getChildren().addAll(psqlGetTables(rs.getString(1)));
                databaseList.add(database);
            }
            return databaseList;
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            return null;
        }
    }

    public ArrayList<DBTableTreeItem> psqlGetTables(String database) {
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<DBTableTreeItem> tables = new ArrayList();
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://" + this.ip + ":5432/" + database, this.connProps);

            PgDatabaseMetaData md = (PgDatabaseMetaData) conn.getMetaData();
            rs = md.getTables(null, "public", "%", new String[]{"TABLE"});

            while (rs.next()) {
                DBTableTreeItem<String> table = new DBTableTreeItem(rs.getString(3));
                tables.add(table);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tables;
    }

    public ArrayList<ArrayList<String>> psqlGetTable(String database, String table) {
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<ArrayList<String>> tables = new ArrayList();
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://" + this.ip + ":5432/" + database, this.connProps);

            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM " + table);

            int colnumber = rs.getMetaData().getColumnCount();

            while (rs.next()) {
                ArrayList<String> row = new ArrayList<String>();
                for (int i = 1; i <= colnumber; i++) {
                    row.add(rs.getString(i));
                }
                tables.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tables;
    }

    public ArrayList<String> psqlGetCols(String database, String table) {

        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<String> returnVal = new ArrayList<String>();
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://" + this.ip + ":5432/" + database, this.connProps);

            PgDatabaseMetaData md = (PgDatabaseMetaData) conn.getMetaData();
            rs = md.getColumns(null, "public", table.toLowerCase(), null);
            while (rs.next()) {
                returnVal.add(rs.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnVal;
    }



    public static ArrayList<String> convertTreeItem(ArrayList<TreeItem> input){
        ArrayList<String> output = new ArrayList<>();

        for(int i = 0; i < input.size(); i++){
            output.add((String)input.get(i).getValue());
        }

        return output;
    }

    public abstract class AbstractTreeItem <E> extends TreeItem <String>{
        public abstract ContextMenu getMenu();
    }

    public class DBTableTreeItem <E> extends AbstractTreeItem <E>{

        public DBTableTreeItem(String name){
            this.setValue(name);
        }

        @Override
        public ContextMenu getMenu(){
            MenuItem it1 = new MenuItem("Insert");
            MenuItem it2 = new MenuItem("Drop");

            it1.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("Insert");
                }
            });

            it2.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("Drop");
                }
            });

            return new ContextMenu(it1,it2);
        }
    }
}