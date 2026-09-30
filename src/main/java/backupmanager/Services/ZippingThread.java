package backupmanager.Services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.BackupOperations;
import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.ErrorType;
import backupmanager.ZipFileVisitor;

public class ZippingThread {

    private static final Logger logger = LoggerFactory.getLogger(ZippingThread.class);
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static volatile Future<?> currentTask;

    public enum Outcome { SUCCESS, INTERRUPTED, FAILED }

    /** @return true if the zip task was submitted, false if it could not even start */
    public static boolean zipDirectory(File sourceFile, File outputFile, ZippingContext context, int totalFilesCount) {
        logger.info("Starting zipping process");

        String sourceDirectoryPath = sourceFile.getAbsolutePath();
        String outupZipPath = outputFile.getAbsolutePath();

        if (!sourceFile.exists()) {
            logger.error("Source directory does not exist: {}", sourceDirectoryPath);
            BackupOperations.setError(ErrorType.ZippingIOError, context.execution().backup().getName());
            BackupOperations.completeBackup(context, outupZipPath, Outcome.FAILED, "Source path does not exist: " + sourceDirectoryPath);
            return false;
        }

        AtomicInteger copiedFilesCount = new AtomicInteger(0);

        // Ensure the executor is not shut down before submitting a task
        if (executorService.isShutdown() || executorService.isTerminated()) {
            logger.warn("ExecutorService is terminated. Re-creating the executor...");
            executorService = Executors.newSingleThreadExecutor();  // Recreate the executor
        }

        currentTask = executorService.submit(() -> {
            Outcome outcome = Outcome.FAILED;
            String error = null;
            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outupZipPath))) {
                Path sourceDir = Paths.get(sourceDirectoryPath);

                if (sourceFile.isFile())
                    addFileToZip(sourceDirectoryPath, outupZipPath, zipOut, sourceFile.toPath(), sourceFile.getName(), copiedFilesCount, totalFilesCount, context);
                else
                    Files.walkFileTree(sourceDir, new ZipFileVisitor(sourceDir, outputFile, zipOut, copiedFilesCount, totalFilesCount, context));

                outcome = Thread.currentThread().isInterrupted() ? Outcome.INTERRUPTED : Outcome.SUCCESS;
            } catch (ClosedByInterruptException e) {
                // interrupting a thread blocked on NIO file I/O surfaces as this exception
                outcome = Outcome.INTERRUPTED;
            } catch (IOException | RuntimeException e) {
                // e.g. a file locked by another program, disk full, access denied
                logger.error("Error while zipping \"" + sourceDirectoryPath + "\": " + e.getMessage(), e);
                BackupOperations.setError(ErrorType.ZippingIOError, context.execution().backup().getName());
                error = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            } finally {
                // The ZipOutputStream is closed by now (try-with-resources runs before finally), so
                // the output file is complete or can safely be deleted. Thread.interrupted() clears the
                // flag — this is a pooled single-thread executor, so leaving it set would make the
                // *next* submitted backup look interrupted too.
                if (Thread.interrupted() && outcome == Outcome.SUCCESS) outcome = Outcome.INTERRUPTED;
                logger.info("Finalizing zipping process: {}", outcome);
                BackupOperations.completeBackup(context, outupZipPath, outcome, error);
            }
        });
        return true;
    }

    private static void addFileToZip(String sourceDirectoryPath, String destinationDirectoryPath, ZipOutputStream zipOut, Path file, String zipEntryName, AtomicInteger copiedFilesCount, int totalFilesCount, ZippingContext context) throws IOException {        
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
        BackupOperations.updateProgressPercentage(actualProgress, sourceDirectoryPath, destinationDirectoryPath, context, zipEntryName, filesCopiedSoFar, totalFilesCount);
    }

    /**
     * Attempts to gracefully stop the given ExecutorService.
     *
     * @param executor The ExecutorService to shut down.
     * @param timeout  The maximum time to wait for termination, in seconds.
     */
    public static void stopExecutorService(int timeout) {
        logger.debug("Stopping zipping executor");

        if (executorService == null || executorService.isShutdown()) {
            logger.debug("executorService == null || executorService.isShutdown()");
            return;
        }

        executorService.shutdown(); // Reject new tasks
        try {
            // Wait for ongoing tasks to complete
            if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
                logger.warn("executorService did not terminate in the given time. Forcing shutdown...");
                executorService.shutdownNow(); // Forcefully stop remaining tasks
                if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
                    logger.warn("executorService did not terminate after forced shutdown");
                }
            }
            logger.info("Zipping executor stopped");
        } catch (InterruptedException e) {
            logger.error("Shutdown process interrupted. Forcing shutdown... With message: " + e.getMessage(), e);
            executorService.shutdownNow(); // Forcefully stop tasks on interruption
            Thread.currentThread().interrupt(); // Preserve interrupted status
        }
    }

    public static boolean isInterrupted() {
        return executorService.isShutdown() || executorService.isTerminated();
    }

    /**
     * Interrupts the currently running zip task, if any. The task itself checks
     * Thread.interrupted() between files/directories (see ZipFileVisitor) and stops
     * cleanly, marking the backup request as TERMINATED and removing the partial file.
     *
     * @return true if a running task was found and interrupted, false if nothing was running.
     */
    public static boolean interruptCurrentTask() {
        Future<?> task = currentTask;
        if (task != null && !task.isDone()) {
            logger.info("Interrupting current zipping task");
            task.cancel(true);
            return true;
        }
        return false;
    }
}
