package com.example.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.example.AudioHost;
import com.example.audio_plugins.DelayPlugin;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;

public class DelayController implements Initializable, AudioPluginController {
    @FXML Slider delaySlider;

    private DelayPlugin delayPlugin = new DelayPlugin();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        delaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            delayPlugin.delayProperty().set(newValue.floatValue());
        });
    }

    @Override
    public void postInit() {
        delaySlider.getScene().getWindow().setOnHidden(event -> {
            audioHost.removePlugin(delayPlugin);
        });
    }

    private AudioHost audioHost;

    @Override
    public void setHost(AudioHost audioHost) {
        this.audioHost = audioHost;
        audioHost.addPlugin(delayPlugin);
    }
    
}
