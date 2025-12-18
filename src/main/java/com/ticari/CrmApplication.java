package com.ticari;

import com.ticari.config.JavaFXApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrmApplication {
    
    public static void main(String[] args) {
        Application.launch(JavaFXApplication.class, args);
    }
}
