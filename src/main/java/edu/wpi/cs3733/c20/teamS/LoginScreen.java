package edu.wpi.cs3733.c20.teamS;

import edu.wpi.cs3733.c20.teamS.app.DialogResult;
import edu.wpi.cs3733.c20.teamS.twoFactor.TwoFactorScreen;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Set;

public class LoginScreen{
    private final Stage stage;
    private final Stage toPass;
    private final Scene scene;

    private LoginScreen(Stage mainStage) {
        this.stage = new Stage();
        this.toPass = mainStage;

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/loginScreen.fxml"));
        loader.setControllerFactory(e -> {
            loginScreenController cont = new loginScreenController();
            cont.dialogCompleted().subscribe(
                    next -> {
                        if(next.result()== DialogResult.OK){
                            //Intercept here
                            TwoFactorScreen mes = new TwoFactorScreen(toPass, next.value());
                        }
                        this.stage.close();
            });
            return cont;
        });

        try {
            Parent root = loader.load();
            scene = new Scene(root);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    private void show() {
        stage.setScene(scene);
        Settings.openWindows.add(this.stage);
        stage.initStyle(StageStyle.UNDECORATED);
        BaseScreen.puggy.register(scene, Event.ANY);
        stage.show();
    }

    public static void showDialog(Stage mainScreen) {
        LoginScreen screen = new LoginScreen(mainScreen);
        screen.show();
    }
}

