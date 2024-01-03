package com.example;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.sound.sampled.LineUnavailableException;

import com.example.controllers.AudioPluginController;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainController implements Initializable {
    @FXML
    private Pane parentPane;

    AudioHost audioHost;

    private void addAudioController(String fxmlResourcePath, String title) {
        FXMLLoader fxmlLoader = new FXMLLoader(MainController.class.getResource(fxmlResourcePath));
        Parent parent;
        try {
            parent = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        var scene = new Scene(parent);
        var stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);

        var controller = (AudioPluginController) fxmlLoader.getController();
        controller.postInit();
        controller.setHost(audioHost);

        stage.show();
    }

    @FXML
    private void addAudioSource() {
        addAudioController("audioSource.fxml", "Audio Source");
    }

    @FXML
    private void addVolume() {
        addAudioController("volume.fxml", "Volume");
    }

     @FXML
    private void addDelay() {
        addAudioController("delay.fxml", "Delay");
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        try {
            audioHost = new AudioHost();
            var thread = new Thread(audioHost);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
            // audioHost.addPlugin(new AudioSourcePlugin(wavFile));
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            Platform.exit();
            return;
        }
    }

    public void postInit() {
        parentPane.getScene().getWindow().setOnHidden(event -> {
            audioHost.stopAudioHost();
            Platform.exit();
        });
    }
}
