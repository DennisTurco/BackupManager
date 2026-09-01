package backupmanager.Entities;

import java.time.LocalDate;
import java.util.Map;

public record BackupAnalyticsSnapshot(
        long totalRequests,
        long successCount,
        long failedCount,
        double successRate,
        double avgDurationMs,
        double avgCompressionRate,
        long totalDiskUsageBytes,
        Map<LocalDate, Double> durationTrend
) {
    public static BackupAnalyticsSnapshot emptyDataset() {
        return new BackupAnalyticsSnapshot(
                0, 0, 0,
                0,
                0,
                0,
                0,
                Map.of()
        );
    }
}
