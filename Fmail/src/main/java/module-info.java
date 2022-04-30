module com.unito.prog3.fmail {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.unito.prog3.fmail to javafx.fxml;
    exports com.unito.prog3.fmail;

    opens com.unito.prog3.fmail.server to javafx.fxml;
    exports com.unito.prog3.fmail.server;

    opens com.unito.prog3.fmail.client to javafx.fxml;
    exports com.unito.prog3.fmail.client;

    opens com.unito.prog3.fmail.support to javafx.fxml;
    exports com.unito.prog3.fmail.support;

    opens com.unito.prog3.fmail.model to javafx.fxml;
    exports com.unito.prog3.fmail.model;
}