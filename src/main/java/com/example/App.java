package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main.fxml"));
        Parent parent = fxmlLoader.load();

        scene = new Scene(parent);
        stage.setScene(scene);
        ((MainController) (fxmlLoader.getController())).postInit();
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }

}