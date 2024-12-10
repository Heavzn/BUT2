module src {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.opencsv;
    requires java.sql;
    requires javafx.media;
    requires jdk.compiler;
    requires org.fxyz3d.core;
    requires org.json;

    opens model to javafx.fxml, com.opencsv;
    exports model;
    exports view;
    opens view to com.opencsv, javafx.fxml;
}
