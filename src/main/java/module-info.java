module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires com.h2database;
    requires javafx.graphics;

    opens com.example to javafx.fxml;
    exports com.example;

    opens com.example.controllers to javafx.fxml;
    exports com.example.controllers;

    opens com.example.usermodel to javafx.fxml, org.hibernate.orm.core;
    exports com.example.usermodel;

}
