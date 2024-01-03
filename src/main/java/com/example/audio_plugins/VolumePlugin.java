package com.example.audio_plugins;

import javafx.beans.property.SimpleFloatProperty;

public class VolumePlugin implements AudioPlugin {
    private SimpleFloatProperty volume = new SimpleFloatProperty(0.5f);

    public VolumePlugin() {
    }

    public VolumePlugin(double volume) {
        this.volume.setValue(volume);
    }

    public SimpleFloatProperty volumeProperty() {
        return volume;
    }

    @Override
    public void process(float[] sample) {
        var volume = (float) this.volume.get();
        for (var i = 0; i < sample.length; i++) {
            sample[i] *= volume;
        }
    }
}
