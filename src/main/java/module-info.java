module pl.sgnit {
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires plc4j.api;

    opens pl.sgnit to javafx.fxml;
    exports pl.sgnit;
}
