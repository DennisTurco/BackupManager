package backupmanager.Services;

import java.awt.TrayIcon;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;

import backupmanager.BackupOperations;
import backupmanager.Logger;
import backupmanager.Logger.LogLevel;
import backupmanager.ZipFileVisitor;
import backupmanager.Entities.Backup;
import backupmanager.Enums.ErrorTypes;
import backupmanager.Enums.ZippingContext;
import backupmanager.GUI.BackupProgressGUI;
import backupmanager.Table.BackupTable;

public class ZippingThread {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void zipDirectory(String sourceDirectoryPath, String targetZipPath, ZippingContext context) {
        Logger.logMessage("Starting zipping process", LogLevel.INFO);

        File sourceFile = new File(sourceDirectoryPath.trim());
        File targetFile = new File(targetZipPath.trim());

        if (!sourceFile.exists()) {
            handleError("Source directory does not exist: " + sourceDirectoryPath, ErrorTypes.ZippingIOError, context);
            return;
        }

        int totalFilesCount = countFilesInDirectory(sourceFile);
        if (totalFilesCount == -1) {
            handleError("No files to zip in: " + sourceDirectoryPath, ErrorTypes.ErrorCountingFiles, context);
            return;
        }

        AtomicInteger copiedFilesCount = new AtomicInteger(0);

        executorService.submit(() -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(targetZipPath))) {
                Path sourceDir = Paths.get(sourceDirectoryPath);
                Files.walkFileTree(sourceDir, new ZipFileVisitor(sourceDir, targetFile, zipOut, copiedFilesCount, totalFilesCount, context));
            } catch (IOException e) {
                Logger.logMessage("I/O error occurred while zipping directory: " + sourceDirectoryPath, Logger.LogLevel.ERROR, e);
                handleError("I/O error occurred", ErrorTypes.ZippingIOError, context);
            } finally {
                finalizeProcess(context);
            }
        });
    }

    private static void handleError(String message, ErrorTypes errorType, ZippingContext context) {
        Logger.logMessage(message, LogLevel.ERROR);
        BackupOperations.setError(errorType, context.trayIcon, null);
        BackupOperations.reEnableButtonsAndTable(context.singleBackupBtn, context.autoBackupBtn, context.backup,
                context.backupTable, context.interruptBackupPopupItem, context.deleteBackupPopupItem);
    }

    private static void finalizeProcess(ZippingContext context) {
        Logger.logMessage("Finalizing zipping process", LogLevel.INFO);
        BackupOperations.reEnableButtonsAndTable(context.singleBackupBtn, context.autoBackupBtn, context.backup,
                context.backupTable, context.interruptBackupPopupItem, context.deleteBackupPopupItem);
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

    /**
     * Attempts to gracefully stop the given ExecutorService.
     *
     * @param executor The ExecutorService to shut down.
     * @param timeout  The maximum time to wait for termination, in seconds.
     */
    public static void stopExecutorService(int timeout) {
        if (executorService == null || executorService.isShutdown()) {
            return;
        }

        executorService.shutdown(); // Reject new tasks
        try {
            // Wait for ongoing tasks to complete
            if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
                Logger.logMessage("executorService did not terminate in the given time. Forcing shutdown...", LogLevel.WARN);
                executorService.shutdownNow(); // Forcefully stop remaining tasks
                if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
                    Logger.logMessage("executorService did not terminate after forced shutdown", LogLevel.WARN);
                }
            }
        } catch (InterruptedException e) {
            Logger.logMessage("Shutdown process interrupted. Forcing shutdown...", LogLevel.ERROR, e);
            executorService.shutdownNow(); // Forcefully stop tasks on interruption
            Thread.currentThread().interrupt(); // Preserve interrupted status
        }
    }

    public static boolean isInterrupted() {
        return executorService.isShutdown() || executorService.isTerminated();
    }
}
