package backupmanager.Services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

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

        long diskUsage = requests.stream()
                .mapToLong(r ->
                        r.zippedTargetSize() != null ?
                                r.zippedTargetSize() :
                                0)
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

    public static CategoryDataset buildRequestsPerMonthDataset(List<BackupRequest> requests, String title) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        LocalDate now = LocalDate.now();
        LocalDate oneYearAgo = now.minusMonths(11).withDayOfMonth(1);

        Map<String, Long> map = requests.stream()
                .filter(r -> !r.startedDate().toLocalDate().isBefore(oneYearAgo))
                .collect(Collectors.groupingBy(
                        r -> {
                            var d = r.startedDate();
                            return d.getYear() + "-" + String.format("%02d", d.getMonthValue());
                        },
                        Collectors.counting()
                ));

        List<String> last12Months = IntStream.rangeClosed(0, 11)
                .mapToObj(i -> {
                    LocalDate month = oneYearAgo.plusMonths(i);
                    return month.getYear() + "-" + String.format("%02d", month.getMonthValue());
                })
                .toList();

        for (String month : last12Months) {
            dataset.addValue(map.getOrDefault(month, 0L), title, month);
        }

        return dataset;
    }

    public static XYDataset buildDurationTrendDataset(Map<LocalDate, Double> trendMap, String title) {

        TimeSeries series = new TimeSeries(title);

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(29);

        trendMap.entrySet().stream()
            .filter(entry -> !entry.getKey().isBefore(thirtyDaysAgo))
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                LocalDate date = entry.getKey();
                Double value = entry.getValue();

                series.add(
                    new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()),
                    value
                );
            });

        return new TimeSeriesCollection(series);
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

    public static double convertAvgDurationinMinutes(BackupAnalyticsSnapshot snapshot) {
        return snapshot.avgDurationMs() / 60000.0;
    }
}
