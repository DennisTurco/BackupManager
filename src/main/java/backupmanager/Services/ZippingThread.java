package backupmanager.Services;

import java.awt.TrayIcon;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;

import backupmanager.BackupOperations;
import backupmanager.Logger;
import backupmanager.Logger.LogLevel;
import backupmanager.Entities.Backup;
import backupmanager.Enums.ErrorTypes;
import backupmanager.GUI.BackupProgressGUI;
import backupmanager.Table.BackupTable;

public class ZippingThread {
    public static Thread zipThread;
    public static void zipDirectory(String sourceDirectoryPath, String targetZipPath, Backup backup, TrayIcon trayIcon, BackupTable backupTable, BackupProgressGUI progressBar, JButton singleBackupBtn, JToggleButton autoBackupBtn, JMenuItem interruptBackupPopupItem, JMenuItem deleteBackupPopuopItem) throws IOException { // Track copied files
        Logger.logMessage("Starting zipping process", LogLevel.INFO);

        File file = new File(sourceDirectoryPath.trim());
        int totalFilesCount = file.isDirectory() ? countFilesInDirectory(file) : 1;
        if (totalFilesCount == -1) {
            Logger.logMessage("No files to zip in: " + sourceDirectoryPath, Logger.LogLevel.WARN);
            progressBar.dispose();
            BackupOperations.setError(ErrorTypes.ErrorCountingFiles, trayIcon, targetZipPath);
            BackupOperations.reEnableButtonsAndTable(singleBackupBtn, autoBackupBtn, backup, backupTable, interruptBackupPopupItem, deleteBackupPopuopItem);
            return;
        }
        
        File sourceFile = new File(sourceDirectoryPath.trim());
        if (!sourceFile.exists()) {
            Logger.logMessage("Source directory does not exist: " + sourceDirectoryPath, Logger.LogLevel.ERROR);
            BackupOperations.setError(ErrorTypes.ZippingIOError, trayIcon, targetZipPath);
            BackupOperations.reEnableButtonsAndTable(singleBackupBtn, autoBackupBtn, backup, backupTable, interruptBackupPopupItem, deleteBackupPopuopItem);
            return;
        }
        
        AtomicInteger copiedFilesCount = new AtomicInteger(0);
        
        zipThread = new Thread(() -> {
            Path sourceDir = Paths.get(sourceDirectoryPath);
            String rootFolderName = sourceDir.getFileName().toString(); // Get the root folder name

            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(targetZipPath))) {
                if (file.isFile()) {
                    addFileToZip(sourceDirectoryPath, targetZipPath, zipOut, file.toPath(), file.getName(), copiedFilesCount, totalFilesCount, backup, trayIcon, progressBar, singleBackupBtn, autoBackupBtn, backupTable, interruptBackupPopupItem, deleteBackupPopuopItem);
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
                                BackupOperations.reEnableButtonsAndTable(singleBackupBtn, autoBackupBtn, backup, backupTable, interruptBackupPopupItem, deleteBackupPopuopItem);
                                return FileVisitResult.TERMINATE; // Stop if interrupted
                            }

                            // Calculate the relative path inside the zip
                            Path targetFilePath = sourceDir.relativize(file);
                            
                            if (rootFolderName == null || rootFolderName.isEmpty()) {
                                Logger.logMessage("Root folder name is null or empty", Logger.LogLevel.ERROR);
                            }
                            if (targetFilePath == null) {
                                Logger.logMessage("Target file path is null", Logger.LogLevel.ERROR);
                            }
                            
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
                            BackupOperations.UpdateProgressPercentage(actualProgress, sourceDirectoryPath, targetZipPath, backup, trayIcon, backupTable, progressBar, singleBackupBtn, autoBackupBtn, interruptBackupPopupItem, deleteBackupPopuopItem);  // Update progress percentage

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
                                BackupOperations.reEnableButtonsAndTable(singleBackupBtn, autoBackupBtn, backup, backupTable, interruptBackupPopupItem, deleteBackupPopuopItem);
                                return FileVisitResult.TERMINATE; // Stop if interrupted
                            }
                            
                            // case when the initial folder is empty
                            if (totalFilesCount == 0) {
                                Logger.logMessage("Directory is empty: " + sourceDirectoryPath, Logger.LogLevel.WARN);
                                BackupOperations.UpdateProgressPercentage(100, sourceDirectoryPath, targetZipPath, backup, trayIcon, backupTable, progressBar, singleBackupBtn, autoBackupBtn, interruptBackupPopupItem, deleteBackupPopuopItem);
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
                Logger.logMessage("I/O error occurred while zipping directory: " + sourceDirectoryPath + ". Error: " + ioEx.getMessage(), Logger.LogLevel.ERROR, ioEx);
            } catch (SecurityException secEx) {
                Logger.logMessage("Security exception while accessing directory: " + sourceDirectoryPath + ". Error: " + secEx.getMessage(), Logger.LogLevel.ERROR, secEx);
                BackupOperations.setError(ErrorTypes.ZippingSecurityError, trayIcon, targetZipPath);
            } catch (Exception ex) {
                Logger.logMessage("Unexpected error during zipping directory: " + sourceDirectoryPath + ". Error: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
                ex.printStackTrace();
                //setError(ErrorTypes.ZippingGenericError, trayIcon, targetZipPath);
            } finally {
                Logger.logMessage("Finalizing zipping process", Logger.LogLevel.INFO);
                BackupOperations.reEnableButtonsAndTable(singleBackupBtn, autoBackupBtn, backup, backupTable, interruptBackupPopupItem, deleteBackupPopuopItem);
            }
        });

        zipThread.start(); // Start the zipping thread
    }

    private static void addFileToZip(String sourceDirectoryPath, String destinationDirectoryPath, ZipOutputStream zipOut, Path file, String zipEntryName, AtomicInteger copiedFilesCount, int totalFilesCount, Backup backup, TrayIcon trayIcon, BackupProgressGUI progressBar, JButton singleBackupBtn, JToggleButton autoBackupBtn, BackupTable backupTable, JMenuItem interruptBackupPopupItem, JMenuItem deleteBackupPopuopItem) throws IOException {
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
        BackupOperations.UpdateProgressPercentage(actualProgress, sourceDirectoryPath, destinationDirectoryPath, backup, trayIcon, backupTable, progressBar, singleBackupBtn, autoBackupBtn, interruptBackupPopupItem, deleteBackupPopuopItem);
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

    public static void StopCopyFiles() {
        if (zipThread != null && zipThread.isAlive())
            zipThread.interrupt();
    }

    public static boolean isInterrupted() {
        return zipThread.isInterrupted();
    }
}
