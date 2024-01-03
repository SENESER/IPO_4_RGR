package com.example.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.example.AudioHost;
import com.example.audio_plugins.AudioSourcePlugin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

public class AudioSourceController implements Initializable, AudioPluginController {

    @FXML
    private Pane parentPane;
    @FXML
    private Pane controlsPane;

    @FXML
    private Button openWavButton;

    @FXML
    private Button togglePlayButton;

    @FXML
    private Slider positionSlider;
    @FXML
    private Label positionLabel;
    @FXML
    private Label endLabel;

    @FXML
    private Slider volumeSlider;
    @FXML
    private Label volumeLabel;

    private FileChooser fileChooser = new FileChooser();
    private AudioHost audioHost;
    private AudioSourcePlugin audioSourcePlugin = new AudioSourcePlugin();

    @FXML
    private void openWav() {
        var file = fileChooser.showOpenDialog(parentPane.getScene().getWindow());
        if (file == null)
            return;

        try {
            audioSourcePlugin.setFile(file);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void togglePlay() {
        var play = !audioSourcePlugin.playProperty().get();
        audioSourcePlugin.playProperty().setValue(play);
        togglePlayButton.setText(play ? "⏸": "⏵");
    }

    public void setHost(AudioHost audioHost) {
        this.audioHost = audioHost;
        audioHost.addPlugin(audioSourcePlugin);
    }

    private static String digits(int n) {
        return (n > 9 ? "" : "0") + n;
    }

    private static String formatTime(double seconds) {
        var v = (int) Math.floor(seconds);

        var sec = digits(v % 60);
        var min = digits((v / 60) % 60);
        var hour = digits(v / 3600);

        return hour + ":" + min + ":" + sec;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var wavExtensionFilter = new FileChooser.ExtensionFilter("Wav audio", new String[] { "*.wav", "*.wave" });
        fileChooser.getExtensionFilters().add(wavExtensionFilter);

        togglePlayButton.disableProperty().bind(audioSourcePlugin.disabledProperty());
        positionSlider.disableProperty().bind(audioSourcePlugin.disabledProperty());
        positionLabel.disableProperty().bind(audioSourcePlugin.disabledProperty());
        endLabel.disableProperty().bind(audioSourcePlugin.disabledProperty());
        volumeSlider.disableProperty().bind(audioSourcePlugin.disabledProperty());
        volumeLabel.disableProperty().bind(audioSourcePlugin.disabledProperty());

        audioSourcePlugin.currentPositionProperty().addListener((observable, oldValue, newValue) -> {
            var v = newValue.doubleValue();
            positionSlider.setValue(v);
            positionLabel.setText(formatTime(v));
        });

        positionSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            var pluginPos = audioSourcePlugin.currentPositionProperty().get();
            var sliderPos = newValue.doubleValue();
            if (Math.abs(pluginPos - sliderPos) < 0.2)
                return;
            
            audioSourcePlugin.setPositionSeconds(sliderPos);
        });

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            var volumePc = newValue.floatValue();
            audioSourcePlugin.volumeProperty().set(volumePc / 100f);
            volumeLabel.setText(String.format("%.1f%%", volumePc));
        });

        audioSourcePlugin.endPositionProperty().addListener((observable, oldValue, newValue) -> {
            var v = newValue.doubleValue();
            positionSlider.setMax(v);
            endLabel.setText(formatTime(v));
        });
    }

    public void postInit() {
        parentPane.getScene().getWindow().setOnHidden(event -> {
            audioHost.removePlugin(audioSourcePlugin);
        });
    }

}
