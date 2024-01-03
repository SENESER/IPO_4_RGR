package com.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat.Encoding;

import com.example.audio_plugins.AudioPlugin;
import com.example.audio_plugins.AudioPluginWithTickListener;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AudioHost extends Thread {
    private SourceDataLine line;

    public static final AudioFormat targetFormat = new AudioFormat(
            Encoding.PCM_SIGNED,
            48000f,
            16,
            2,
            4, // 2 channels * 2 bytes per sample (16 bits)
            48000,
            false);

    public static final int sampleFramesCount = (int) targetFormat.getFrameRate() / 4;
    public static final int sampleLength = sampleFramesCount * targetFormat.getChannels();
    public static final int sampleBufferSize = sampleFramesCount * targetFormat.getFrameSize();

    private static final byte[] emptyBuffer = new byte[sampleBufferSize];

    private ObservableList<AudioPlugin> pluginList = FXCollections.observableArrayList();
    private final ReentrantLock pluginListLock = new ReentrantLock();

    public void removePlugin(AudioPlugin plugin) {
        pluginListLock.lock();
        try {
            pluginList.remove(plugin);

            if (plugin instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) plugin).close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            pluginListLock.unlock();
        }
    }

    public void addPlugin(AudioPlugin plugin) {
        if (plugin instanceof AudioPluginWithTickListener) {
            ((AudioPluginWithTickListener) plugin).setTickProperty(tickProperty());
        }
        pluginListLock.lock();
        try {
            pluginList.add(plugin);
        } finally {
            pluginListLock.unlock();
        }
    }

    private long tick = 0;
    private SimpleLongProperty tickProp = new SimpleLongProperty(tick);
    private ReadOnlyLongProperty tickPropRO = ReadOnlyLongProperty.readOnlyLongProperty(tickProp);

    public ReadOnlyLongProperty tickProperty() {
        return tickPropRO;
    }

    private AnimationTimer animationTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (tick == tickProp.get())
                return;

            tickProp.set(tick);
        }
    };

    public AudioHost() throws LineUnavailableException {
        animationTimer.start();
        var info = new DataLine.Info(
                SourceDataLine.class, targetFormat,
                48000);

        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(targetFormat);
        line.start();
    }

    private boolean running = true;

    public void stopAudioHost() {
        running = false;
        animationTimer.stop();
    }

    @Override
    public void run() {
        var pluginList = new ArrayList<AudioPlugin>();
        var frames = new float[sampleLength];
        var buffer = ByteBuffer.allocate(sampleBufferSize).order(ByteOrder.LITTLE_ENDIAN);

        while (running) {

            pluginListLock.lock();
            try {
                pluginList.addAll(this.pluginList);
            } finally {
                pluginListLock.unlock();
            }

            if (pluginList.size() == 0) {
                line.write(emptyBuffer, 0, sampleBufferSize);
                continue;
            }

            for (AudioPlugin plugin : pluginList) {
                plugin.process(frames);
            }

            for (float frame : frames) {
                buffer.putShort(
                        (short) Math.round(Math.clamp(frame * Short.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE)));
            }

            line.write(buffer.array(), 0, sampleBufferSize);

            tick++;

            buffer.clear();
            pluginList.clear();
            for (var i = 0; i < frames.length; i++) {
                frames[i] = 0f;
            }

            
        }
        line.drain();
        line.close();
    }
}
