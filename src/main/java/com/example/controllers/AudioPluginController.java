package com.example.controllers;

import com.example.AudioHost;

public interface AudioPluginController {
    public void postInit();
    public void setHost(AudioHost audioHost);
}
