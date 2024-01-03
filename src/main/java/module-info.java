module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires transitive java.desktop;
    requires transitive javafx.graphics;
    requires transitive javafx.base;

    opens com.example to javafx.fxml;
    opens com.example.controllers to javafx.fxml;
    exports com.example;
    exports com.example.audio_plugins;
    exports com.example.controllers;
}
