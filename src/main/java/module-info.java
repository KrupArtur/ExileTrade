module com.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires jnativehook;
    requires jdk.httpserver;
    requires java.net.http;
    requires java.desktop;
    requires org.json;
    requires javafx.web;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.sun.jna.platform;
    requires com.sun.jna;
    requires java.logging;
    requires com.dustinredmond.fxtrayicon;

    opens com.demo.poe to javafx.fxml,com.fasterxml.jackson.databind;
    exports com.demo.poe;
    exports com.demo.poe.Controller;
    opens com.demo.poe.Controller to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.demo.poe.Model;
    opens com.demo.poe.Model to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.demo.poe.Model.Json;
    opens com.demo.poe.Model.Json to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.demo.poe.Service;
    opens com.demo.poe.Service to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.demo.poe.Model.Json.Filters;
    opens com.demo.poe.Model.Json.Filters to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.demo.poe.Model.Json.Stats;
    opens com.demo.poe.Model.Json.Stats to com.fasterxml.jackson.databind, javafx.fxml;
}