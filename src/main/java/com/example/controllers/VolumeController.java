package com.example.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.example.AudioHost;
import com.example.audio_plugins.VolumePlugin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;

public class VolumeController implements Initializable, AudioPluginController {
    @FXML Slider volumeSlider;

    private VolumePlugin volumePlugin = new VolumePlugin();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            volumePlugin.volumeProperty().set(newValue.floatValue() / 100);
        });
    }

    @Override
    public void postInit() {
        volumeSlider.getScene().getWindow().setOnHidden(event -> {
            audioHost.removePlugin(volumePlugin);
        });
    }

    private AudioHost audioHost;

    @Override
    public void setHost(AudioHost audioHost) {
        this.audioHost = audioHost;
        audioHost.addPlugin(volumePlugin);
    }
    
}
