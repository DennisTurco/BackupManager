package backupmanager;

import java.awt.TrayIcon;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.Backup;
import backupmanager.Entities.RunningBackups;
import backupmanager.Entities.TimeInterval;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.BackupStatusEnum;
import backupmanager.Enums.ErrorTypes;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import backupmanager.GUI.BackupManagerGUI;
import static backupmanager.GUI.BackupManagerGUI.dateForfolderNameFormatter;
import static backupmanager.GUI.BackupManagerGUI.formatter;
import backupmanager.Managers.BackupManager;
import backupmanager.Managers.ExceptionManager;
import backupmanager.Services.ZippingThread;
import backupmanager.Table.TableDataManager;

public class BackupOperations {
    private static final Logger logger = LoggerFactory.getLogger(BackupOperations.class);    
    public static void SingleBackup(ZippingContext context) {
        if (context.backup == null) throw new IllegalArgumentException("Backup cannot be null!");
        
        logger.info("Event --> manual backup started");

        try {
            String temp = "\\";
            String path1 = context.backup.getInitialPath();
            String path2 = context.backup.getDestinationPath();

            if(!CheckInputCorrect(context.backup.getBackupName(), path1, path2, context.trayIcon)) 
                return;

            if (context.progressBar != null)
                context.progressBar.setVisible(true);

            LocalDateTime dateNow = LocalDateTime.now();
            String date = dateNow.format(dateForfolderNameFormatter);
            String name1 = path1.substring(path1.length()-1, path1.length()-1);

            for(int i = path1.length() - 1; i >= 0; i--) {
                if(path1.charAt(i) != temp.charAt(0)) name1 = path1.charAt(i) + name1;
                else break;
            }

            name1 = removeExtension(name1);
            path2 = path2 + "\\" + name1 + " (Backup " + date + ")";

            logger.info("date backup: " + date);

            ZippingThread.zipDirectory(path1, path2 + ".zip", context);
        } catch (Exception ex) {
            logger.error("An error occurred: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
            reEnableButtonsAndTable(context);
        }
    }

    public static String removeExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }
    
    private static void updateAfterBackup(String path1, String path2, ZippingContext context) {
        if (context.backup == null) throw new IllegalArgumentException("Backup cannot be null!");
        if (path1 == null) throw new IllegalArgumentException("Initial path cannot be null!");
        if (path2 == null) throw new IllegalArgumentException("Destination path cannot be null!");
                   
        logger.info("Backup completed!");

        reEnableButtonsAndTable(context);

        // next day backup update
        if (context.backup.isAutoBackup() == true) {
            TimeInterval time = context.backup.getTimeIntervalBackup();
            LocalDateTime nextDateBackup = BackupManager.getNexDateBackup(time);
            context.backup.setNextDateBackup(nextDateBackup);
            logger.info("Next date backup setted to: " + nextDateBackup);
        }
        context.backup.setLastBackup(LocalDateTime.now());
        context.backup.setBackupCount(context.backup.getBackupCount()+1);
                    
        try {
            List<Backup> backups = BackupManager.getBackupList();
            
            for (Backup b : backups) {
                if (b.getBackupName().equals(context.backup.getBackupName())) {
                    b.UpdateBackup(context.backup);
                    break;
                }
            }
            
            BackupManager.updateBackup(context.backup);

            logger.info("Backup :\"" + context.backup.getBackupName() + "\" updated after the backup");
            
            if (context.trayIcon != null) { 
                context.trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + context.backup.getBackupName() + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.SUCCESS_MESSAGE) + "\n" + TranslationCategory.GENERAL.getTranslation(TranslationKey.FROM) + ": " + path1 + "\n" + TranslationCategory.GENERAL.getTranslation(TranslationKey.TO) + ": " + path2, TrayIcon.MessageType.INFO);
            }
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

            if (selectedFile.isDirectory()) {
                logger.info("You selected the directory: " + selectedFile);
            } else if (selectedFile.isFile()) {
                logger.info("You selected the file: " + selectedFile);
            }

            return selectedFile.toString();
        }

        return null;
    }
    
    public static boolean CheckInputCorrect(String backupName, String path1, String path2, TrayIcon trayIcon) {
        //check if inputs are null
        if(path1.length() == 0 || path2.length() == 0) {
            setError(ErrorTypes.InputMissing, trayIcon, backupName);
            return false;
        }
        
        if (!Files.exists(Path.of(path1)) || !Files.exists(Path.of(path2))) {
            setError(ErrorTypes.InputError, trayIcon, backupName);
            return false;
        }

        if (path1.equals(path2)) {
            setError(ErrorTypes.SamePaths, trayIcon, backupName);
            return false;
        }

        return true;
    }

    public static void interruptBackupProcess(ZippingContext context) {
        logger.info("Event --> interrupt backup process");
        
        ZippingThread.stopExecutorService(1);
        if (ZippingThread.isInterrupted())
            reEnableButtonsAndTable(context);
        
        if (context.progressBar != null)
            context.progressBar.dispose();
    }

    public static void reEnableButtonsAndTable(ZippingContext context) {
        if (context.interruptBackupPopupItem != null) context.interruptBackupPopupItem.setEnabled(false);
        if (context.deleteBackupPopupItem != null) context.deleteBackupPopupItem.setEnabled(true);

        // edit the backup running state
        RunningBackups.updateBackupStatusAfterCompletition(context.backup.getBackupName());

        if (BackupManagerGUI.backupTable != null) 
            TableDataManager.removeProgressInTheTableAndRestoreAsDefault(context.backup, formatter);
    } 
    
    public static void UpdateProgressPercentage(int value, String path1, String path2, ZippingContext context, String fileProcessed, int filesCopiedSoFar, int totalFilesCount) {
        if (value == 0 || value == 25 || value == 50 || value == 75 || value == 100)
            logger.info("Zipping progress: " + value + "%");

        if (context.progressBar != null) {
            context.progressBar.updateProgressBar(value, fileProcessed, filesCopiedSoFar, totalFilesCount);
        }

        if (BackupManagerGUI.backupTable != null) {
            TableDataManager.updateProgressBarPercentage(context.backup, value, formatter);
        }
        
        // updating running backups file .json
        RunningBackups running = RunningBackups.readBackupFromJSON(context.backup.getBackupName());
        if (running != null) {
            running.progress = value;
            RunningBackups.updateBackupToJSON(running);
        }else {
            RunningBackups.updateBackupToJSON(new RunningBackups(context.backup.getBackupName(), path2, value, BackupStatusEnum.Progress));
        }
       
        // if (value == 100) {
        //     RunningBackups.updateBackupToJSON(new RunningBackups(context.backup.getBackupName(), path2, value, BackupStatusEnum.Finished));
        // } else {
        //     RunningBackups.updateBackupToJSON(new RunningBackups(context.backup.getBackupName(), path2, value, BackupStatusEnum.Progress));
        // }

        if (value == 100) {
            updateAfterBackup(path1, path2, context);
            deleteOldBackupsIfNecessary(context.backup.getMaxBackupsToKeep(), path2);
        }
    }
    
    private static void deleteOldBackupsIfNecessary(int maxBackupsToKeep, String destinationPath) {
        logger.info("Deleting old backups if necessary");

        File folder = new File(destinationPath).getParentFile();
        String fileBackuppedToSearch = new File(destinationPath).getName();
        
        // extract the file name (before the parentesis)
        String baseName = fileBackuppedToSearch.substring(0, fileBackuppedToSearch.indexOf(" (Backup"));
        
        if (folder != null && folder.isDirectory()) {
            // get current count
            FilenameFilter filter = (dir, name) -> name.matches(baseName + " \\(Backup \\d{2}-\\d{2}-\\d{4} \\d{2}\\.\\d{2}\\.\\d{2}\\)\\.zip");
            File[] matchingFiles = folder.listFiles(filter); // getting files for that filter  

            if (matchingFiles == null) {
                logger.warn("Error during deleting old backups: none matching files");
                return;
            }

            // check if the max is passed, and if it is, remove the oldest
            if (matchingFiles.length > maxBackupsToKeep) {
                logger.info("Found " + matchingFiles.length + " matching files, exceeding max allowed: " + maxBackupsToKeep);

                Arrays.sort(matchingFiles, (f1, f2) -> {
                    String datePattern = "\\(Backup (\\d{2}-\\d{2}-\\d{4} \\d{2}\\.\\d{2}\\.\\d{2})\\)\\.zip"; // regex aggiornata
                
                    try {
                        // extracting dates from file names
                        String date1 = extractDateFromFileName(f1.getName(), datePattern);
                        String date2 = extractDateFromFileName(f2.getName(), datePattern);
                
                        LocalDateTime dateTime1 = LocalDateTime.parse(date1, BackupManagerGUI.dateForfolderNameFormatter);
                        LocalDateTime dateTime2 = LocalDateTime.parse(date2, BackupManagerGUI.dateForfolderNameFormatter);

                        return dateTime1.compareTo(dateTime2);
                    } catch (Exception e) {
                        logger.error("Error parsing dates: " + e.getMessage(), e);
                        return 0;
                    }
                });

                // delete older files
                for (int i = 0; i < matchingFiles.length - maxBackupsToKeep; i++) {
                    File fileToDelete = matchingFiles[i];
                    if (fileToDelete.delete()) {
                        logger.info("Deleted old backup: " + fileToDelete.getName());
                    } else {
                        logger.warn("Failed to delete old backup: " + fileToDelete.getName());
                    }
                }
            }
        } else {
            logger.warn("Destination path is not a directory: " + destinationPath);
        }
    }

    public static boolean deletePartialBackup(String filePath) {
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

    private static String extractDateFromFileName(String fileName, String pattern) throws Exception {
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(fileName);

        if (matcher.find()) {
            return matcher.group(1);
        }
        
        throw new Exception("No date found in file name: " + fileName);
    }

    public static void setError(ErrorTypes error, TrayIcon trayIcon, String backupName) {
        switch (error) {
            case InputMissing:
                logger.warn("Input Missing!");
                if (trayIcon != null) {
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_INPUT_MISSING), TrayIcon.MessageType.ERROR);
                } else {
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_INPUT_MISSING_GENERIC), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case InputError:
                logger.warn("Input Error! One or both paths do not exist.");
                if (trayIcon != null) { 
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_FILES_NOT_EXISTING), TrayIcon.MessageType.ERROR);
                } else {
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_PATH_NOT_EXISTING), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case SamePaths:
                logger.warn("The initial path and destination path cannot be the same. Please choose different paths");
                if (trayIcon != null) { 
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_SAME_PATHS), TrayIcon.MessageType.ERROR);
                } else {
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_SAME_PATHS_GENERIC), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case ErrorCountingFiles:
                logger.warn("Error during counting files in directory");
                if (trayIcon != null) { 
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_COUNTING_FILES), TrayIcon.MessageType.ERROR);
                } else {
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_COUNTING_FILES), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case ZippingGenericError:
                logger.warn("Error during zipping directory");
                if (trayIcon != null) { 
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_GENERIC), TrayIcon.MessageType.ERROR);
                } else {
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_GENERIC), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case ZippingIOError:
                logger.warn("I/O error occurred while zipping directory");
                if (trayIcon != null) { 
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_IO), TrayIcon.MessageType.ERROR);
                } else {
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_IO), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case ZippingSecurityError:
                logger.warn("Security exception while zipping directory");
                if (trayIcon != null) { 
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_SECURITY), TrayIcon.MessageType.ERROR);
                } else {
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_SECURITY), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
                break;
            default:
                throw new IllegalArgumentException("Error type not recognized: " + error);
        }
    }
    
}
