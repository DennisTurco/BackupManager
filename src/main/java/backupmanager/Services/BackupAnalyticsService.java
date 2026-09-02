package backupmanager.Services;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import backupmanager.Entities.BackupAnalyticsSnapshot;
import backupmanager.Entities.BackupRequest;
import backupmanager.Enums.BackupStatus;

public class BackupAnalyticsService {

    public static BackupAnalyticsSnapshot buildSnapshot(List<BackupRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            return BackupAnalyticsSnapshot.emptyDataset();
        }

        long total = requests.size();

        long successCount = requests.stream()
                .filter(r -> r.status() == BackupStatus.FINISHED)
                .count();

        long failedCount = requests.stream()
                .filter(r -> r.status() == BackupStatus.TERMINATED)
                .count();

        double successRate = total == 0 ? 0 : successCount * 100.0 / total;

        double avgDuration = requests.stream()
                .filter(r -> r.durationMs() != null)
                .mapToLong(BackupRequest::durationMs)
                .average()
                .orElse(0);

        double avgCompressionRate = computeCompressionRate(requests);

        // Only count requests whose output file is still actually present on disk — the history
        // table keeps a row for every run ever made, including ones long since removed by the
        // "max backups to keep" retention policy, so summing all of them would massively
        // overstate real disk usage.
        long diskUsage = requests.stream()
                .filter(r -> r.zippedTargetSize() != null && r.outputPath() != null)
                .filter(r -> new File(r.outputPath()).exists())
                .mapToLong(BackupRequest::zippedTargetSize)
                .sum();

        Map<LocalDate, Double> durationTrend =
                requests.stream()
                        .filter(r -> r.durationMs() != null)
                        .collect(Collectors.groupingBy(
                                r -> r.startedDate().toLocalDate(),
                                Collectors.averagingDouble(
                                        r -> r.durationMs() / 60000.0
                                )));

        return new BackupAnalyticsSnapshot(total, successCount, failedCount, successRate, avgDuration, avgCompressionRate, diskUsage, durationTrend);
    }

    public static double computeCompressionRate(List<BackupRequest> requests) {

        long totalUnzipped = requests.stream()
                .mapToLong(BackupRequest::unzippedTargetSize)
                .sum();

        long totalZipped = requests.stream()
                .filter(r -> r.zippedTargetSize() != null)
                .mapToLong(BackupRequest::zippedTargetSize)
                .sum();

        if (totalUnzipped == 0)
            return 0;

        return (double) totalZipped / totalUnzipped;
    }
}
