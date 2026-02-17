package backupmanager;

import java.awt.TrayIcon;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.BackupRequest;
import backupmanager.Entities.ConfigurationBackup;
import backupmanager.Entities.TimeInterval;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.BackupStatus;
import backupmanager.Enums.BackupTriggerType;
import backupmanager.Enums.ErrorType;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import backupmanager.GUI.BackupManagerGUI;
import backupmanager.Helpers.BackupHelper;
import static backupmanager.Helpers.BackupHelper.dateForfolderNameFormatter;
import static backupmanager.Helpers.BackupHelper.formatter;
import backupmanager.Managers.ExceptionManager;
import backupmanager.Services.RunningBackupService;
import backupmanager.Services.ZippingThread;
import backupmanager.Table.TableDataManager;
import backupmanager.Utils.FolderUtils;
import backupmanager.database.Repositories.BackupRequestRepository;

public class BackupOperations {
    private static final Logger logger = LoggerFactory.getLogger(BackupOperations.class);
    public static void singleBackup(ZippingContext context, BackupTriggerType triggeredBy) {
        if (context.backup() == null) throw new IllegalArgumentException("Backup cannot be null!");

        logger.info("Event --> manual backup started");

        try {
            String path1 = context.backup().getTargetPath();
            String path2 = context.backup().getDestinationPath();

            if(!checkInputCorrect(context.backup().getName(), path1, path2, context.trayIcon()))
                return;

            if (context.progressBar() != null)
                context.progressBar().setVisible(true);

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
            reEnableButtonsAndTable(context);
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
        BackupRequestRepository.insertBackupRequest(BackupRequest.createNewBackupRequest(context.backup().getId(), triggeredBy, outputFile.getAbsolutePath(), targetSize, totalFilesCount));
    }

    public static String removeExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0)
            return fileName.substring(0, dotIndex);
        return fileName;
    }

    private static void updateAfterBackup(String path1, String path2, ZippingContext context) {
        if (context.backup() == null) throw new IllegalArgumentException("Backup cannot be null!");
        if (path1 == null) throw new IllegalArgumentException("Initial path cannot be null!");
        if (path2 == null) throw new IllegalArgumentException("Destination path cannot be null!");

        logger.info("Backup completed!");

        reEnableButtonsAndTable(context);

        // next day backup update
        if (context.backup().isAutomatic() == true) {
            TimeInterval time = context.backup().getTimeIntervalBackup();
            LocalDateTime nextDateBackup = BackupHelper.getNexDateBackup(time);
            context.backup().setNextBackupDate(nextDateBackup);
            logger.info("Next date backup setted to: " + nextDateBackup);
        }
        context.backup().setLastBackupDate(LocalDateTime.now());
        context.backup().setCount(context.backup().getCount()+1);

        try {
            List<ConfigurationBackup> backups = BackupHelper.getBackupList();

            for (ConfigurationBackup b : backups) {
                if (b.getName().equals(context.backup().getName())) {
                    b.updateBackup(context.backup());
                    break;
                }
            }

            BackupHelper.updateBackup(context.backup());

            logger.info("Backup :\"" + context.backup().getName() + "\" updated after the backup");

            if (context.trayIcon() != null)
                context.trayIcon().displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + context.backup().getName() + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.SUCCESS_MESSAGE) + "\n" + TranslationCategory.GENERAL.getTranslation(TranslationKey.FROM) + ": " + path1 + "\n" + TranslationCategory.GENERAL.getTranslation(TranslationKey.TO) + ": " + path2, TrayIcon.MessageType.INFO);
        } catch (IllegalArgumentException ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    public static String pathSearchWithFileChooser(boolean allowFiles) {
        logger.info("Event --> File chooser");

        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        if (allowFiles)
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        else
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnValue = jfc.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();

            if (selectedFile.isDirectory())
                logger.info("You selected the directory: " + selectedFile);
            else if (selectedFile.isFile())
                logger.info("You selected the file: " + selectedFile);

            return selectedFile.toString();
        }

        return null;
    }

    public static boolean checkInputCorrect(String backupName, String path1, String path2, TrayIcon trayIcon) {
        //check if inputs are null
        if(path1.length() == 0 || path2.length() == 0) {
            setError(ErrorType.InputMissing, trayIcon, backupName);
            return false;
        }

        if (!Files.exists(Path.of(path1)) || !Files.exists(Path.of(path2))) {
            setError(ErrorType.InputError, trayIcon, backupName);
            return false;
        }

        if (path1.equals(path2)) {
            setError(ErrorType.SamePaths, trayIcon, backupName);
            return false;
        }

        return true;
    }

    public static void interruptBackupProcess(ZippingContext context) {
        logger.info("Event --> interrupt backup process");

        ZippingThread.stopExecutorService(1);
        if (ZippingThread.isInterrupted())
            reEnableButtonsAndTable(context);

        if (context.progressBar() != null)
            context.progressBar().dispose();
    }

    public static void reEnableButtonsAndTable(ZippingContext context) {
        if (context.interruptBackupPopupItem() != null) context.interruptBackupPopupItem().setEnabled(false);
        if (context.deleteBackupPopupItem() != null) context.deleteBackupPopupItem().setEnabled(true);

        RunningBackupService.updateBackupStatusAfterCompletitionByBackupConfigurationId(context.backup().getId());

        if (BackupManagerGUI.backupTable != null)
            TableDataManager.removeProgressInTheTableAndRestoreAsDefault(context.backup(), formatter);
    }

    public static void updateProgressPercentage(int value, String path1, String path2, ZippingContext context, String fileProcessed, int filesCopiedSoFar, int totalFilesCount) {
        if (value == 0 || value == 25 || value == 50 || value == 75 || value == 100)
            logger.info("Zipping progress: " + value + "%");

        if (context.progressBar() != null)
            context.progressBar().updateProgressBar(value, fileProcessed, filesCopiedSoFar, totalFilesCount);

        if (BackupManagerGUI.backupTable != null)
            TableDataManager.updateProgressBarPercentage(context.backup(), value, formatter);

        BackupRequest request = BackupRequestRepository.getLastBackupInProgressByConfigurationId(context.backup().getId());
        if (request != null) {

            if (value < 100)
                BackupRequestRepository.updateRequestProgressByRequestId(request.backupRequestId(), value);
            else if (value == 100) {
                RunningBackupService.updateBackupZippedFolderSizeById(request.backupRequestId(), path2);

                updateAfterBackup(path1, path2, context);
                deleteOldBackupsIfNecessary(context.backup().getMaxToKeep(), path2);
            }
        }
    }

    private static void deleteOldBackupsIfNecessary(int maxBackupsToKeep, String destinationPath) {

        logger.info("Deleting old backups if necessary");

        File file = new File(destinationPath);
        File folder = file.getParentFile();

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
                boolean deleted = deletePartialBackup(request.outputPath());
                if (deleted) {
                    BackupRequestRepository.updateRequestStatusByRequestId(request.backupRequestId(), BackupStatus.TERMINATED);
                }
            }
        }
    }

    private static boolean deletePartialBackup(String filePath) {
        logger.info("Attempting to delete partial backup: " + filePath);

        ZippingThread.stopExecutorService(1);

        if (filePath == null || filePath.isEmpty()) {
            logger.warn("The file path is null or empty.");
            return false;
        }

        File file = new File(filePath);

        // Check if the file exists and is a valid file
        if (file.exists()) {
            if (file.isFile()) {
                try {
                    if (file.delete()) {
                        logger.info("Partial backup deleted successfully: " + file.getName());
                        return true;
                    } else {
                        logger.warn("Failed to delete partial backup (delete failed): " + file.getName());
                    }
                } catch (SecurityException e) {
                    logger.error("Security exception occurred while attempting to delete: " + file.getName(), e);
                } catch (Exception e) {
                    logger.error("Unexpected error while attempting to delete: " + file.getName(), e);
                }
            } else {
                logger.warn("The path points to a directory, not a file: " + filePath);
            }
        } else {
            logger.warn("The file does not exist: " + filePath);
        }

        return false;
}

    public static void setError(ErrorType error, TrayIcon trayIcon, String backupName) {
        switch (error) {
            case InputMissing -> {
                logger.warn("Input Missing!");
                if (trayIcon != null)
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_INPUT_MISSING), TrayIcon.MessageType.ERROR);
                else
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_INPUT_MISSING_GENERIC), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            }
            case InputError -> {
                logger.warn("Input Error! One or both paths do not exist.");
                if (trayIcon != null)
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_FILES_NOT_EXISTING), TrayIcon.MessageType.ERROR);
                else
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_PATH_NOT_EXISTING), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            }
            case SamePaths -> {
                logger.warn("The initial path and destination path cannot be the same. Please choose different paths");
                if (trayIcon != null)
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_SAME_PATHS), TrayIcon.MessageType.ERROR);
                else
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_SAME_PATHS_GENERIC), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            }
            case ErrorCountingFiles -> {
                logger.warn("Error during counting files in directory");
                if (trayIcon != null)
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_COUNTING_FILES), TrayIcon.MessageType.ERROR);
                else
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_COUNTING_FILES), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            }
            case ZippingGenericError -> {
                logger.warn("Error during zipping directory");
                if (trayIcon != null)
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_GENERIC), TrayIcon.MessageType.ERROR);
                else
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_GENERIC), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            }
            case ZippingIOError -> {
                logger.warn("I/O error occurred while zipping directory");
                if (trayIcon != null)
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_IO), TrayIcon.MessageType.ERROR);
                else
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_IO), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            }
            case ZippingSecurityError -> {
                logger.warn("Security exception while zipping directory");
                if (trayIcon != null)
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_SECURITY), TrayIcon.MessageType.ERROR);
                else
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_SECURITY), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            }
            default -> throw new IllegalArgumentException("Error type not recognized: " + error);
        }
    }
}
