package backupmanager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.BackupTriggerType;
import backupmanager.Enums.ErrorType;
import backupmanager.Helpers.BackupHelper;
import static backupmanager.Helpers.BackupHelper.dateForfolderNameFormatter;
import backupmanager.Managers.ExceptionManager;
import backupmanager.Services.RunningBackupService;
import backupmanager.Services.ZippingThread;
import backupmanager.Utils.FolderUtils;
import backupmanager.database.Repositories.BackupConfigurationRepository;
import backupmanager.database.Repositories.BackupRequestRepository;

public class BackupOperations {
    private static final Logger logger = LoggerFactory.getLogger(BackupOperations.class);

    public static void requestSingleBackup(ZippingContext context, BackupTriggerType triggeredBy) {
        if (BackupRequestRepository.isAnyBackupRunning()) {
            logger.warn("A backup is already running. Skipping this request (triggeredBy={}).", triggeredBy);
            return;
        }
        singleBackup(context, triggeredBy);
    }

    private static void singleBackup(ZippingContext context, BackupTriggerType triggeredBy) {
        if (context.execution().backup() == null) throw new IllegalArgumentException("Backup cannot be null!");

        logger.info("Event --> manual backup started");

        try {
            String path1 = context.execution().backup().getTargetPath();
            String path2 = context.execution().backup().getDestinationPath();

            if(!checkInputCorrect(context.execution().backup().getName(), path1, path2))
                return;

            LocalDateTime dateNow = LocalDateTime.now();
            String date = dateNow.format(dateForfolderNameFormatter);
            String name1 = new File(path1).getName();
            name1 = removeExtension(name1);
            path2 = path2 + "\\" + name1 + "_" + date;

            logger.info("date backup: " + date);

            executeBackup(context, triggeredBy, path1, path2);
        } catch (Exception ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    public static void executeBackup(ZippingContext context, BackupTriggerType triggeredBy, String path1, String path2) {
        File sourceFile = new File(path1.trim());
        File outputFile = new File((path2+".zip").trim());

        int totalFilesCount = sourceFile.isDirectory() ? FolderUtils.countFilesInDirectory(sourceFile) : 1;

        createBackupRequest(context, triggeredBy, sourceFile, outputFile, totalFilesCount);

        ZippingThread.zipDirectory(sourceFile, outputFile, context, totalFilesCount);
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

    private static void updateAfterBackup(String path1, String path2, ZippingContext context) {
        if (context.execution().backup() == null) throw new IllegalArgumentException("Backup cannot be null!");
        if (path1 == null) throw new IllegalArgumentException("Initial path cannot be null!");
        if (path2 == null) throw new IllegalArgumentException("Destination path cannot be null!");

        logger.info("Backup completed!");

        // next day backup update
        if (context.execution().backup().isAutomatic()) {
            TimeInterval time = context.execution().backup().getTimeIntervalBackup();
            if (time != null) {
                LocalDateTime nextDateBackup = BackupHelper.getNexDateBackup(time);
                context.execution().backup().setNextBackupDate(nextDateBackup);
                logger.info("Next date backup setted to: " + nextDateBackup);
            }
        }
        context.execution().backup().setLastBackupDate(LocalDateTime.now());
        context.execution().backup().setCount(context.execution().backup().getCount()+1);

        try {
            List<ConfigurationBackup> backups = BackupConfigurationRepository.getBackupList();

            for (ConfigurationBackup b : backups) {
                if (b.getName().equals(context.execution().backup().getName())) {
                    b.updateBackup(context.execution().backup());
                    break;
                }
            }

            BackupHelper.updateBackup(context.execution().backup());

            logger.info("Backup :\"" + context.execution().backup().getName() + "\" updated after the backup");
        } catch (IllegalArgumentException ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
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

    public static void reEnableButtonsAndTable(ZippingContext context) {
        RunningBackupService.updateBackupStatusAfterCompletitionByBackupConfigurationId(context.execution().backup().getId());
    }

    public static void updateProgressPercentage(int value, String path1, String path2, ZippingContext context, String fileProcessed, int filesCopiedSoFar, int totalFilesCount) {
        if (value == 0 || value == 25 || value == 50 || value == 75 || value == 100)
            logger.info("Zipping progress: " + value + "%");

        BackupRequest request = BackupRequestRepository.getLastBackupInProgressByConfigurationId(context.execution().backup().getId());
        if (request != null) {

            if (value < 100)
                BackupRequestRepository.updateRequestProgressByRequestId(request.backupRequestId(), value);
            else if (value == 100) {
                RunningBackupService.updateBackupZippedFolderSizeById(request.backupRequestId(), path2);

                updateAfterBackup(path1, path2, context);
                deleteOldBackupsIfNecessary(context.execution().backup().getMaxToKeep(), path2);
            }
        }
    }

    private static void deleteOldBackupsIfNecessary(int maxBackupsToKeep, String destinationPath) {

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

        File[] matchingFiles = folder.listFiles((dir, name) -> name.matches(regex));

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
