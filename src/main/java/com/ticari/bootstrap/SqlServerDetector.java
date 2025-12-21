package com.ticari.bootstrap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

public class SqlServerDetector {
    
    private static final String[] SQLSERVER_SERVICE_NAMES = {
        "MSSQL$SQLEXPRESS",
        "MSSQLSERVER",
        "SQLServerExpress"
    };
    
    public static class DetectionResult {
        public boolean isInstalled;
        public boolean isRunning;
        public boolean canConnect;
        public String serviceName;
        public String errorMessage;
        public List<String> installedInstances = new ArrayList<>();
        
        @Override
        public String toString() {
            return String.format("SQL Server Detection: installed=%s, running=%s, canConnect=%s, service=%s, instances=%s",
                isInstalled, isRunning, canConnect, serviceName, installedInstances);
        }
    }
    
    public static DetectionResult detectSqlServer() {
        DetectionResult result = new DetectionResult();
        
        result.installedInstances = getInstalledInstances();
        result.isInstalled = !result.installedInstances.isEmpty();
        
        if (result.isInstalled) {
            result.serviceName = findRunningService();
            result.isRunning = result.serviceName != null;
            
            if (result.isRunning) {
                result.canConnect = testConnection();
            }
        }
        
        return result;
    }
    
    private static List<String> getInstalledInstances() {
        List<String> instances = new ArrayList<>();
        
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-Command",
                "Get-Service | Where-Object {$_.Name -like 'MSSQL*' -or $_.Name -like 'SQLServer*'} | Select-Object -ExpandProperty Name"
            );
            
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        instances.add(trimmed);
                    }
                }
            }
            
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Error detecting SQL Server instances: " + e.getMessage());
        }
        
        return instances;
    }
    
    private static String findRunningService() {
        for (String serviceName : SQLSERVER_SERVICE_NAMES) {
            if (isServiceRunning(serviceName)) {
                return serviceName;
            }
        }
        
        List<String> installed = getInstalledInstances();
        for (String serviceName : installed) {
            if (isServiceRunning(serviceName)) {
                return serviceName;
            }
        }
        
        return null;
    }
    
    public static boolean isServiceRunning(String serviceName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "sc", "query", serviceName
            );
            
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("RUNNING")) {
                        return true;
                    }
                }
            }
            
            process.waitFor();
        } catch (Exception e) {
            // Service might not exist
        }
        
        return false;
    }
    
    public static boolean startService(String serviceName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "net", "start", serviceName
            );
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                Thread.sleep(3000);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error starting SQL Server service: " + e.getMessage());
        }
        
        return false;
    }
    
    private static boolean testConnection() {
        String[] connectionStrings = {
            "jdbc:sqlserver://localhost:1433;encrypt=true;trustServerCertificate=true;loginTimeout=5",
            "jdbc:sqlserver://localhost\\SQLEXPRESS;encrypt=true;trustServerCertificate=true;loginTimeout=5"
        };
        
        for (String url : connectionStrings) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                
                try (Connection conn = DriverManager.getConnection(url, "sa", "YourPassword123!")) {
                    return true;
                } catch (Exception e) {
                    // Try next connection string
                }
            } catch (ClassNotFoundException e) {
                System.err.println("SQL Server JDBC driver not found");
                return false;
            }
        }
        
        return false;
    }
    
    public static boolean waitForServiceStart(String serviceName, int maxWaitSeconds) {
        int waited = 0;
        while (waited < maxWaitSeconds) {
            if (isServiceRunning(serviceName)) {
                return true;
            }
            
            try {
                Thread.sleep(1000);
                waited++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        return false;
    }
}
