package DatabaseExplorer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;


public class DatabaseExplorer extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage){
        try{
            Parent root = FXMLLoader.load(getClass().getResource("seas.fxml"));

            Scene scene = new Scene(root, 900, 600);

            stage.setTitle("Database Explorer");
            stage.setScene(scene);
            stage.show();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("ERROR");
        }

    }


}
