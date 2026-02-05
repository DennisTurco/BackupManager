package backupmanager.database.Repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.BackupRequest;
import backupmanager.Enums.BackupStatusEnum;
import backupmanager.Enums.BackupTriggeredEnum;
import backupmanager.Helpers.SqlHelper;
import backupmanager.database.Database;

public class BackupRequestRepository {
    private static final Logger logger = LoggerFactory.getLogger(BackupRequestRepository.class);

    public static void insertBackupRequest(BackupRequest backup) {
        String sql = """
        INSERT INTO
            BackupRequest (BackupConfigurationId, StartedDate, CompletionDate, Status, Progress, TriggeredBy, DurationMs, UnzippedTargetSize, ZippedTargetSize, FilesCount, ErrorMessage)
        VALUES
            (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, backup.backupConfigurationId());
            stmt.setLong(2, SqlHelper.toMilliseconds(backup.startedDate()));
            stmt.setLong(3, SqlHelper.toMilliseconds(backup.completionDate()));
            stmt.setInt(4, backup.status().getCode());
            stmt.setInt(5, backup.progress());
            stmt.setInt(6, backup.triggeredBy().getCode());
            stmt.setLong(7, backup.durationMs());
            stmt.setLong(8, backup.unzippedTagetSize());
            stmt.setLong(9, backup.zippedTagetSize());
            stmt.setInt(10, backup.filesCount());
            stmt.setString(11, backup.errorMessage());
            stmt.executeUpdate();

            logger.info("Backup request inserted succesfully");

        } catch (SQLException e) {
            logger.error("Backup request inserting error: " + e.getMessage());
        }
    }

    public static List<BackupRequest> getBackupRequestList() {
        String sql = """
        SELECT
            BackupRequestId,
            BackupConfigurationId,
            StartedDate,
            CompletionDate,
            Status,
            Progress,
            TriggeredBy,
            DurationMs,
            UnzippedTargetSize,
            ZippedTargetSize,
            FilesCount,
            ErrorMessage
        FROM
            BackupRequests
                """;

        List<BackupRequest> backups = new ArrayList<>();

        try (
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                int backupRequestId = rs.getInt("BackupRequestId");
                int backupConfigurationId = rs.getInt("BackupConfigurationId");
                Long startedDateMills = rs.getLong("StartedDate");
                Long completionDateMills = rs.getLong("CompletionDate");
                int statusInt = rs.getInt("Status");
                int progress = rs.getInt("Progress");
                int triggeredByInt = rs.getInt("TriggeredBy");
                Long durationMs = rs.getLong("DurationMs");
                long unzippedTargetSize = rs.getLong("UnzippedTargetSize");
                long zippedTargetSize = rs.getLong("ZippedTargetSize");

                LocalDateTime startedDate = SqlHelper.toLocalDateTime(startedDateMills);
                LocalDateTime completionDate = SqlHelper.toLocalDateTime(completionDateMills);
                BackupStatusEnum status = BackupStatusEnum.fromCode(statusInt);
                BackupTriggeredEnum triggeredBy = BackupTriggeredEnum.fromCode(triggeredByInt);

                backups.add(new BackupRequest(backupRequestId, backupConfigurationId, startedDate, completionDate, status, progress, triggeredBy, durationMs, unzippedTargetSize, zippedTargetSize, 0, null));
            }

        } catch (SQLException e) {
            logger.error("Error fetching backup requests list: " + e.getMessage(), e);
        }

        return backups;
    }

    public static List<BackupRequest> getRunningBackups() {
        String sql = """
        SELECT
            BackupRequestId,
            BackupConfigurationId,
            StartedDate,
            CompletionDate,
            Status,
            Progress,
            TriggeredBy,
            DurationMs,
            UnzippedTargetSize,
            ZippedTargetSize,
            FilesCount,
            ErrorMessage
        FROM
            BackupRequests
        WHERE
            Status = ?
            """;

        List<BackupRequest> backups = new ArrayList<>();

        try (
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            BackupStatusEnum status = BackupStatusEnum.IN_PROGRESS;
            stmt.setInt(1, status.getCode());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int backupRequestId = rs.getInt("BackupRequestId");
                    int backupConfigurationId = rs.getInt("BackupConfigurationId");
                    Long startedDateMills = rs.getLong("StartedDate");
                    Long completionDateStr = rs.getLong("CompletionDate");
                    int progress = rs.getInt("Progress");
                    int triggeredByInt = rs.getInt("TriggeredBy");
                    Long durationMs = rs.getLong("DurationMs");
                    long unzippedTargetSize = rs.getLong("UnzippedTargetSize");
                    long zippedTargetSize = rs.getLong("ZippedTargetSize");

                    LocalDateTime startedDate = SqlHelper.toLocalDateTime(startedDateMills);
                    LocalDateTime completionDate = SqlHelper.toLocalDateTime(completionDateStr);
                    BackupTriggeredEnum triggeredBy = BackupTriggeredEnum.fromCode(triggeredByInt);

                    backups.add(new BackupRequest(backupRequestId, backupConfigurationId, startedDate, completionDate, status, progress, triggeredBy, durationMs, unzippedTargetSize, zippedTargetSize, 0, null));
                }
            }

        } catch (SQLException e) {
            logger.error("Error fetching running backup requests list: " + e.getMessage(), e);
        }

        return backups;
    }

    public static boolean isAnyBackupRunning() {
        String sql = """
            SELECT 1
            FROM BackupRequests
            WHERE Status = 1        -- 1 = IN_PROGRESS
            LIMIT 1
        """;

        try (
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            return rs.next();
        } catch (SQLException e) {
            logger.error("Error checking running backups", e);
            return true; // fail-safe
        }
    }

    public static void updateRequestStatusByRequestId(int backupRequestId, BackupStatusEnum status) {
        String sql = """
        UPDATE
            BackupRequests
        SET
            Status = ?,
        WHERE
            BackupRequestId = ?
                """;

        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, status.getCode());
            stmt.setInt(2, backupRequestId);
            stmt.executeUpdate();

            logger.info("Backup request status updated succesfully");

        } catch (SQLException e) {
            logger.error("Backup request status updating error: " + e.getMessage());
        }
    }

    public static void updateRequestProgressByRequestId(int backupRequestId, int progress) {
        String sql = """
        UPDATE
            BackupRequests
        SET
            Progress = ?,
        WHERE
            BackupRequestId = ?
                """;

        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, progress);
            stmt.setInt(2, backupRequestId);
            stmt.executeUpdate();

            logger.info("Backup request progress updated succesfully");

        } catch (SQLException e) {
            logger.error("Backup request progress updating error: " + e.getMessage());
        }
    }

    public static void updateZippedTargetSizeByRequestId(int backupRequestId, Long zippedTargetSize) {
        String sql = """
        UPDATE
            BackupRequests
        SET
            ZippedTargetSize = ?,
        WHERE
            BackupRequestId = ?
                """;

        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, zippedTargetSize);
            stmt.setInt(2, backupRequestId);
            stmt.executeUpdate();

            logger.info("Backup zipped target size updated succesfully");

        } catch (SQLException e) {
            logger.error("Backup zipped target size progress updating error: " + e.getMessage());
        }
    }

    public static BackupRequest getBackupByConfigurationId(int configurationId) {
        String sql = """
        SELECT
            BackupRequestId,
            BackupConfigurationId,
            StartedDate,
            CompletionDate,
            Status,
            Progress,
            TriggeredBy,
            DurationMs,
            UnzippedTargetSize,
            ZipperTargetSize,
            FilesCount,
            ErrorMessage
        FROM
            BackupRequests
        WHERE
            BackupConfigurationId = ?
            """;

        try (
            Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            int backupRequestId = rs.getInt("BackupRequestId");
            Long startedDateMills = rs.getLong("StartedDate");
            Long completionDateStr = rs.getLong("CompletionDate");
            int statusInt = rs.getInt("Status");
            int progress = rs.getInt("Progress");
            int triggeredByInt = rs.getInt("TriggeredBy");
            Long durationMs = rs.getLong("DurationMs");
            long unzippedTargetSize = rs.getLong("UnzippedTargetSize");
            long zippedTargetSize = rs.getLong("ZippedTargetSize");
            int filesCount = rs.getInt("FilesCount");
            String errorMessage = rs.getString("ErrorMessage");

            LocalDateTime startedDate = SqlHelper.toLocalDateTime(startedDateMills);
            LocalDateTime completionDate = SqlHelper.toLocalDateTime(completionDateStr);
            BackupStatusEnum status = BackupStatusEnum.fromCode(statusInt);
            BackupTriggeredEnum triggeredBy = BackupTriggeredEnum.fromCode(triggeredByInt);

            stmt.setInt(1, configurationId);

            return new BackupRequest(backupRequestId, configurationId, startedDate, completionDate, status, progress, triggeredBy, durationMs, unzippedTargetSize, zippedTargetSize, filesCount, errorMessage);

        } catch (SQLException e) {
            logger.error("Error fetching backup request by configuration id: " + e.getMessage(), e);
        }

        return null;
    }
}
