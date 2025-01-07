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
    exports com.demo.poe.Model.POE2.Json;
    opens com.demo.poe.Model.POE2.Json to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.demo.poe.Service;
    opens com.demo.poe.Service to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.demo.poe.Model.POE2.Json.Filters;
    opens com.demo.poe.Model.POE2.Json.Filters to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.demo.poe.Model.POE2.Json.Stats;
    opens com.demo.poe.Model.POE2.Json.Stats to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.demo.poe.Model.Json.Leagues to com.fasterxml.jackson.databind;
    exports com.demo.poe.Service.poe2;
    opens com.demo.poe.Service.poe2 to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.demo.poe.Service.poe;
    opens com.demo.poe.Service.poe to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.demo.poe.Model.POE2;
    opens com.demo.poe.Model.POE2 to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.demo.poe.Model.POE.Json.Stats to com.fasterxml.jackson.databind;
    exports com.demo.poe.Model.POE.Json.Filters to com.fasterxml.jackson.databind;
}