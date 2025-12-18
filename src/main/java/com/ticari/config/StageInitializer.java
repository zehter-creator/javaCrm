package com.ticari.config;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageInitializer implements ApplicationListener<JavaFXApplication.StageReadyEvent> {
    
    private final SpringFXMLLoader fxmlLoader;
    
    public StageInitializer(SpringFXMLLoader fxmlLoader) {
        this.fxmlLoader = fxmlLoader;
    }
    
    @Override
    public void onApplicationEvent(JavaFXApplication.StageReadyEvent event) {
        try {
            Stage stage = event.getStage();
            var root = fxmlLoader.load("/com/ticari/cariler.fxml");
            
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.setTitle("Ticari CRM - Cari Hesap Yönetimi");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
