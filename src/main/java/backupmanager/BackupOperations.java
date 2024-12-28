package backupmanager;

import java.awt.TrayIcon;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

import static backupmanager.GUI.BackupManagerGUI.OpenExceptionMessage;
import static backupmanager.GUI.BackupManagerGUI.dateForfolderNameFormatter;
import static backupmanager.GUI.BackupManagerGUI.formatter;
import backupmanager.Entities.Backup;
import backupmanager.Enums.ErrorTypes;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import backupmanager.Entities.Preferences;
import backupmanager.GUI.BackupManagerGUI;
import backupmanager.GUI.BackupProgressGUI;
import backupmanager.Entities.TimeInterval;
import backupmanager.Logger.LogLevel;
import backupmanager.Json.JSONAutoBackup;

public class BackupOperations {
    
    private static final JSONAutoBackup JSON = new JSONAutoBackup();
    private static Thread zipThread;
    
    public static void SingleBackup(Backup backup, TrayIcon trayIcon, BackupProgressGUI progressBar, JButton singleBackupBtn, JToggleButton autoBackupBtn) {
        if (backup == null) throw new IllegalArgumentException("Backup cannot be null!");
        
        Logger.logMessage("Event --> automatic single backup started", Logger.LogLevel.INFO);

        if (singleBackupBtn != null) singleBackupBtn.setEnabled(false);
        if (autoBackupBtn != null) autoBackupBtn.setEnabled(false);
        
        try {
            String temp = "\\";
            String path1 = backup.getInitialPath();
            String path2 = backup.getDestinationPath();

            if(!CheckInputCorrect(backup.getBackupName(), path1, path2, trayIcon)) return;

            LocalDateTime dateNow = LocalDateTime.now();
            String date = dateNow.format(dateForfolderNameFormatter);
            String name1 = path1.substring(path1.length()-1, path1.length()-1);

            for(int i = path1.length() - 1; i >= 0; i--) {
                if(path1.charAt(i) != temp.charAt(0)) name1 = path1.charAt(i) + name1;
                else break;
            }

            name1 = removeExtension(name1);

            path2 = path2 + "\\" + name1 + " (Backup " + date + ")";

            zipDirectory(path1, path2+".zip", backup, trayIcon, progressBar, singleBackupBtn, autoBackupBtn);
        } catch (IOException e) {
            Logger.logMessage("Error during the backup operation: the initial path is incorrect!", Logger.LogLevel.WARN);
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_INCORRECT_INITIAL_PATH), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            reEnableButtons(singleBackupBtn, autoBackupBtn);
            return;
        } catch (Exception ex) {
            Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            OpenExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
            reEnableButtons(singleBackupBtn, autoBackupBtn);
            return;
        }
    }

    public static String removeExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }
    
    private static void updateAfterBackup(String path1, String path2, Backup backup, TrayIcon trayIcon, JButton singleBackupBtn, JToggleButton autoBackupBtn) {
        if (backup == null) throw new IllegalArgumentException("Backup cannot be null!");
        if (path1 == null) throw new IllegalArgumentException("Initial path cannot be null!");
        if (path2 == null) throw new IllegalArgumentException("Destination path cannot be null!");
        
        LocalDateTime dateNow = LocalDateTime.now();
           
        Logger.logMessage("Backup completed!", Logger.LogLevel.INFO);

        reEnableButtons(singleBackupBtn, autoBackupBtn);
        
        // next day backup update
        if (backup.isAutoBackup() == true) {
            TimeInterval time = backup.getTimeIntervalBackup();
            LocalDateTime nextDateBackup = dateNow.plusDays(time.getDays())
                    .plusHours(time.getHours())
                    .plusMinutes(time.getMinutes());
            backup.setNextDateBackup(nextDateBackup);
            Logger.logMessage("Next date backup setted to: " + nextDateBackup, Logger.LogLevel.INFO);
        }
        backup.setLastBackup(dateNow);
        backup.setBackupCount(backup.getBackupCount()+1);
                    
        try {
            List<Backup> backups = JSON.ReadBackupListFromJSON(Preferences.getBackupList().getDirectory(), Preferences.getBackupList().getFile());
                        
            for (Backup b : backups) {
                if (b.getBackupName().equals(backup.getBackupName())) {
                    b.UpdateBackup(backup);
                    break;
                }
            }
            
            updateBackup(backups, backup);
            
            if (trayIcon != null) { 
                trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backup.getBackupName() + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.SUCCESS_MESSAGE) + "\n" + TranslationCategory.GENERAL.getTranslation(TranslationKey.FROM) + ": " + path1 + "\n" + TranslationCategory.GENERAL.getTranslation(TranslationKey.TO) + ": " + path2, TrayIcon.MessageType.INFO);
            }
        } catch (IllegalArgumentException ex) {
            Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            OpenExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        } catch (Exception e) {
            Logger.logMessage("Error saving file", Logger.LogLevel.WARN);
            JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_SAVING_FILE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    public static String pathSearchWithFileChooser(boolean allowFiles) {
        Logger.logMessage("Event --> File chooser", Logger.LogLevel.INFO);
        
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        
        if (allowFiles)
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        else
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnValue = jfc.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();

            if (selectedFile.isDirectory()) {
                Logger.logMessage("You selected the directory: " + selectedFile, Logger.LogLevel.INFO);
            } else if (selectedFile.isFile()) {
                Logger.logMessage("You selected the file: " + selectedFile, Logger.LogLevel.INFO);
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

    public static void zipDirectory(String sourceDirectoryPath, String targetZipPath, Backup backup, TrayIcon trayIcon, BackupProgressGUI progressBar, JButton singleBackupBtn, JToggleButton autoBackupBtn) throws IOException { // Track copied files
        Logger.logMessage("Starting zipping process", LogLevel.INFO);

        File file = new File(sourceDirectoryPath.trim());
        int totalFilesCount = file.isDirectory() ? countFilesInDirectory(file) : 1;
        if (totalFilesCount == -1) {
            progressBar.dispose();
            setError(ErrorTypes.ErrorCountingFiles, trayIcon, targetZipPath);
            reEnableButtons(singleBackupBtn, autoBackupBtn);
            return;
        }
        
        AtomicInteger copiedFilesCount = new AtomicInteger(0);
        
        zipThread = new Thread(() -> {
            Path sourceDir = Paths.get(sourceDirectoryPath);
            String rootFolderName = sourceDir.getFileName().toString(); // Get the root folder name

            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(targetZipPath))) {
                if (file.isFile()) {
                    addFileToZip(sourceDirectoryPath, targetZipPath, zipOut, file.toPath(), file.getName(), copiedFilesCount, totalFilesCount, backup, trayIcon, progressBar, singleBackupBtn, autoBackupBtn);
                } else {
                    Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (file == null) {
                                Logger.logMessage("File is null", Logger.LogLevel.WARN);
                                return FileVisitResult.CONTINUE;
                            }

                            if (Thread.currentThread().isInterrupted()) {
                                Logger.logMessage("Zipping process manually interrupted", Logger.LogLevel.INFO);
                                reEnableButtons(singleBackupBtn, autoBackupBtn);
                                return FileVisitResult.TERMINATE; // Stop if interrupted
                            }

                            // Calculate the relative path inside the zip
                            Path targetFilePath = sourceDir.relativize(file);
                            String zipEntryName = rootFolderName + "/" + targetFilePath.toString();

                            Logger.logMessage("Zipping file: " + zipEntryName, Logger.LogLevel.DEBUG);

                            // Create a new zip entry for the file
                            zipOut.putNextEntry(new ZipEntry(zipEntryName));

                            // Copy the file content to the zip output stream
                            try (InputStream in = Files.newInputStream(file)) {
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = in.read(buffer)) > 0) {
                                    zipOut.write(buffer, 0, len);
                                }
                            }

                            zipOut.closeEntry(); // Close the zip entry after the file is written
                            
                            // Update progress
                            int filesCopiedSoFar = copiedFilesCount.incrementAndGet();
                            int actualProgress = (int) (((double) filesCopiedSoFar / totalFilesCount) * 100);
                            UpdateProgressPercentage(actualProgress, sourceDirectoryPath, targetZipPath, backup, trayIcon, progressBar, singleBackupBtn, autoBackupBtn);  // Update progress percentage

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            if (dir == null) {
                                Logger.logMessage("Directory is null", Logger.LogLevel.WARN);
                                return FileVisitResult.CONTINUE;
                            }

                            if (Thread.currentThread().isInterrupted()) {
                                Logger.logMessage("Zipping process manually interrupted", Logger.LogLevel.INFO);
                                reEnableButtons(singleBackupBtn, autoBackupBtn);
                                return FileVisitResult.TERMINATE; // Stop if interrupted
                            }
                            
                            // case when the initial folder is empty
                            if (totalFilesCount == 0) {
                                UpdateProgressPercentage(100, sourceDirectoryPath, targetZipPath, backup, trayIcon, progressBar, singleBackupBtn, autoBackupBtn);
                                return FileVisitResult.TERMINATE;
                            }

                            // Create directory entry in the zip if needed
                            Path targetDir = sourceDir.relativize(dir);
                            String name = rootFolderName + "/" + targetDir.toString() + "/";
                            Logger.logMessage("Zipping directory: " + name, Logger.LogLevel.DEBUG);
                            zipOut.putNextEntry(new ZipEntry(name));
                            zipOut.closeEntry();
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }  catch (IOException ioEx) {
                // here we can't run setError because it happens somethimes during backups for "Documents" folder randomly
                Logger.logMessage("I/O error occurred while zipping directory: " + sourceDirectoryPath + ". Error: " + ioEx.getMessage(), Logger.LogLevel.WARN);
            } catch (SecurityException secEx) {
                Logger.logMessage("Security exception while accessing directory: " + sourceDirectoryPath + ". Error: " + secEx.getMessage(), Logger.LogLevel.ERROR, secEx);
                setError(ErrorTypes.ZippingSecurityError, trayIcon, targetZipPath);
            } catch (Exception ex) {
                Logger.logMessage("Unexpected error during zipping directory: " + sourceDirectoryPath + ". Error: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
                setError(ErrorTypes.ZippingGenericError, trayIcon, targetZipPath);
            } finally {
                reEnableButtons(singleBackupBtn, autoBackupBtn);
            }
        });

        zipThread.start(); // Start the zipping thread
    }

    private static void reEnableButtons(JButton singleBackupBtn, JToggleButton autoBackupBtn) {
        if (singleBackupBtn != null) singleBackupBtn.setEnabled(true);
        if (autoBackupBtn != null) autoBackupBtn.setEnabled(true);
    }

    private static void addFileToZip(String sourceDirectoryPath, String destinationDirectoryPath, ZipOutputStream zipOut, Path file, String zipEntryName, AtomicInteger copiedFilesCount, int totalFilesCount, Backup backup, TrayIcon trayIcon, BackupProgressGUI progressBar, JButton singleBackupBtn, JToggleButton autoBackupBtn) throws IOException {
        if (zipEntryName == null || zipEntryName.isEmpty()) {
            zipEntryName = file.getFileName().toString();
        }    
        zipOut.putNextEntry(new ZipEntry(zipEntryName));
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                zipOut.write(buffer, 0, len);
            }
        }
        zipOut.closeEntry();
        
        int filesCopiedSoFar = copiedFilesCount.incrementAndGet();
        int actualProgress = (int) (((double) filesCopiedSoFar / totalFilesCount) * 100);
        UpdateProgressPercentage(actualProgress, sourceDirectoryPath, destinationDirectoryPath, backup, trayIcon, progressBar, singleBackupBtn, autoBackupBtn);
    }
    
    public static void updateBackupList(List<Backup> backups) {
        if (backups == null) throw new IllegalArgumentException("Backup list is null!");
            
        JSON.UpdateBackupListJSON(Preferences.getBackupList().getDirectory(), Preferences.getBackupList().getFile(), backups);
        
        if (BackupManagerGUI.model != null)
            updateTableWithNewBackupList(backups);
    }
    
    public static void updateBackup(List<Backup> backups, Backup updatedBackup) {
        if (updatedBackup == null) throw new IllegalArgumentException("Backup is null!");
        
        JSON.UpdateSingleBackupInJSON(Preferences.getBackupList().getDirectory(), Preferences.getBackupList().getFile(), updatedBackup);
        
        if (BackupManagerGUI.model != null)
            updateTableWithNewBackupList(backups);
    }
    
    public static void UpdateProgressPercentage(int value, String path1, String path2, Backup backup, TrayIcon trayIcon, BackupProgressGUI progressBar, JButton singleBackupBtn, JToggleButton autoBackupBtn) {
        if (value == 0 || value == 25 || value == 50 || value == 75 || value == 100)
            Logger.logMessage("Zipping progress: " + value, Logger.LogLevel.INFO);
        
        if (progressBar != null)
            progressBar.UpdateProgressBar(value);
        
        if (value == 100) {
            updateAfterBackup(path1, path2, backup, trayIcon, singleBackupBtn, autoBackupBtn);

            // delete 
            deleteOldBackupsIfNecessary(backup.getMaxBackupsToKeep(), path2);
        }
    }

    private static void deleteOldBackupsIfNecessary(int maxBackupsToKeep, String destinationPath) {
        Logger.logMessage("Deleting old backups if necessary", LogLevel.INFO);

        File folder = new File(destinationPath).getParentFile();
        String fileBackuppedToSearch = new File(destinationPath).getName();
        
        // extract the file name (before the parentesis)
        String baseName = fileBackuppedToSearch.substring(0, fileBackuppedToSearch.indexOf(" (Backup"));
        
        if (folder != null && folder.isDirectory()) {
            // get current count
            FilenameFilter filter = (dir, name) -> name.matches(baseName + " \\(Backup \\d{2}-\\d{2}-\\d{4} \\d{2}\\.\\d{2}\\.\\d{2}\\)\\.zip");
            File[] matchingFiles = folder.listFiles(filter); // getting files for that filter  

            if (matchingFiles == null) {
                Logger.logMessage("Error during deleting old backups: none matching files", LogLevel.WARN);
                return;
            }

            // check if the max is passed, and if it is, remove the oldest
            if (matchingFiles.length > maxBackupsToKeep) {
                Logger.logMessage("Found " + matchingFiles.length + " matching files, exceeding max allowed: " + maxBackupsToKeep, LogLevel.INFO);

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
                        Logger.logMessage("Error parsing dates: " + e.getMessage(), LogLevel.ERROR);
                        return 0;
                    }
                });

                // delete older files
                for (int i = 0; i < matchingFiles.length - maxBackupsToKeep; i++) {
                    File fileToDelete = matchingFiles[i];
                    if (fileToDelete.delete()) {
                        Logger.logMessage("Deleted old backup: " + fileToDelete.getName(), LogLevel.INFO);
                    } else {
                        Logger.logMessage("Failed to delete old backup: " + fileToDelete.getName(), LogLevel.WARN);
                    }
                }
            }
        } else {
            Logger.logMessage("Destination path is not a directory: " + destinationPath, LogLevel.ERROR);
        }
    }

    private static String extractDateFromFileName(String fileName, String pattern) throws Exception {
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(fileName);

        if (matcher.find()) {
            return matcher.group(1);
        }
        
        throw new Exception("No date found in file name: " + fileName);
    }

    private static void setError(ErrorTypes error, TrayIcon trayIcon, String backupName) {
        switch (error) {
            case InputMissing:
                Logger.logMessage("Input Missing!", Logger.LogLevel.WARN);
                if (trayIcon != null) {
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_INPUT_MISSING), TrayIcon.MessageType.ERROR);
                } else {
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_INPUT_MISSING_GENERIC), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case InputError:
                Logger.logMessage("Input Error! One or both paths do not exist.", Logger.LogLevel.WARN);
                if (trayIcon != null) { 
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_FILES_NOT_EXISTING), TrayIcon.MessageType.ERROR);
                } else {
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_PATH_NOT_EXISTING), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case SamePaths:
                Logger.logMessage("The initial path and destination path cannot be the same. Please choose different paths", Logger.LogLevel.WARN);
                if (trayIcon != null) { 
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_SAME_PATHS), TrayIcon.MessageType.ERROR);
                } else {
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_SAME_PATHS_GENERIC), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case ErrorCountingFiles:
                Logger.logMessage("Error during counting files in directory", Logger.LogLevel.WARN);
                if (trayIcon != null) { 
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_COUNTING_FILES), TrayIcon.MessageType.ERROR);
                } else {
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_COUNTING_FILES), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case ZippingGenericError:
                Logger.logMessage("Error during zipping directory", Logger.LogLevel.WARN);
                if (trayIcon != null) { 
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_GENERIC), TrayIcon.MessageType.ERROR);
                } else {
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_GENERIC), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case ZippingIOError:
                Logger.logMessage("I/O error occurred while zipping directory", Logger.LogLevel.WARN);
                if (trayIcon != null) { 
                    trayIcon.displayMessage(TranslationCategory.GENERAL.getTranslation(TranslationKey.APP_NAME), TranslationCategory.GENERAL.getTranslation(TranslationKey.BACKUP) + ": " + backupName + TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_IO), TrayIcon.MessageType.ERROR);
                } else {
                    JOptionPane.showMessageDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_MESSAGE_ZIPPING_IO), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case ZippingSecurityError:
                Logger.logMessage("Security exception while zipping directory", Logger.LogLevel.WARN);
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
    
    private static int countFilesInDirectory(File directory) {
        if (directory == null) {
            Logger.logMessage("Directory is null", Logger.LogLevel.WARN);
            return -1;
        }
        if (!directory.canRead()) {
            Logger.logMessage("Unable to read directory: " + directory.getAbsolutePath(), Logger.LogLevel.WARN);
            return -1;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            Logger.logMessage("Unable to list files for directory: " + directory.getAbsolutePath(), Logger.LogLevel.WARN);
            return -1;
        }
    	
    	int count = 0;
        for (File file : files) {
            if (file.isFile()) {
                count++;
            } else if (file.isDirectory()) {
                count += countFilesInDirectory(file); // Recursively count files in subdirectories.
            }
        }
        return count;
    }

    public static void updateTableWithNewBackupList(List<Backup> updatedBackups) { 
        Logger.logMessage("updating backup list", Logger.LogLevel.DEBUG);
        
        SwingUtilities.invokeLater(() -> {
            BackupManagerGUI.model.setRowCount(0);

            for (Backup backup : updatedBackups) {
                BackupManagerGUI.model.addRow(new Object[]{
                    backup.getBackupName(),
                    backup.getInitialPath(),
                    backup.getDestinationPath(),
                    backup.getLastBackup() != null ? backup.getLastBackup().format(formatter) : "",
                    backup.isAutoBackup(),
                    backup.getNextDateBackup() != null ? backup.getNextDateBackup().format(formatter) : "",
                    backup.getTimeIntervalBackup() != null ? backup.getTimeIntervalBackup().toString() : ""
                });
            }
        });
    }

    public static void StopCopyFiles() {
        zipThread.interrupt();
    }
}
