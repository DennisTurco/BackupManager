package backupmanager.api.routes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Entities.BackupExecutionContext;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.BackupTriggerType;
import backupmanager.Exceptions.BackupDeletionException;
import backupmanager.Helpers.BackupHelper;
import backupmanager.Services.ZippingThread;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

public class BackupRoutes {

    private static final Logger logger = LoggerFactory.getLogger(BackupRoutes.class);

    public static void register(Javalin app) {
        app.get("/api/backups", BackupRoutes::getAll);
        app.post("/api/backups", BackupRoutes::create);
        app.get("/api/backups/{id}", BackupRoutes::getById);
        app.put("/api/backups/{id}", BackupRoutes::update);
        app.delete("/api/backups/{id}", BackupRoutes::delete);
        app.post("/api/backups/{id}/run", BackupRoutes::runBackup);
        app.post("/api/backups/{id}/interrupt", BackupRoutes::interruptBackup);
    }

    private static void getAll(Context ctx) {
        List<ConfigurationBackup> backups = BackupConfigurationRepository.getBackupList();
        ctx.json(backups);
    }

    private static void getById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ConfigurationBackup backup = BackupConfigurationRepository.getBackupById(id);
        if (backup == null) throw new NotFoundResponse("Backup not found: " + id);
        ctx.json(backup);
    }

    private static void create(Context ctx) {
        BackupRequest req = ctx.bodyAsClass(BackupRequest.class);

        if (!Files.exists(Path.of(req.targetPath()))) {
            ctx.status(422).json(new ErrorMsg("Source path does not exist: " + req.targetPath()));
            return;
        }
        if (!Files.exists(Path.of(req.destinationPath()))) {
            ctx.status(422).json(new ErrorMsg("Destination path does not exist: " + req.destinationPath()));
            return;
        }
        if (req.targetPath().equals(req.destinationPath())) {
            ctx.status(422).json(new ErrorMsg("Source and destination paths cannot be the same"));
            return;
        }

        TimeInterval ti = (req.automatic() && req.timeIntervalBackup() != null)
            ? new TimeInterval(req.timeIntervalBackup().days(), req.timeIntervalBackup().hours(), req.timeIntervalBackup().minutes())
            : null;
        LocalDateTime nextDate = (req.automatic() && ti != null) ? BackupHelper.getNexDateBackup(ti) : null;
        ConfigurationBackup backup = new ConfigurationBackup(
            req.name(), req.targetPath(), req.destinationPath(),
            null, req.automatic(), nextDate, ti,
            req.notes(), LocalDateTime.now(), LocalDateTime.now(), 0, req.maxToKeep()
        );
        BackupConfigurationRepository.insertBackup(backup);
        ctx.status(201).json(backup);
    }

    private static void update(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ConfigurationBackup existing = BackupConfigurationRepository.getBackupById(id);
        if (existing == null) throw new NotFoundResponse("Backup not found: " + id);

        BackupRequest req = ctx.bodyAsClass(BackupRequest.class);

        // Validate paths exist before saving
        if (!Files.exists(Path.of(req.targetPath()))) {
            ctx.status(422).json(new ErrorMsg("Source path does not exist: " + req.targetPath()));
            return;
        }
        if (!Files.exists(Path.of(req.destinationPath()))) {
            ctx.status(422).json(new ErrorMsg("Destination path does not exist: " + req.destinationPath()));
            return;
        }
        if (req.targetPath().equals(req.destinationPath())) {
            ctx.status(422).json(new ErrorMsg("Source and destination paths cannot be the same"));
            return;
        }

        TimeInterval ti = (req.automatic() && req.timeIntervalBackup() != null)
            ? new TimeInterval(req.timeIntervalBackup().days(), req.timeIntervalBackup().hours(), req.timeIntervalBackup().minutes())
            : null;
        // Recompute nextBackupDate when automatic is being turned on or the interval changed
        LocalDateTime nextDate;
        if (!req.automatic()) {
            nextDate = null;
        } else if (ti == null) {
            nextDate = existing.getNextBackupDate();
        } else {
            boolean wasAutomatic = existing.isAutomatic();
            boolean intervalChanged = existing.getTimeIntervalBackup() == null
                || existing.getTimeIntervalBackup().days()    != ti.days()
                || existing.getTimeIntervalBackup().hours()   != ti.hours()
                || existing.getTimeIntervalBackup().minutes() != ti.minutes();
            nextDate = (!wasAutomatic || intervalChanged)
                ? BackupHelper.getNexDateBackup(ti)
                : existing.getNextBackupDate();
        }
        ConfigurationBackup updated = new ConfigurationBackup(
            id, req.name(), req.targetPath(), req.destinationPath(),
            existing.getLastBackupDate(), req.automatic(), nextDate, ti,
            req.notes(), existing.getCreationDate(), LocalDateTime.now(),
            existing.getCount(), req.maxToKeep()
        );
        BackupConfigurationRepository.updateBackup(updated);
        ctx.json(updated);
    }

    private static void delete(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ConfigurationBackup existing = BackupConfigurationRepository.getBackupById(id);
        if (existing == null) throw new NotFoundResponse("Backup not found: " + id);
        try {
            BackupConfigurationRepository.deleteBackup(id);
            ctx.status(204);
        } catch (BackupDeletionException e) {
            ctx.status(409).json(new ErrorMsg(e.getMessage()));
        }
    }

    private static void runBackup(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ConfigurationBackup backup = BackupConfigurationRepository.getBackupById(id);
        if (backup == null) throw new NotFoundResponse("Backup not found: " + id);

        if (BackupRequestRepository.isAnyBackupRunning()) {
            ctx.status(409).json(new ErrorMsg("A backup is already running"));
            return;
        }

        // Validate paths here so we can return a proper HTTP error
        // instead of letting the check fail silently inside a headless virtual thread
        String src  = backup.getTargetPath();
        String dest = backup.getDestinationPath();
        if (src == null || src.isBlank() || dest == null || dest.isBlank()) {
            ctx.status(422).json(new ErrorMsg("Source or destination path is empty"));
            return;
        }
        if (!Files.exists(Path.of(src))) {
            ctx.status(422).json(new ErrorMsg("Source path does not exist: " + src));
            return;
        }
        if (!Files.exists(Path.of(dest))) {
            ctx.status(422).json(new ErrorMsg("Destination path does not exist: " + dest));
            return;
        }
        if (src.equals(dest)) {
            ctx.status(422).json(new ErrorMsg("Source and destination paths cannot be the same"));
            return;
        }

        ZippingContext zCtx = new ZippingContext(BackupExecutionContext.create(backup));
        Thread.ofVirtual().start(() -> {
            try {
                BackupOperations.requestSingleBackup(zCtx, BackupTriggerType.API);
            } catch (Exception e) {
                logger.error("Backup {} failed in background thread: {}", id, e.getMessage(), e);
            }
        });

        ctx.status(202).json(new ErrorMsg("Backup started"));
    }

    private static void interruptBackup(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ConfigurationBackup backup = BackupConfigurationRepository.getBackupById(id);
        if (backup == null) throw new NotFoundResponse("Backup not found: " + id);

        if (BackupRequestRepository.getLastBackupInProgressByConfigurationId(id) == null) {
            ctx.status(409).json(new ErrorMsg("No backup is currently running for this configuration"));
            return;
        }

        boolean interrupted = ZippingThread.interruptCurrentTask();
        if (!interrupted) {
            ctx.status(409).json(new ErrorMsg("No backup is currently running"));
            return;
        }

        ctx.status(202).json(new ErrorMsg("Backup interruption requested"));
    }

    public record BackupRequest(
        String name,
        String targetPath,
        String destinationPath,
        boolean automatic,
        TimeIntervalDto timeIntervalBackup,
        String notes,
        int maxToKeep
    ) {}

    public record TimeIntervalDto(int days, int hours, int minutes) {}

    private record ErrorMsg(String message) {}
}
