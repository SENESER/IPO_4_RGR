package com.example.audio_plugins;

import com.example.AudioHost;
import com.example.FloatQueue;

import javafx.beans.property.SimpleDoubleProperty;

public class DelayPlugin implements AudioPlugin {

    private SimpleDoubleProperty delay = new SimpleDoubleProperty(0);

    public SimpleDoubleProperty delayProperty() {
        return delay;
    }

    private FloatQueue floatQueue = new FloatQueue();

    @Override
    public void process(float[] sample) {
        var queueSize = (int) Math.round(delay.get() * AudioHost.targetFormat.getFrameRate())
                * AudioHost.targetFormat.getChannels();

        while (floatQueue.size() > queueSize) {
            floatQueue.getFront();
        }

        for (var i = 0; i < sample.length; i++) {
            floatQueue.insert(sample[i]);
            sample[i] += floatQueue.size() > queueSize ? floatQueue.getFront() : 0;
        }
    }

}
