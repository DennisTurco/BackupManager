package backupmanager.Repositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.Backup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Managers.ExceptionManager;

public class BackupConfigurationRepository extends Repository{
    private static final Logger logger = LoggerFactory.getLogger(BackupConfigurationRepository.class);

    public static void insertBackup(Backup backup) {
        String sql = """
        INSERT INTO
            BackupConfigurations (BackupName, TargetPath, DestinationPath, LastBackupDate, Automatic, NextBackupDate, TimeIntervalBackup, CreationDate, LastUpdateDate, BackupCount, MaxToKeep, Notes)
        VALUES
            (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;
        try (Connection conn = DriverManager.getConnection(getUrlConnection());
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, backup.getName());
            stmt.setString(2, backup.getTargetPath());
            stmt.setString(3, backup.getDestinationPath());
            stmt.setString(4, backup.getLastBackupDate() != null ? backup.getLastBackupDate().toString() : null);
            stmt.setBoolean(5, backup.isAutomatic());
            stmt.setString(6, backup.getNextBackupDate() != null ? backup.getNextBackupDate().toString() : null);
            stmt.setString(7, backup.getTimeIntervalBackup() != null ? backup.getTimeIntervalBackup().toString() : null);
            stmt.setString(8, backup.getCreationDate() != null ? backup.getCreationDate().toString() : LocalDateTime.now().toString());
            stmt.setString(9, backup.getLastUpdateDate() != null ? backup.getLastUpdateDate().toString() : LocalDateTime.now().toString());
            stmt.setInt(10, backup.getCount());
            stmt.setInt(11, backup.getMaxToKeep());
            stmt.setString(12, backup.getNotes());
            stmt.executeUpdate();

            logger.info("Backup inserted succesfully");

        } catch (SQLException e) {
            logger.error("Backup inserting error: " + e.getMessage());
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
    }

    public static void updateBackup(Backup backup) {
        String sql = """
        UPDATE
            BackupConfigurations
        SET
            BackupName = ?, TargetPath = ?, DestinationPath = ?, LastBackupDate = ?,
            Automatic = ?, NextBackupDate = ?, TimeIntervalBackup = ?, CreationDate = ?,
            LastUpdateDate = ?, BackupCount = ?, MaxToKeep = ?, Notes = ?
        WHERE
            BackupId = ?
        """;
        try (Connection conn = DriverManager.getConnection(getUrlConnection());
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, backup.getName());
            stmt.setString(2, backup.getTargetPath());
            stmt.setString(3, backup.getDestinationPath());
            stmt.setString(4, backup.getLastBackupDate() != null ? backup.getLastBackupDate().toString() : null);
            stmt.setBoolean(5, backup.isAutomatic());
            stmt.setString(6, backup.getNextBackupDate() != null ? backup.getNextBackupDate().toString() : null);
            stmt.setString(7, backup.getTimeIntervalBackup() != null ? backup.getTimeIntervalBackup().toString() : null);
            stmt.setString(8, backup.getCreationDate() != null ? backup.getCreationDate().toString() : LocalDateTime.now().toString());
            stmt.setString(9, backup.getLastUpdateDate() != null ? backup.getLastUpdateDate().toString() : LocalDateTime.now().toString());
            stmt.setInt(10, backup.getCount());
            stmt.setInt(11, backup.getMaxToKeep());
            stmt.setString(12, backup.getNotes());
            stmt.setInt(13, backup.getId());
            stmt.executeUpdate();

            logger.info("User updated succesfully");

        } catch (SQLException e) {
            logger.error("User updating error: " + e.getMessage());
        }
    }

    public static void deleteBackup(int backupId) {
        String sql = "DELETE BackupConfigurations WHERE BackupId = ?";
        try (Connection conn = DriverManager.getConnection(getUrlConnection());
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, backupId);
            stmt.executeUpdate();

            logger.info("Backup deleted succesfully");

        } catch (SQLException e) {
            logger.error("Backup deleting error: " + e.getMessage());
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
    }

    public static List<Backup> getBackupList() {
        String sql = """
            SELECT
                BackupId, BackupName, TargetPath, DestinationPath, LastBackupDate, Automatic, NextBackupDate,
                TimeIntervalBackup, CreationDate, LastUpdateDate, BackupCount, MaxToKeep, Notes
            FROM
                BackupConfigurations
            """;

        List<Backup> backups = new ArrayList<>();

        try (
            Connection conn = DriverManager.getConnection(getUrlConnection());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                int id = rs.getInt("BackupId");
                String name = rs.getString("BackupName");
                String targetPath = rs.getString("TargetPath");
                String destinationPath = rs.getString("DestinationPath");

                String lastBackupDateStr = rs.getString("LastBackupDate");
                LocalDateTime lastBackupDate = lastBackupDateStr != null ? LocalDateTime.parse(lastBackupDateStr) : null;

                boolean automatic = rs.getBoolean("Automatic");

                String nextBackupDateStr = rs.getString("NextBackupDate");
                LocalDateTime nextBackupDate = nextBackupDateStr != null ? LocalDateTime.parse(nextBackupDateStr) : null;

                String timeIntervalStr = rs.getString("TimeIntervalBackup");
                TimeInterval timeInterval = timeIntervalStr != null ? TimeInterval.getTimeIntervalFromString(timeIntervalStr) : null;

                String creationDateStr = rs.getString("CreationDate");
                LocalDateTime creationDate = creationDateStr != null ? LocalDateTime.parse(creationDateStr) : null;

                String lastUpdateDateStr = rs.getString("LastUpdateDate");
                LocalDateTime lastUpdateDate = lastUpdateDateStr != null ? LocalDateTime.parse(lastUpdateDateStr) : null;

                int count = rs.getInt("BackupCount");
                int max = rs.getInt("MaxToKeep");
                String notes = rs.getString("Notes");

                backups.add(new Backup(id, name, targetPath, destinationPath, lastBackupDate, automatic, nextBackupDate, timeInterval, notes, creationDate, lastUpdateDate, count, max));
            }

        } catch (SQLException e) {
            logger.error("Error fetching backup list: " + e.getMessage(), e);
        }

        return backups;
    }

    public static Backup getBackupById(int backupId) {
        String sql = """
            SELECT
                BackupId, BackupName, TargetPath, DestinationPath, LastBackupDate, Automatic, NextBackupDate,
                TimeIntervalBackup, CreationDate, LastUpdateDate, BackupCount, MaxToKeep, Notes
            FROM
                BackupConfigurations
            WHERE
                BackupId = ?
            """;

        try (Connection conn = DriverManager.getConnection(getUrlConnection()); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, backupId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("BackupId");
                    String name = rs.getString("BackupName");
                    String targetPath = rs.getString("TargetPath");
                    String destinationPath = rs.getString("DestinationPath");

                    String lastBackupDateStr = rs.getString("LastBackupDate");
                    LocalDateTime lastBackupDate = lastBackupDateStr != null ? LocalDateTime.parse(lastBackupDateStr) : null;

                    boolean automatic = rs.getBoolean("Automatic");

                    String nextBackupDateStr = rs.getString("NextBackupDate");
                    LocalDateTime nextBackupDate = nextBackupDateStr != null ? LocalDateTime.parse(nextBackupDateStr) : null;

                    String timeIntervalStr = rs.getString("TimeIntervalBackup");
                    TimeInterval timeInterval = timeIntervalStr != null ? TimeInterval.getTimeIntervalFromString(timeIntervalStr) : null;

                    String creationDateStr = rs.getString("CreationDate");
                    LocalDateTime creationDate = creationDateStr != null ? LocalDateTime.parse(creationDateStr) : null;

                    String lastUpdateDateStr = rs.getString("LastUpdateDate");
                    LocalDateTime lastUpdateDate = lastUpdateDateStr != null ? LocalDateTime.parse(lastUpdateDateStr) : null;

                    int count = rs.getInt("BackupCount");
                    int max = rs.getInt("MaxToKeep");
                    String notes = rs.getString("Notes");

                    return new Backup(id, name, targetPath, destinationPath, lastBackupDate, automatic, nextBackupDate, timeInterval, notes, creationDate, lastUpdateDate, count, max);
                }
            }

        } catch (SQLException e) {
            logger.error("Error fetching backup by ID: " + e.getMessage(), e);
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        }

        return null;
    }

    public static Backup getBackupByName(String backupName) {
        String sql = """
            SELECT
                BackupId, BackupName, TargetPath, DestinationPath, LastBackupDate, Automatic, NextBackupDate,
                TimeIntervalBackup, CreationDate, LastUpdateDate, BackupCount, MaxToKeep, Notes
            FROM
                BackupConfigurations
            WHERE
                BackupName = ?
            """;

        try (Connection conn = DriverManager.getConnection(getUrlConnection()); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, backupName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("BackupId");
                    String name = rs.getString("BackupName");
                    String targetPath = rs.getString("TargetPath");
                    String destinationPath = rs.getString("DestinationPath");

                    String lastBackupDateStr = rs.getString("LastBackupDate");
                    LocalDateTime lastBackupDate = lastBackupDateStr != null ? LocalDateTime.parse(lastBackupDateStr) : null;

                    boolean automatic = rs.getBoolean("Automatic");

                    String nextBackupDateStr = rs.getString("NextBackupDate");
                    LocalDateTime nextBackupDate = nextBackupDateStr != null ? LocalDateTime.parse(nextBackupDateStr) : null;

                    String timeIntervalStr = rs.getString("TimeIntervalBackup");
                    TimeInterval timeInterval = timeIntervalStr != null ? TimeInterval.getTimeIntervalFromString(timeIntervalStr) : null;

                    String creationDateStr = rs.getString("CreationDate");
                    LocalDateTime creationDate = creationDateStr != null ? LocalDateTime.parse(creationDateStr) : null;

                    String lastUpdateDateStr = rs.getString("LastUpdateDate");
                    LocalDateTime lastUpdateDate = lastUpdateDateStr != null ? LocalDateTime.parse(lastUpdateDateStr) : null;

                    int count = rs.getInt("BackupCount");
                    int max = rs.getInt("MaxToKeep");
                    String notes = rs.getString("Notes");

                    return new Backup(id, name, targetPath, destinationPath, lastBackupDate, automatic, nextBackupDate, timeInterval, notes, creationDate, lastUpdateDate, count, max);
                }
            }

        } catch (SQLException e) {
            logger.error("Error fetching backup by Name: " + e.getMessage(), e);
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        }

        return null;
    }
}
