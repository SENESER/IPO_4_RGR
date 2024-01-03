package com.example.audio_plugins;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.example.AudioHost;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;

public class AudioSourcePlugin implements AutoCloseable, AudioPlugin, AudioPluginWithTickListener {
    private SimpleObjectProperty<float[]> frames = new SimpleObjectProperty<>();

    private SimpleBooleanProperty play = new SimpleBooleanProperty(true);
    public SimpleBooleanProperty playProperty() {
        return play;
    }

    private int currentFrame = 0;
    private SimpleDoubleProperty currentPosition = new SimpleDoubleProperty(0);
    private ReadOnlyDoubleProperty currentPositionRO = ReadOnlyDoubleProperty.readOnlyDoubleProperty(currentPosition);

    private SimpleDoubleProperty endPosition = new SimpleDoubleProperty(0);
    private ReadOnlyDoubleProperty endPositionRO = ReadOnlyDoubleProperty.readOnlyDoubleProperty(endPosition);

    private SimpleFloatProperty volume = new SimpleFloatProperty(0.5f);
    public SimpleFloatProperty volumeProperty() {
        return volume;
    }

    private BooleanBinding disabled = Bindings.createBooleanBinding(() -> frames.get() == null, frames);
    public ObservableBooleanValue disabledProperty() {
        return disabled;
    }

    public AudioSourcePlugin() {
        frames.addListener((observable, oldValue, newValue) -> {
            currentFrame = 0;
            currentPosition.set(0);

            var channels = AudioHost.targetFormat.getChannels();
            var frameRate = AudioHost.targetFormat.getFrameRate();
            endPosition.set(newValue == null ? 0 : (double) (newValue.length) / channels / frameRate);
            return;
        });
    }

    private final ReentrantLock currentFrameLock = new ReentrantLock();

    private static float[] toFloat(ShortBuffer buffer) {
        float mult = 1f / Short.MAX_VALUE;
        float[] arr = new float[buffer.capacity()];
        buffer.position(0);
        for (int i = 0; i < arr.length; i++) {
            arr[i] = buffer.get() * mult;
        }
        return arr;
    }

    private ReadOnlyLongProperty tickProperty;
    private InvalidationListener tickPropertyListener = observable -> {
        var channels = AudioHost.targetFormat.getChannels();
        var frameRate = AudioHost.targetFormat.getFrameRate();
        currentPosition.set((double) currentFrame / channels / frameRate);
    };

    public void setTickProperty(ReadOnlyLongProperty tickProperty) {
        this.tickProperty = tickProperty;
        tickProperty.addListener(tickPropertyListener);
    }

    public void setFile(File file) throws UnsupportedAudioFileException, IOException {
        var srcStream = AudioSystem.getAudioInputStream(file);
        var stream = AudioSystem.getAudioInputStream(AudioHost.targetFormat, srcStream);
        var streamBytes = stream.readAllBytes();
        var frameBuffer = ByteBuffer
                .wrap(streamBytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer();

        this.frames.set(toFloat(frameBuffer));
    }

    public ReadOnlyDoubleProperty currentPositionProperty() {
        return currentPositionRO;
    }

    public ReadOnlyDoubleProperty endPositionProperty() {
        return endPositionRO;
    }

    public void setPositionSeconds(double seconds) {
        var index = (int) Math.round(seconds * AudioHost.targetFormat.getFrameRate()) * AudioHost.targetFormat.getChannels();
        currentFrameLock.lock();
        
        try {
            currentFrame = index;
        } finally {
            currentFrameLock.unlock();
        }
    }

    @Override
    public void process(float[] sample) {
        var frames = this.frames.get();
        var volume = this.volume.get();

        if (frames == null || !play.get())
            return;

        currentFrameLock.lock();
        int currentFrame;
        try {
            currentFrame = this.currentFrame;
        } finally {
            currentFrameLock.unlock();
        }

        for (var i = 0; i < sample.length; i++) {
            sample[i] += frames[(currentFrame + i) % frames.length] * volume;
        }

        currentFrameLock.lock();
        try {
            if (this.currentFrame != currentFrame)
                return;
            this.currentFrame += sample.length;
            this.currentFrame %= frames.length;
        } finally {
            currentFrameLock.unlock();
        }

    }

    @Override
    public void close() {
        tickProperty.removeListener(tickPropertyListener);
    }
}
