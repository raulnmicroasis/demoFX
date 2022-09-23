module com.example.demofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires org.apache.poi.poi;
    requires annotations;
    requires org.apache.poi.ooxml;

    opens com.example.demofx to javafx.fxml;
    exports com.example.demofx;
}