package backupmanager.api.routes;

import java.util.List;

import backupmanager.Entities.BackupAnalyticsSnapshot;
import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Services.BackupAnalyticsService;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class AnalyticsRoutes {

    public static void register(Javalin app) {
        app.get("/api/analytics", AnalyticsRoutes::getSnapshot);
        app.get("/api/history", AnalyticsRoutes::getHistory);
        app.get("/api/history/{configId}", AnalyticsRoutes::getHistoryByConfig);
        app.get("/api/backups/running", AnalyticsRoutes::getRunning);
        app.get("/api/backups/export.csv", AnalyticsRoutes::exportCsv);
    }

    private static void getSnapshot(Context ctx) {
        List<BackupRequest> all = BackupRequestRepository.getRequestBackups();
        BackupAnalyticsSnapshot snapshot = BackupAnalyticsService.buildSnapshot(all);
        ctx.json(snapshot);
    }

    private static void getHistory(Context ctx) {
        ctx.json(BackupRequestRepository.getRequestBackups());
    }

    private static void getHistoryByConfig(Context ctx) {
        int configId = Integer.parseInt(ctx.pathParam("configId"));
        List<BackupRequest> all = BackupRequestRepository.getRequestBackups();
        List<BackupRequest> filtered = all.stream()
            .filter(r -> r.backupConfigurationId() == configId)
            .toList();
        ctx.json(filtered);
    }

    private static void getRunning(Context ctx) {
        ctx.json(BackupRequestRepository.getRunningBackups());
    }

    private static void exportCsv(Context ctx) {
        List<ConfigurationBackup> backups = BackupConfigurationRepository.getBackupList();
        StringBuilder sb = new StringBuilder();
        sb.append(ConfigurationBackup.getCSVHeader()).append("\n");
        for (ConfigurationBackup b : backups) {
            sb.append(b.toCsvString()).append("\n");
        }
        ctx.contentType("text/csv")
           .header("Content-Disposition", "attachment; filename=\"backups.csv\"")
           .result(sb.toString());
    }
}
