package backupmanager.database.Repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Exceptions.BackupDeletionException;
import backupmanager.Helpers.SqlHelper;
import backupmanager.Managers.ExceptionManager;
import backupmanager.database.Database;
import java.util.stream.Collectors;

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
            logger.error("Backup configuration inserting error: {}", ex.getMessage(), ex);
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
            logger.error("Backup configuration updating error: {}", e.getMessage(), e);
        }
    }

    public static void deleteBackup(int backupId) throws BackupDeletionException {
        String sql = "UPDATE BackupConfigurations SET DeletedAt = ? WHERE BackupId = ?";
        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, System.currentTimeMillis());
            stmt.setInt(2, backupId);
            stmt.executeUpdate();

            logger.info("Backup soft-deleted successfully (id={})", backupId);

        } catch (SQLException e) {
            String error = "Backup configuration deleting error: " + e.getMessage();
            logger.error("{}", error, e);
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
            throw new BackupDeletionException(error, e);
        }
    }

    private static final String SELECT_ALL_COLUMNS = """
            SELECT
                BackupId, BackupName, TargetPath, DestinationPath, LastBackupDate, Automatic, NextBackupDate,
                TimeIntervalBackup, CreationDate, LastUpdateDate, BackupCount, MaxToKeep, Notes
            FROM
                BackupConfigurations
            """;

    private static ConfigurationBackup mapResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("BackupId");
        String name = rs.getString("BackupName");
        String targetPath = rs.getString("TargetPath");
        String destinationPath = rs.getString("DestinationPath");
        LocalDateTime lastBackupDate = SqlHelper.toLocalDateTime(rs.getLong("LastBackupDate"));
        boolean automatic = rs.getBoolean("Automatic");
        LocalDateTime nextBackupDate = SqlHelper.toLocalDateTime(rs.getLong("NextBackupDate"));
        TimeInterval timeInterval = SqlHelper.toTimeInterval(rs.getString("TimeIntervalBackup"));
        LocalDateTime creationDate = SqlHelper.toLocalDateTime(rs.getLong("CreationDate"));
        LocalDateTime lastUpdateDate = SqlHelper.toLocalDateTime(rs.getLong("LastUpdateDate"));
        int count = rs.getInt("BackupCount");
        int max = rs.getInt("MaxToKeep");
        String notes = rs.getString("Notes");
        return new ConfigurationBackup(id, name, targetPath, destinationPath, lastBackupDate, automatic, nextBackupDate, timeInterval, notes, creationDate, lastUpdateDate, count, max);
    }

    public static List<ConfigurationBackup> getBackupList() {
        List<ConfigurationBackup> backups = new ArrayList<>();
        String sql = SELECT_ALL_COLUMNS + " WHERE DeletedAt IS NULL";
        try (
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                backups.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching backup configuration list: {}", e.getMessage(), e);
        }
        return backups;
    }

    public static ConfigurationBackup getBackupById(int backupId) {
        String sql = SELECT_ALL_COLUMNS + " WHERE BackupId = ?";
        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, backupId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching backup configuration by ID: {}", e.getMessage(), e);
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
        return null;
    }

    public static ConfigurationBackup getBackupByName(String backupName) {
        String sql = SELECT_ALL_COLUMNS + " WHERE BackupName = ? AND DeletedAt IS NULL";
        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, backupName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching backup configuration by Name: {}", e.getMessage(), e);
            ExceptionManager.openExceptionMessage(e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
        return null;
    }

    public static Map<Integer, ConfigurationBackup> getBackupMap() {
        return getBackupList().stream()
                .collect(Collectors.toMap(ConfigurationBackup::getId, b -> b));
    }
}
