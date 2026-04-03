module eus.ehu {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires com.h2database;
    requires javafx.graphics;

    opens eus.ehu to javafx.fxml;
    exports eus.ehu;

    opens eus.ehu.controllers to javafx.fxml;
    exports eus.ehu.controllers;

    opens eus.ehu.usermodel to javafx.fxml, org.hibernate.orm.core;
    exports eus.ehu.usermodel;

}

