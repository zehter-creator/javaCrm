package com.ticari.config;

import com.ticari.bootstrap.BootstrapService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFXApplication extends Application {
    
    private ConfigurableApplicationContext applicationContext;
    private boolean bootstrapFailed = false;
    
    @Override
    public void init() {
        System.out.println("=== Starting Application Bootstrap ===");
        
        BootstrapService bootstrapService = new BootstrapService();
        BootstrapService.BootstrapResult result = bootstrapService.performBootstrap(true);
        
        System.out.println("Bootstrap result: " + result);
        
        if (!result.success) {
            System.err.println("Bootstrap failed: " + result.errorMessage);
            bootstrapFailed = true;
            
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Application Initialization Failed");
                alert.setHeaderText("Cannot start application");
                alert.setContentText(result.errorMessage + "\n\n" +
                    "Please ensure:\n" +
                    "1. SQL Server Express is installed\n" +
                    "2. SQL Server service is running\n" +
                    "3. SA password is set to: YourPassword123!\n\n" +
                    "For automatic installation, run: bootstrap/install-sqlserver.ps1");
                
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        Platform.exit();
                        System.exit(1);
                    }
                });
            });
            
            return;
        }
        
        System.out.println("=== Bootstrap Successful - Starting Spring Boot ===");
        
        try {
            if (result.connectionUrl != null && !result.connectionUrl.isEmpty()) {
                System.setProperty("spring.datasource.url", result.connectionUrl);
            }
            
            applicationContext = new SpringApplicationBuilder(com.ticari.CrmApplication.class)
                    .run(getParameters().getRaw().toArray(new String[0]));
                    
        } catch (Exception e) {
            System.err.println("Spring Boot initialization failed: " + e.getMessage());
            e.printStackTrace();
            bootstrapFailed = true;
            
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Application Startup Failed");
                alert.setHeaderText("Spring Boot failed to start");
                alert.setContentText("Error: " + e.getMessage() + "\n\nCheck logs for details.");
                alert.showAndWait();
                Platform.exit();
                System.exit(1);
            });
        }
    }
    
    @Override
    public void start(Stage primaryStage) {
        if (bootstrapFailed) {
            Platform.exit();
            return;
        }
        
        applicationContext.publishEvent(new StageReadyEvent(primaryStage));
    }
    
    @Override
    public void stop() {
        if (applicationContext != null) {
            applicationContext.close();
        }
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
