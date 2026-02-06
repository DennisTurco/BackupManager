package backupmanager.database.Repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Helpers.SqlHelper;
import backupmanager.Managers.ExceptionManager;
import backupmanager.database.Database;

public class BackupConfigurationRepository {
    private static final Logger logger = LoggerFactory.getLogger(BackupConfigurationRepository.class);

    public static void insertBackup(ConfigurationBackup backup) {
        String sql = """
        INSERT INTO
            BackupConfigurations (BackupName, TargetPath, DestinationPath, LastBackupDate, Automatic, NextBackupDate, TimeIntervalBackup, CreationDate, LastUpdateDate, BackupCount, MaxToKeep, Notes)
        VALUES
            (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;
        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, backup.getName());
            stmt.setString(2, backup.getTargetPath());
            stmt.setString(3, backup.getDestinationPath());
            stmt.setLong(4, SqlHelper.toMilliseconds(backup.getLastBackupDate()));
            stmt.setBoolean(5, backup.isAutomatic());
            stmt.setLong(6, SqlHelper.toMilliseconds(backup.getNextBackupDate()));
            stmt.setString(7, SqlHelper.toString(backup.getTimeIntervalBackup()));
            stmt.setLong(8, SqlHelper.toMilliseconds(backup.getCreationDate(), LocalDateTime.now()));
            stmt.setLong(9, SqlHelper.toMilliseconds(backup.getLastUpdateDate(), LocalDateTime.now()));
            stmt.setInt(10, backup.getCount());
            stmt.setInt(11, backup.getMaxToKeep());
            stmt.setString(12, backup.getNotes());
            stmt.executeUpdate();

            logger.info("Backup inserted succesfully");

        } catch (SQLException ex) {
            logger.error("Backup configuration inserting error: " + ex.getMessage());
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    public static void updateBackup(ConfigurationBackup backup) {
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
        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, backup.getName());
            stmt.setString(2, backup.getTargetPath());
            stmt.setString(3, backup.getDestinationPath());
            stmt.setLong(4, SqlHelper.toMilliseconds(backup.getLastBackupDate()));
            stmt.setBoolean(5, backup.isAutomatic());
            stmt.setLong(6, SqlHelper.toMilliseconds(backup.getNextBackupDate()));
            stmt.setString(7, SqlHelper.toString(backup.getTimeIntervalBackup()));
            stmt.setLong(8, SqlHelper.toMilliseconds(backup.getCreationDate(), LocalDateTime.now()));
            stmt.setLong(9, SqlHelper.toMilliseconds(backup.getLastUpdateDate(), LocalDateTime.now()));
            stmt.setInt(10, backup.getCount());
            stmt.setInt(11, backup.getMaxToKeep());
            stmt.setString(12, backup.getNotes());
            stmt.setInt(13, backup.getId());
            stmt.executeUpdate();

            logger.info("Backup configuration updated succesfully");

        } catch (SQLException e) {
            logger.error("Backup configuration updating error: " + e.getMessage());
        }
    }

    public static void deleteBackup(int backupId) {
        String sql = "DELETE FROM BackupConfigurations WHERE BackupId = ?";
        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, backupId);
            stmt.executeUpdate();

            logger.info("Backup deleted succesfully");

        } catch (SQLException e) {
            logger.error("Backup configuration deleting error: " + e.getMessage());
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
    }

    public static List<ConfigurationBackup> getBackupList() {
        String sql = """
            SELECT
                BackupId, BackupName, TargetPath, DestinationPath, LastBackupDate, Automatic, NextBackupDate,
                TimeIntervalBackup, CreationDate, LastUpdateDate, BackupCount, MaxToKeep, Notes
            FROM
                BackupConfigurations
            """;

        List<ConfigurationBackup> backups = new ArrayList<>();

        try (
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                int id = rs.getInt("BackupId");
                String name = rs.getString("BackupName");
                String targetPath = rs.getString("TargetPath");
                String destinationPath = rs.getString("DestinationPath");
                Long lastBackupDateMillis = rs.getLong("LastBackupDate");
                boolean automatic = rs.getBoolean("Automatic");
                Long nextBackupDateMillis = rs.getLong("NextBackupDate");
                String timeIntervalStr = rs.getString("TimeIntervalBackup");
                Long creationDateMillis = rs.getLong("CreationDate");
                Long lastUpdateDateMillis = rs.getLong("LastUpdateDate");

                LocalDateTime lastBackupDate = SqlHelper.toLocalDateTime(lastBackupDateMillis);
                LocalDateTime nextBackupDate = SqlHelper.toLocalDateTime(nextBackupDateMillis);
                TimeInterval timeInterval = SqlHelper.toTimeInterval(timeIntervalStr);
                LocalDateTime creationDate = SqlHelper.toLocalDateTime(creationDateMillis);
                LocalDateTime lastUpdateDate = SqlHelper.toLocalDateTime(lastUpdateDateMillis);

                int count = rs.getInt("BackupCount");
                int max = rs.getInt("MaxToKeep");
                String notes = rs.getString("Notes");

                backups.add(new ConfigurationBackup(id, name, targetPath, destinationPath, lastBackupDate, automatic, nextBackupDate, timeInterval, notes, creationDate, lastUpdateDate, count, max));
            }

        } catch (SQLException e) {
            logger.error("Error fetching backup configuration list: " + e.getMessage(), e);
        }

        return backups;
    }

    public static ConfigurationBackup getBackupById(int backupId) {
        String sql = """
            SELECT
                BackupId, BackupName, TargetPath, DestinationPath, LastBackupDate, Automatic, NextBackupDate,
                TimeIntervalBackup, CreationDate, LastUpdateDate, BackupCount, MaxToKeep, Notes
            FROM
                BackupConfigurations
            WHERE
                BackupId = ?
            """;

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, backupId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("BackupId");
                    String name = rs.getString("BackupName");
                    String targetPath = rs.getString("TargetPath");
                    String destinationPath = rs.getString("DestinationPath");
                    Long lastBackupDateMillis = rs.getLong("LastBackupDate");
                    boolean automatic = rs.getBoolean("Automatic");
                    Long nextBackupDateMillis = rs.getLong("NextBackupDate");
                    String timeIntervalStr = rs.getString("TimeIntervalBackup");
                    Long creationDateMillis = rs.getLong("CreationDate");
                    Long lastUpdateDateMillis = rs.getLong("LastUpdateDate");

                    LocalDateTime lastBackupDate = SqlHelper.toLocalDateTime(lastBackupDateMillis);
                    LocalDateTime nextBackupDate = SqlHelper.toLocalDateTime(nextBackupDateMillis);
                    TimeInterval timeInterval = SqlHelper.toTimeInterval(timeIntervalStr);
                    LocalDateTime creationDate = SqlHelper.toLocalDateTime(creationDateMillis);
                    LocalDateTime lastUpdateDate = SqlHelper.toLocalDateTime(lastUpdateDateMillis);

                    int count = rs.getInt("BackupCount");
                    int max = rs.getInt("MaxToKeep");
                    String notes = rs.getString("Notes");

                    return new ConfigurationBackup(id, name, targetPath, destinationPath, lastBackupDate, automatic, nextBackupDate, timeInterval, notes, creationDate, lastUpdateDate, count, max);
                }
            }

        } catch (SQLException e) {
            logger.error("Error fetching backup configuration by ID: " + e.getMessage(), e);
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        }

        return null;
    }

    public static ConfigurationBackup getBackupByName(String backupName) {
        String sql = """
            SELECT
                BackupId, BackupName, TargetPath, DestinationPath, LastBackupDate, Automatic, NextBackupDate,
                TimeIntervalBackup, CreationDate, LastUpdateDate, BackupCount, MaxToKeep, Notes
            FROM
                BackupConfigurations
            WHERE
                BackupName = ?
            """;

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, backupName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("BackupId");
                    String name = rs.getString("BackupName");
                    String targetPath = rs.getString("TargetPath");
                    String destinationPath = rs.getString("DestinationPath");
                    Long lastBackupDateMillis = rs.getLong("LastBackupDate");
                    boolean automatic = rs.getBoolean("Automatic");
                    Long nextBackupDateMillis = rs.getLong("NextBackupDate");
                    String timeIntervalStr = rs.getString("TimeIntervalBackup");
                    Long creationDateMillis = rs.getLong("CreationDate");
                    Long lastUpdateDateMillis = rs.getLong("LastUpdateDate");

                    LocalDateTime lastBackupDate = SqlHelper.toLocalDateTime(lastBackupDateMillis);
                    LocalDateTime nextBackupDate = SqlHelper.toLocalDateTime(nextBackupDateMillis);
                    TimeInterval timeInterval = SqlHelper.toTimeInterval(timeIntervalStr);
                    LocalDateTime creationDate = SqlHelper.toLocalDateTime(creationDateMillis);
                    LocalDateTime lastUpdateDate = SqlHelper.toLocalDateTime(lastUpdateDateMillis);

                    int count = rs.getInt("BackupCount");
                    int max = rs.getInt("MaxToKeep");
                    String notes = rs.getString("Notes");

                    return new ConfigurationBackup(id, name, targetPath, destinationPath, lastBackupDate, automatic, nextBackupDate, timeInterval, notes, creationDate, lastUpdateDate, count, max);
                }
            }

        } catch (SQLException e) {
            logger.error("Error fetching backup configuration by Name: " + e.getMessage(), e);
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        }

        return null;
    }

    public static Map<Integer, ConfigurationBackup> getBackupMap() {

        List<ConfigurationBackup> backups = getBackupList();
        Map<Integer, ConfigurationBackup> map = new HashMap<>(backups.size());

        for (ConfigurationBackup backup : backups) {
            map.put(backup.getId(), backup);
        }

        return map;
    }
}
