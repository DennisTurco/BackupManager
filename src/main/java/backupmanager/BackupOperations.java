package backupmanager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.BackupStatus;
import backupmanager.Enums.BackupTriggerType;
import backupmanager.Enums.ErrorType;
import backupmanager.Helpers.BackupHelper;
import static backupmanager.Helpers.BackupHelper.dateForfolderNameFormatter;
import backupmanager.Managers.ExceptionManager;
import backupmanager.Services.ZippingThread;
import backupmanager.Utils.FolderUtils;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;

public class BackupOperations {
    private static final Logger logger = LoggerFactory.getLogger(BackupOperations.class);

    // Set from the moment a backup is accepted until its outcome is recorded. The DB row only exists
    // after the source has been scanned (which can take a while on big folders), so the DB alone
    // can't stop a second backup from starting during that window.
    private static final AtomicBoolean backupInFlight = new AtomicBoolean(false);

    // After a failed/interrupted automatic run, retry after this delay (or the interval, if shorter)
    // instead of on every scheduler tick.
    private static final Duration FAILED_RUN_RETRY_DELAY = Duration.ofHours(1);

    public static boolean isBackupInFlight() {
        return backupInFlight.get() || BackupRequestRepository.isAnyBackupRunning();
    }

    /** @return true if the backup was started */
    public static boolean requestSingleBackup(ZippingContext context, BackupTriggerType triggeredBy) {
        if (!backupInFlight.compareAndSet(false, true)) {
            logger.warn("A backup is already running. Skipping this request (triggeredBy={}).", triggeredBy);
            return false;
        }
        boolean submitted = false;
        try {
            if (BackupRequestRepository.isAnyBackupRunning()) {
                logger.warn("A backup is already running. Skipping this request (triggeredBy={}).", triggeredBy);
                return false;
            }
            submitted = singleBackup(context, triggeredBy);
            return submitted;
        } finally {
            if (!submitted) backupInFlight.set(false);
        }
    }

    /** @return true if the zip task was submitted (it then releases the in-flight flag itself) */
    private static boolean singleBackup(ZippingContext context, BackupTriggerType triggeredBy) {
        if (context.execution().backup() == null) throw new IllegalArgumentException("Backup cannot be null!");

        logger.info("Event --> manual backup started");

        try {
            String path1 = context.execution().backup().getTargetPath();
            String path2 = context.execution().backup().getDestinationPath();

            if(!checkInputCorrect(context.execution().backup().getName(), path1, path2))
                return false;

            LocalDateTime dateNow = LocalDateTime.now();
            String date = dateNow.format(dateForfolderNameFormatter);
            String name1 = new File(path1).getName();
            name1 = removeExtension(name1);
            path2 = new File(path2, name1 + "_" + date).getPath();

            logger.info("date backup: " + date);

            return executeBackup(context, triggeredBy, path1, path2);
        } catch (Exception ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
            return false;
        }
    }

    public static boolean executeBackup(ZippingContext context, BackupTriggerType triggeredBy, String path1, String path2) {
        File sourceFile = new File(path1.trim());
        File outputFile = new File((path2+".zip").trim());

        int totalFilesCount = sourceFile.isDirectory() ? FolderUtils.countFilesInDirectory(sourceFile) : 1;

        createBackupRequest(context, triggeredBy, sourceFile, outputFile, totalFilesCount);

        return ZippingThread.zipDirectory(sourceFile, outputFile, context, totalFilesCount);
    }

    private static void createBackupRequest(ZippingContext context, BackupTriggerType triggeredBy, File sourceFile, File outputFile, int totalFilesCount) {
        long targetSize = FolderUtils.calculateFileOrFolderSize(sourceFile.getAbsolutePath());
        BackupRequestRepository.insertBackupRequest(BackupRequest.createNewBackupRequest(context.execution().backup().getId(), triggeredBy, outputFile.getAbsolutePath(), targetSize, totalFilesCount));
    }

    public static String removeExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0)
            return fileName.substring(0, dotIndex);
        return fileName;
    }

    // Re-reads the configuration instead of saving the snapshot taken when the run started, so
    // edits made while the backup was running (rename, notes, auto toggle...) aren't overwritten.
    private static ConfigurationBackup updateAfterSuccessfulBackup(int backupId) {
        ConfigurationBackup fresh = BackupConfigurationRepository.getBackupById(backupId);
        if (fresh == null) {
            logger.warn("Backup configuration {} was deleted while its backup was running", backupId);
            return null;
        }

        if (fresh.isAutomatic() && fresh.getTimeIntervalBackup() != null) {
            LocalDateTime nextDateBackup = BackupHelper.getNexDateBackup(fresh.getTimeIntervalBackup());
            fresh.setNextBackupDate(nextDateBackup);
            logger.info("Next date backup setted to: " + nextDateBackup);
        }
        fresh.setLastBackupDate(LocalDateTime.now());
        fresh.setCount(fresh.getCount() + 1);
        BackupHelper.updateBackup(fresh);

        logger.info("Backup :\"" + fresh.getName() + "\" updated after the backup");
        return fresh;
    }

    // Without this, an automatic backup that keeps failing would be retried on every scheduler tick
    public static void postponeAfterFailedRun(int backupId) {
        ConfigurationBackup fresh = BackupConfigurationRepository.getBackupById(backupId);
        if (fresh == null || !fresh.isAutomatic() || fresh.getTimeIntervalBackup() == null) return;

        TimeInterval ti = fresh.getTimeIntervalBackup();
        Duration interval = Duration.ofDays(ti.days()).plusHours(ti.hours()).plusMinutes(ti.minutes());
        Duration delay = interval.compareTo(FAILED_RUN_RETRY_DELAY) < 0 ? interval : FAILED_RUN_RETRY_DELAY;
        fresh.setNextBackupDate(LocalDateTime.now().plus(delay));
        BackupHelper.updateBackup(fresh);
        logger.info("Automatic backup \"{}\" did not complete, next attempt at {}", fresh.getName(), fresh.getNextBackupDate());
    }

    /**
     * Records the final result of a run. Called exactly once per submitted zip task, after the
     * output stream has been closed, so the file on disk is either complete or safe to delete.
     */
    public static void completeBackup(ZippingContext context, String outputZipPath, ZippingThread.Outcome outcome, String errorMessage) {
        int backupId = context.execution().backup().getId();
        try {
            BackupRequest request = BackupRequestRepository.getLastBackupInProgressByConfigurationId(backupId);
            LocalDateTime now = LocalDateTime.now();

            if (outcome == ZippingThread.Outcome.SUCCESS) {
                logger.info("Backup completed!");
                Long zippedSize = new File(outputZipPath).isFile() ? FolderUtils.calculateFileOrFolderSize(outputZipPath) : null;
                if (request != null) {
                    BackupRequestRepository.updateBackupRequestByRequestId(request.backupRequestId(), new BackupRequest(
                        request.backupRequestId(), request.backupConfigurationId(), request.startedDate(), now,
                        BackupStatus.FINISHED, 100, request.triggeredBy(), durationMs(request, now), request.outputPath(),
                        request.unzippedTargetSize(), zippedSize, request.filesCount(), null));
                }
                ConfigurationBackup fresh = updateAfterSuccessfulBackup(backupId);
                if (fresh != null) deleteOldBackupsIfNecessary(backupId, fresh.getMaxToKeep(), outputZipPath);
            } else {
                BackupHelper.deletePartialBackup(outputZipPath);
                if (request != null) {
                    String message = outcome == ZippingThread.Outcome.INTERRUPTED ? "Interrupted" : errorMessage;
                    BackupRequestRepository.updateBackupRequestByRequestId(request.backupRequestId(), new BackupRequest(
                        request.backupRequestId(), request.backupConfigurationId(), request.startedDate(), now,
                        BackupStatus.TERMINATED, request.progress(), request.triggeredBy(), durationMs(request, now), request.outputPath(),
                        request.unzippedTargetSize(), null, request.filesCount(), message));
                }
                postponeAfterFailedRun(backupId);
            }
        } catch (RuntimeException ex) {
            logger.error("Failed to record the outcome of backup {}: {}", backupId, ex.getMessage(), ex);
        } finally {
            backupInFlight.set(false);
        }
    }

    private static long durationMs(BackupRequest request, LocalDateTime end) {
        return Duration.between(request.startedDate(), end).toMillis();
    }

    public static boolean checkInputCorrect(String backupName, String path1, String path2) {
        if(path1 == null || path2 == null || path1.isEmpty() || path2.isEmpty()) {
            setError(ErrorType.InputMissing, backupName);
            return false;
        }

        if (!Files.exists(Path.of(path1)) || !Files.exists(Path.of(path2))) {
            setError(ErrorType.InputError, backupName);
            return false;
        }

        if (path1.equals(path2)) {
            setError(ErrorType.SamePaths, backupName);
            return false;
        }

        return true;
    }

    public static void updateProgressPercentage(int value, String path1, String path2, ZippingContext context, String fileProcessed, int filesCopiedSoFar, int totalFilesCount) {
        // 100% is only reported once the run is actually complete (see completeBackup): the file
        // count taken before zipping can differ from what actually gets visited
        int progress = Math.max(0, Math.min(99, value));
        if (progress == 0 || progress == 25 || progress == 50 || progress == 75)
            logger.info("Zipping progress: " + progress + "%");

        BackupRequest request = BackupRequestRepository.getLastBackupInProgressByConfigurationId(context.execution().backup().getId());
        if (request != null && request.progress() != progress)
            BackupRequestRepository.updateRequestProgressByRequestId(request.backupRequestId(), progress);
    }

    private static void deleteOldBackupsIfNecessary(int backupId, int maxBackupsToKeep, String destinationPath) {

        logger.info("Deleting old backups if necessary");

        File file = new File(destinationPath);
        File folder = file.getParentFile();

        if (folder == null) {
            logger.warn("Cannot determine parent folder of destination path: {}", destinationPath);
            return;
        }

        String baseName = removeExtension(file.getName());
        int lastUnderscore = baseName.lastIndexOf('_');
        if (lastUnderscore > 0) {
            baseName = baseName.substring(0, lastUnderscore);
        }

        // regex: baseName + "_" + timestamp
        String regex = Pattern.quote(baseName) + "_\\d{2}-\\d{2}-\\d{4}T\\d{2}-\\d{2}-\\d{2}\\.zip";

        // Two configurations whose source folders share a name and whose destination is the same
        // produce identically-prefixed files: never count (or delete) files that the history
        // records as another configuration's output.
        Set<String> otherConfigsOutputs = BackupRequestRepository.getRequestBackups().stream()
            .filter(r -> r.backupConfigurationId() != backupId && r.outputPath() != null)
            .map(r -> normalizeForComparison(new File(r.outputPath())))
            .collect(Collectors.toSet());

        File[] matchingFiles = folder.listFiles((dir, name) -> name.matches(regex)
            && !otherConfigsOutputs.contains(normalizeForComparison(new File(dir, name))));

        if (matchingFiles == null) {
            logger.warn("Error during deleting old backups: none matching files");
            return;
        }

        if (matchingFiles.length <= maxBackupsToKeep) {
            logger.info("No old backups to delete, {} files within limit {}", matchingFiles.length, maxBackupsToKeep);
            logger.info("Files retained:");
            for (File f : matchingFiles) {
                logger.info(" - {}", f.getName());
            }
            return;
        }

        logger.info("Found {} matching files, exceeding max allowed: {}", matchingFiles.length, maxBackupsToKeep);

        Arrays.sort(matchingFiles, Comparator.comparingLong(File::lastModified));

        for (int i = 0; i < matchingFiles.length - maxBackupsToKeep; i++) {
            File fileToDelete = matchingFiles[i];

            if (fileToDelete.delete())
                logger.info("Deleted old backup: {}", fileToDelete.getName());
            else
                logger.warn("Failed to delete old backup: {}", fileToDelete.getName());
        }

        logger.info("Files retained after deletion:");
        for (int i = matchingFiles.length - maxBackupsToKeep; i < matchingFiles.length; i++) {
            logger.info(" - {}", matchingFiles[i].getName());
        }
    }

    // Windows paths are case-insensitive, Linux/macOS ones are not ("Foo.zip" and "foo.zip" are two files)
    private static final boolean CASE_INSENSITIVE_PATHS = System.getProperty("os.name", "").toLowerCase().startsWith("windows");

    private static String normalizeForComparison(File file) {
        String path = file.getAbsolutePath();
        return CASE_INSENSITIVE_PATHS ? path.toLowerCase() : path;
    }

    // if last execution stopped brutally we have to delete the partial backups
    // for example if the computer has turned down before complete the backup process
    public static void deletePotentiallyIncompletedBackupsFromLastExecution() {
        List<BackupRequest> requests = BackupRequestRepository.getRunningBackups();
        if (requests != null) {
            for (BackupRequest request : requests) {
                ZippingThread.stopExecutorService(1);
                BackupHelper.forceBackupTermination(request);
            }
        }
    }

    public static void setError(ErrorType error, String backupName) {
        switch (error) {
            case InputMissing -> logger.warn("Input Missing! Backup: {}", backupName);
            case InputError -> logger.warn("Input Error! One or both paths do not exist. Backup: {}", backupName);
            case SamePaths -> logger.warn("The initial path and destination path cannot be the same. Backup: {}", backupName);
            case ErrorCountingFiles -> logger.warn("Error during counting files in directory. Backup: {}", backupName);
            case ZippingGenericError -> logger.warn("Error during zipping directory. Backup: {}", backupName);
            case ZippingIOError -> logger.warn("I/O error occurred while zipping directory. Backup: {}", backupName);
            case ZippingSecurityError -> logger.warn("Security exception while zipping directory. Backup: {}", backupName);
            default -> throw new IllegalArgumentException("Error type not recognized: " + error);
        }
    }
}
