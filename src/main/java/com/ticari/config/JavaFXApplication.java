package com.ticari.config;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFXApplication extends Application {
    
    private ConfigurableApplicationContext applicationContext;
    
    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(com.ticari.CrmApplication.class)
                .run(getParameters().getRaw().toArray(new String[0]));
    }
    
    @Override
    public void start(Stage primaryStage) {
        applicationContext.publishEvent(new StageReadyEvent(primaryStage));
    }
    
    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }
    
    public static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }
        
        public Stage getStage() {
            return (Stage) getSource();
        }
    }
}
