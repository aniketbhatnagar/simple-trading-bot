package com.sonar.trading;

import com.sonar.trading.utils.Resources;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static void main( String[] args ) {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Resources.getResourceURL("app.fxml"));

        Scene scene = new Scene(root);

        scene.getStylesheets().add(Resources.getResourcePath("app.css"));

        stage.setTitle("Sonar Trading");
        stage.setScene(scene);
        stage.show();

        AppInitializer appInitializer = new AppInitializer();
        appInitializer.init(stage);

    }
}
