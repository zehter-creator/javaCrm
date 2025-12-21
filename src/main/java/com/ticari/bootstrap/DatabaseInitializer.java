package com.ticari.bootstrap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseInitializer {
    
    private static final String DATABASE_NAME = "TicariDB";
    private static final String SA_PASSWORD = "YourPassword123!";
    
    public static class InitializationResult {
        public boolean success;
        public boolean databaseExisted;
        public boolean databaseCreated;
        public String errorMessage;
        
        @Override
        public String toString() {
            return String.format("Database Init: success=%s, existed=%s, created=%s, error=%s",
                success, databaseExisted, databaseCreated, errorMessage);
        }
    }
    
    public static InitializationResult initializeDatabase() {
        InitializationResult result = new InitializationResult();
        
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            if (databaseExists()) {
                result.success = true;
                result.databaseExisted = true;
                System.out.println("Database '" + DATABASE_NAME + "' already exists");
                return result;
            }
            
            boolean created = createDatabase();
            result.success = created;
            result.databaseCreated = created;
            
            if (created) {
                System.out.println("Database '" + DATABASE_NAME + "' created successfully");
            } else {
                result.errorMessage = "Failed to create database";
                System.err.println("Failed to create database '" + DATABASE_NAME + "'");
            }
            
        } catch (ClassNotFoundException e) {
            result.success = false;
            result.errorMessage = "SQL Server JDBC driver not found: " + e.getMessage();
            System.err.println(result.errorMessage);
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = "Error during database initialization: " + e.getMessage();
            System.err.println(result.errorMessage);
            e.printStackTrace();
        }
        
        return result;
    }
    
    private static boolean databaseExists() {
        String[] connectionUrls = {
            "jdbc:sqlserver://localhost:1433;encrypt=true;trustServerCertificate=true;loginTimeout=10",
            "jdbc:sqlserver://localhost\\SQLEXPRESS;encrypt=true;trustServerCertificate=true;loginTimeout=10"
        };
        
        for (String masterUrl : connectionUrls) {
            try (Connection conn = DriverManager.getConnection(masterUrl, "sa", SA_PASSWORD);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT database_id FROM sys.databases WHERE name = '" + DATABASE_NAME + "'")) {
                
                if (rs.next()) {
                    return true;
                }
                
            } catch (Exception e) {
                // Try next URL
            }
        }
        
        return false;
    }
    
    private static boolean createDatabase() {
        String[] connectionUrls = {
            "jdbc:sqlserver://localhost:1433;encrypt=true;trustServerCertificate=true;loginTimeout=10",
            "jdbc:sqlserver://localhost\\SQLEXPRESS;encrypt=true;trustServerCertificate=true;loginTimeout=10"
        };
        
        for (String masterUrl : connectionUrls) {
            try (Connection conn = DriverManager.getConnection(masterUrl, "sa", SA_PASSWORD);
                 Statement stmt = conn.createStatement()) {
                
                String createDbSql = "CREATE DATABASE [" + DATABASE_NAME + "]";
                stmt.executeUpdate(createDbSql);
                
                System.out.println("Executed: " + createDbSql);
                
                Thread.sleep(2000);
                
                return databaseExists();
                
            } catch (Exception e) {
                System.err.println("Attempt to create database failed with URL: " + masterUrl);
                System.err.println("Error: " + e.getMessage());
            }
        }
        
        return false;
    }
    
    public static String[] detectConnectionUrl() {
        String[] urls = {
            "jdbc:sqlserver://localhost:1433;databaseName=" + DATABASE_NAME + ";encrypt=true;trustServerCertificate=true",
            "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=" + DATABASE_NAME + ";encrypt=true;trustServerCertificate=true"
        };
        
        for (String url : urls) {
            try (Connection conn = DriverManager.getConnection(url, "sa", SA_PASSWORD)) {
                System.out.println("Successfully connected using URL: " + url);
                return new String[] { url, "sa", SA_PASSWORD };
            } catch (Exception e) {
                // Try next URL
            }
        }
        
        return new String[] { urls[0], "sa", SA_PASSWORD };
    }
}
