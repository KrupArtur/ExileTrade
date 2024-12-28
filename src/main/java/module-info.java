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

    opens com.example.demo1 to javafx.fxml,com.fasterxml.jackson.databind;
    exports com.example.demo1;
    exports com.example.demo1.Controller;
    opens com.example.demo1.Controller to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.example.demo1.Model;
    opens com.example.demo1.Model to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.example.demo1.Model.Json;
    opens com.example.demo1.Model.Json to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.example.demo1.Service;
    opens com.example.demo1.Service to com.fasterxml.jackson.databind, javafx.fxml;
}