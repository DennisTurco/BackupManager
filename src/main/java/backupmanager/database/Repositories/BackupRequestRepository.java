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
            BackupRequests (BackupConfigurationId, StartedDate, CompletionDate, Status, Progress, TriggeredBy, DurationMs, OutputPath, UnzippedTargetSize, ZippedTargetSize, FilesCount, ErrorMessage)
        VALUES
            (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, backup.backupConfigurationId());
            stmt.setLong(2, SqlHelper.toMilliseconds(backup.startedDate()));
            stmt.setObject(3, SqlHelper.toMilliseconds(backup.completionDate()));
            stmt.setInt(4, backup.status().getCode());
            stmt.setInt(5, backup.progress());
            stmt.setInt(6, backup.triggeredBy().getCode());
            stmt.setObject(7, backup.durationMs());
            stmt.setString(8, backup.outputPath());
            stmt.setLong(9, backup.unzippedTargetSize());
            stmt.setObject(10, backup.zippedTargetSize());
            stmt.setObject(11, backup.filesCount());
            stmt.setObject(12, backup.errorMessage());
            stmt.executeUpdate();

            logger.info("Backup request inserted succesfully");

        } catch (SQLException e) {
            logger.error("Backup request inserting error: " + e.getMessage());
        }
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
            OutputPath,
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
                    String outputPath = rs.getString("OutputPath");
                    long unzippedTargetSize = rs.getLong("UnzippedTargetSize");
                    long zippedTargetSize = rs.getLong("ZippedTargetSize");
                    int filesCount = rs.getInt("FilesCount");
                    String errorMessage = rs.getString("ErrorMessage");

                    LocalDateTime startedDate = SqlHelper.toLocalDateTime(startedDateMills);
                    LocalDateTime completionDate = SqlHelper.toLocalDateTime(completionDateStr);
                    BackupTriggeredEnum triggeredBy = BackupTriggeredEnum.fromCode(triggeredByInt);

                    backups.add(new BackupRequest(backupRequestId, backupConfigurationId, startedDate, completionDate, status, progress, triggeredBy, durationMs, outputPath, unzippedTargetSize, zippedTargetSize, filesCount, errorMessage));
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
            Status = ?
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
            Progress = ?
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

    public static void updateBackupRequestByRequestId(int backupRequestId, BackupRequest request) {
        String sql = """
        UPDATE
            BackupRequests
        SET
            StartedDate = ?,
            CompletionDate = ?,
            Status = ?,
            Progress = ?,
            TriggeredBy = ?,
            DurationMs = ?,
            OutputPath = ?,
            UnzippedTargetSize = ?,
            ZippedTargetSize = ?,
            FilesCount = ?,
            ErrorMessage = ?
        WHERE
            BackupRequestId = ?
                """;

        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, SqlHelper.toMilliseconds(request.startedDate()));
            stmt.setLong(2, SqlHelper.toMilliseconds(request.completionDate()));
            stmt.setInt(3, request.status().getCode());
            stmt.setInt(4, request.progress());
            stmt.setInt(5, request.triggeredBy().getCode());
            stmt.setLong(6, request.durationMs());
            stmt.setString(7, request.outputPath());
            stmt.setLong(8, request.unzippedTargetSize());
            stmt.setLong(9, request.zippedTargetSize());
            stmt.setInt(10, request.filesCount());
            stmt.setString(11, request.errorMessage());
            stmt.setLong(12, request.backupRequestId());
            stmt.executeUpdate();

            logger.info("Backup request updated succesfully");

        } catch (SQLException e) {
            logger.error("Backup request updating error: " + e.getMessage());
        }
    }

    public static BackupRequest getLastBackupInProgressByConfigurationId(int configurationId) {
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
            OutputPath,
            UnzippedTargetSize,
            ZippedTargetSize,
            FilesCount,
            ErrorMessage
        FROM
            BackupRequests
        WHERE
            BackupConfigurationId = ?
            AND Status = 1
        ORDER BY 1 DESC
            """;

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, configurationId);

            try (ResultSet rs = stmt.executeQuery()){
                int backupRequestId = rs.getInt("BackupRequestId");
                Long startedDateMills = rs.getLong("StartedDate");
                Long completionDateStr = rs.getLong("CompletionDate");
                int statusInt = rs.getInt("Status");
                int progress = rs.getInt("Progress");
                int triggeredByInt = rs.getInt("TriggeredBy");
                Long durationMs = rs.getLong("DurationMs");
                String outputPath = rs.getString("OutputPath");
                long unzippedTargetSize = rs.getLong("UnzippedTargetSize");
                long zippedTargetSize = rs.getLong("ZippedTargetSize");
                int filesCount = rs.getInt("FilesCount");
                String errorMessage = rs.getString("ErrorMessage");

                LocalDateTime startedDate = SqlHelper.toLocalDateTime(startedDateMills);
                LocalDateTime completionDate = SqlHelper.toLocalDateTime(completionDateStr);
                BackupStatusEnum status = BackupStatusEnum.fromCode(statusInt);
                BackupTriggeredEnum triggeredBy = BackupTriggeredEnum.fromCode(triggeredByInt);

                return new BackupRequest(backupRequestId, configurationId, startedDate, completionDate, status, progress, triggeredBy, durationMs, outputPath, unzippedTargetSize, zippedTargetSize, filesCount, errorMessage);
            }
        } catch (SQLException e) {
            logger.error("Error fetching backup request by configuration id: " + e.getMessage(), e);
        }

        return null;
    }
}
