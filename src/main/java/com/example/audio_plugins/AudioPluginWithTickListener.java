package com.example.audio_plugins;

import javafx.beans.property.ReadOnlyLongProperty;

public interface AudioPluginWithTickListener {
    public void setTickProperty(ReadOnlyLongProperty tickProperty);
}
