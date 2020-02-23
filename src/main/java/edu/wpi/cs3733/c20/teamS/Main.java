package edu.wpi.cs3733.c20.teamS;

import edu.wpi.cs3733.c20.teamS.database.DatabaseController;
import edu.wpi.cs3733.c20.teamS.pathfinding.AStar;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

  public void start(Stage primaryStage) {
    DatabaseController dbc = new DatabaseController();
    dbc.importStartUpData();

    //Mailer.sendMail(null);
    new MainToLoginScreen(primaryStage, new AStar());
//    MapEditingScreen test = new MapEditingScreen(primaryStage,
//            new Employee(17, "Bob", AccessLevel.ADMIN));

  }

  public static void main(String[] args) {
    App.launch();
  }
}
