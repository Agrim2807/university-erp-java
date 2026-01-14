package edu.univ.erp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors; 


public class DatabaseBackupRestore {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupRestore.class);

    
    private static final String MYSQL_DUMP_COMMAND = "mysqldump";
    private static final String MYSQL_COMMAND = "mysql";
    private static final String BACKUP_DIR = "backups/";

    
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "agrim2006";


    
    public static BackupResult backupDatabase(String host, String port, String username, String password) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFileName = "university_erp_backup_" + timestamp + ".sql";
        File backupFile = new File(BACKUP_DIR + backupFileName);

        try {
            Files.createDirectories(Paths.get(BACKUP_DIR));

            ProcessBuilder processBuilder = new ProcessBuilder(
                MYSQL_DUMP_COMMAND,
                "-h", host,
                "-P", port,
                "-u", username,
                "-p" + password,
                "--routines",
                "--triggers",
                "--single-transaction",
                
                "--databases", "university_auth", "university_erp",
                
                "--result-file=" + backupFile.getAbsolutePath()
            );

            
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            
            
            String output = readProcessOutput(process.getInputStream());
            
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Database backup successful: {}", backupFile.getAbsolutePath());
                return new BackupResult(true, backupFile.getAbsolutePath(),
                                       "Backup completed successfully", backupFile.length());
            } else {
                
                logger.error("Database backup failed with exit code {}: {}", exitCode, output);
                return new BackupResult(false, null, "Backup failed: " + output, 0);
            }

        } catch (Exception e) {
            logger.error("Error during database backup", e);
            return new BackupResult(false, null, "Backup error: " + e.getMessage(), 0);
        }
    }

    
    public static RestoreResult restoreDatabase(String host, String port, String username,
                                               String password, String backupFilePath) {
        File backupFile = new File(backupFilePath);

        if (!backupFile.exists()) {
            return new RestoreResult(false, "Backup file not found: " + backupFilePath);
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                MYSQL_COMMAND,
                "-h", host,
                "-P", port,
                "-u", username,
                "-p" + password
                
            );

            processBuilder.redirectInput(ProcessBuilder.Redirect.from(backupFile));
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            String output = readProcessOutput(process.getInputStream());
            
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Database restore successful from: {}", backupFilePath);
                return new RestoreResult(true, "Restore completed successfully");
            } else {
                logger.error("Database restore failed with exit code {}: {}", exitCode, output);
                return new RestoreResult(false, "Restore failed: " + output);
            }

        } catch (Exception e) {
            logger.error("Error during database restore", e);
            return new RestoreResult(false, "Restore error: " + e.getMessage());
        }
    }

    
    public static File[] listBackups() {
        File backupDir = new File(BACKUP_DIR);

        if (!backupDir.exists()) {
            return new File[0];
        }

        File[] backups = backupDir.listFiles((dir, name) ->
            name.startsWith("university_erp_backup_") && name.endsWith(".sql"));

        return backups != null ? backups : new File[0];
    }

    
    public static boolean deleteBackup(String backupFilePath) {
        try {
            return Files.deleteIfExists(Paths.get(backupFilePath));
        } catch (IOException e) {
            logger.error("Error deleting backup file: {}", backupFilePath, e);
            return false;
        }
    }

    
    public static BackupResult quickBackup() {
        return backupDatabase(DB_HOST, DB_PORT, DB_USER, DB_PASS);
    }

    
    public static RestoreResult quickRestore(String backupFilePath) {
        return restoreDatabase(DB_HOST, DB_PORT, DB_USER, DB_PASS, backupFilePath);
    }

    
    private static String readProcessOutput(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            logger.error("Error reading process output", e);
            return "Error reading process output: " + e.getMessage();
        }
    }

    
    public static class BackupResult {
        private final boolean success;
        private final String filePath;
        private final String message;
        private final long fileSize;

        public BackupResult(boolean success, String filePath, String message, long fileSize) {
            this.success = success;
            this.filePath = filePath;
            this.message = message;
            this.fileSize = fileSize;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getMessage() {
            return message;
        }

        public long getFileSize() {
            return fileSize;
        }

        public String getFileSizeFormatted() {
            if (fileSize < 1024) {
                return fileSize + " B";
            } else if (fileSize < 1024 * 1024) {
                return String.format("%.2f KB", fileSize / 1024.0);
            } else {
                return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
            }
        }
    }

    
    public static class RestoreResult {
        private final boolean success;
        private final String message;

        public RestoreResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}