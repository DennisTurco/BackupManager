package backupmanager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import backupmanager.Enums.ZippingContext;

public class ZipFileVisitor extends SimpleFileVisitor<Path> {
    private final Path sourceDir;
    private final File destinationDir;
    private final ZipOutputStream zipOut;
    private final AtomicInteger copiedFilesCount;
    private final int totalFilesCount;
    private final ZippingContext context;

    public ZipFileVisitor(Path sourceDir, File destinationDIr, ZipOutputStream zipOut, AtomicInteger copiedFilesCount,
                          int totalFilesCount, ZippingContext context) {
        this.sourceDir = sourceDir;
        this.destinationDir = destinationDIr;
        this.zipOut = zipOut;
        this.copiedFilesCount = copiedFilesCount;
        this.totalFilesCount = totalFilesCount;
        this.context = context;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (Thread.currentThread().isInterrupted()) {
            Logger.logMessage("Zipping process manually interrupted", Logger.LogLevel.INFO);
            return FileVisitResult.TERMINATE; // Termina il processo se il thread è interrotto
        }

        String zipEntryName = sourceDir.relativize(dir).toString() + "/";
        Logger.logMessage("Adding directory to zip: " + zipEntryName, Logger.LogLevel.DEBUG);

        // Aggiungi l'entry per la directory
        zipOut.putNextEntry(new ZipEntry(zipEntryName));
        zipOut.closeEntry();

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (Thread.currentThread().isInterrupted()) {
            Logger.logMessage("Zipping process manually interrupted", Logger.LogLevel.INFO);
            return FileVisitResult.TERMINATE; // Termina il processo se il thread è interrotto
        }

        String zipEntryName = sourceDir.relativize(file).toString();
        Logger.logMessage("Adding file to zip: " + zipEntryName, Logger.LogLevel.DEBUG);

        // Aggiungi l'entry per il file
        zipOut.putNextEntry(new ZipEntry(zipEntryName));

        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                zipOut.write(buffer, 0, len);
            }
        }

        zipOut.closeEntry();

        // Aggiorna il progresso
        int filesCopiedSoFar = copiedFilesCount.incrementAndGet();
        int actualProgress = (int) (((double) filesCopiedSoFar / totalFilesCount) * 100);
        BackupOperations.UpdateProgressPercentage(actualProgress, sourceDir.toString(), destinationDir.toString(),
                context.backup, context.trayIcon, context.backupTable, context.progressBar,
                context.singleBackupBtn, context.autoBackupBtn, context.interruptBackupPopupItem,
                context.deleteBackupPopupItem);

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        Logger.logMessage("Failed to visit file: " + file + ". Error: " + exc.getMessage(), Logger.LogLevel.ERROR, exc);
        return FileVisitResult.CONTINUE; // Continua anche se ci sono errori
    }
}
