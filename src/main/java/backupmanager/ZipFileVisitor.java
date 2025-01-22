package backupmanager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.ZippingContext;

public class ZipFileVisitor extends SimpleFileVisitor<Path> {
    private static final Logger logger = LoggerFactory.getLogger(ZipFileVisitor.class);
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
            logger.info("Zipping process manually interrupted");
            return FileVisitResult.TERMINATE; // Termina il processo se il thread è interrotto
        }

        String zipEntryName = sourceDir.relativize(dir).toString() + "/";
        logger.debug("Adding directory to zip: " + zipEntryName);

        // Aggiungi l'entry per la directory
        zipOut.putNextEntry(new ZipEntry(zipEntryName));
        zipOut.closeEntry();

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (Thread.currentThread().isInterrupted()) {
            logger.info("Zipping process manually interrupted");
            return FileVisitResult.TERMINATE; // Termina il processo se il thread è interrotto
        }

        String zipEntryName = sourceDir.relativize(file).toString();
        logger.debug("Adding file to zip: " + zipEntryName);

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
        BackupOperations.UpdateProgressPercentage(actualProgress, sourceDir.toString(), destinationDir.toString(), context);

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        logger.error("Failed to visit file: " + file + ". Error: " + exc.getMessage(), exc);
        return FileVisitResult.CONTINUE; // Continua anche se ci sono errori
    }
}
