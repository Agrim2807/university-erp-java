package edu.univ.erp.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;


public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    private static HikariDataSource authDataSource;
    private static HikariDataSource erpDataSource;
    
    static {
        initializeDataSources();
    }
    
    private static void initializeDataSources() {
        try {
            
            HikariConfig authConfig = new HikariConfig();
            authConfig.setJdbcUrl("jdbc:mysql://localhost:3306/university_auth");
            authConfig.setUsername("root");
            authConfig.setPassword("agrim2006");
            authConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            
            authConfig.setMaximumPoolSize(10);
            authConfig.setMinimumIdle(2);
            authConfig.setConnectionTimeout(30000);
            authConfig.setIdleTimeout(600000);
            authConfig.setMaxLifetime(1800000);
            authConfig.setPoolName("AuthDB-Pool");
            
            authDataSource = new HikariDataSource(authConfig);
            
            
            HikariConfig erpConfig = new HikariConfig();
            erpConfig.setJdbcUrl("jdbc:mysql://localhost:3306/university_erp");
            erpConfig.setUsername("root");
            erpConfig.setPassword("agrim2006");
            erpConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            erpConfig.setMaximumPoolSize(15);
            erpConfig.setMinimumIdle(3);
            erpConfig.setConnectionTimeout(30000);
            erpConfig.setIdleTimeout(600000);
            erpConfig.setMaxLifetime(1800000);
            erpConfig.setPoolName("ERP-DB-Pool");
            
            erpDataSource = new HikariDataSource(erpConfig);
            
            logger.info("Database connection pools initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pools", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    public static Connection getAuthConnection() throws SQLException {
        return authDataSource.getConnection();
    }
    
    public static Connection getERPConnection() throws SQLException {
        return erpDataSource.getConnection();
    }
    
    public static void closeDataSources() {
        if (authDataSource != null && !authDataSource.isClosed()) {
            authDataSource.close();
        }
        if (erpDataSource != null && !erpDataSource.isClosed()) {
            erpDataSource.close();
        }
        logger.info("Database connection pools closed");
    }
}