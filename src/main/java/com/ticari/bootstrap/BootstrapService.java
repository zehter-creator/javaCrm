package com.ticari.bootstrap;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

public class BootstrapService {
    
    private Stage splashStage;
    private Label statusLabel;
    private ProgressBar progressBar;
    
    public static class BootstrapResult {
        public boolean success;
        public boolean sqlServerInstalled;
        public boolean sqlServerStarted;
        public boolean databaseInitialized;
        public String errorMessage;
        public String connectionUrl;
        
        @Override
        public String toString() {
            return String.format("Bootstrap: success=%s, sqlInstalled=%s, sqlStarted=%s, dbInit=%s",
                success, sqlServerInstalled, sqlServerStarted, databaseInitialized);
        }
    }
    
    public BootstrapResult performBootstrap(boolean showUI) {
        BootstrapResult result = new BootstrapResult();
        
        try {
            if (showUI) {
                Platform.runLater(() -> showSplashScreen());
                Thread.sleep(500);
            }
            
            updateStatus("Checking SQL Server installation...", 0.1);
            SqlServerDetector.DetectionResult detection = SqlServerDetector.detectSqlServer();
            System.out.println(detection);
            
            if (!detection.isInstalled) {
                updateStatus("SQL Server Express not found. Installation required.", 0.2);
                result.errorMessage = "SQL Server Express not installed. Please run the installer script or install manually.";
                
                boolean shouldInstall = promptForInstallation();
                if (shouldInstall) {
                    updateStatus("Installing SQL Server Express...", 0.3);
                    boolean installed = installSqlServerExpress();
                    result.sqlServerInstalled = installed;
                    
                    if (!installed) {
                        result.errorMessage = "SQL Server Express installation failed. Please install manually.";
                        return result;
                    }
                    
                    updateStatus("Waiting for SQL Server to start...", 0.5);
                    Thread.sleep(10000);
                    
                    detection = SqlServerDetector.detectSqlServer();
                } else {
                    return result;
                }
            } else {
                result.sqlServerInstalled = true;
            }
            
            if (!detection.isRunning) {
                updateStatus("Starting SQL Server service...", 0.4);
                String serviceName = detection.serviceName != null ? detection.serviceName : "MSSQL$SQLEXPRESS";
                boolean started = SqlServerDetector.startService(serviceName);
                
                if (started) {
                    updateStatus("Waiting for SQL Server to be ready...", 0.5);
                    boolean ready = SqlServerDetector.waitForServiceStart(serviceName, 30);
                    result.sqlServerStarted = ready;
                    
                    if (!ready) {
                        result.errorMessage = "SQL Server service started but not responding";
                        return result;
                    }
                } else {
                    result.errorMessage = "Failed to start SQL Server service: " + serviceName;
                    return result;
                }
            } else {
                result.sqlServerStarted = true;
            }
            
            updateStatus("Initializing database...", 0.7);
            DatabaseInitializer.InitializationResult dbInit = DatabaseInitializer.initializeDatabase();
            System.out.println(dbInit);
            result.databaseInitialized = dbInit.success;
            
            if (!dbInit.success) {
                result.errorMessage = "Database initialization failed: " + dbInit.errorMessage;
                return result;
            }
            
            updateStatus("Detecting connection URL...", 0.9);
            String[] connInfo = DatabaseInitializer.detectConnectionUrl();
            result.connectionUrl = connInfo[0];
            
            updateStatus("Bootstrap complete!", 1.0);
            result.success = true;
            
            if (showUI) {
                Thread.sleep(1000);
                Platform.runLater(() -> closeSplashScreen());
            }
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = "Bootstrap error: " + e.getMessage();
            e.printStackTrace();
        }
        
        return result;
    }
    
    private void showSplashScreen() {
        splashStage = new Stage(StageStyle.UNDECORATED);
        splashStage.setTitle("Application Bootstrap");
        splashStage.setAlwaysOnTop(true);
        
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-padding: 40; -fx-background-color: #f0f0f0; -fx-border-color: #333; -fx-border-width: 2;");
        
        Label titleLabel = new Label("Ticari CRM - Initializing...");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        statusLabel = new Label("Starting bootstrap process...");
        statusLabel.setStyle("-fx-font-size: 12px;");
        
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        
        vbox.getChildren().addAll(titleLabel, statusLabel, progressBar);
        
        Scene scene = new Scene(vbox, 500, 200);
        splashStage.setScene(scene);
        splashStage.show();
        splashStage.centerOnScreen();
    }
    
    private void closeSplashScreen() {
        if (splashStage != null) {
            splashStage.close();
        }
    }
    
    private void updateStatus(String message, double progress) {
        System.out.println("[Bootstrap] " + message);
        
        if (statusLabel != null && progressBar != null) {
            Platform.runLater(() -> {
                statusLabel.setText(message);
                progressBar.setProgress(progress);
            });
        }
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private boolean promptForInstallation() {
        File installerScript = new File("bootstrap/install-sqlserver.ps1");
        return installerScript.exists();
    }
    
    private boolean installSqlServerExpress() {
        try {
            File installerScript = new File("bootstrap/install-sqlserver.ps1");
            if (!installerScript.exists()) {
                System.err.println("SQL Server installer script not found at: " + installerScript.getAbsolutePath());
                return false;
            }
            
            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-ExecutionPolicy", "Bypass",
                "-File", installerScript.getAbsolutePath()
            );
            
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            return exitCode == 0;
            
        } catch (Exception e) {
            System.err.println("Error installing SQL Server Express: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
