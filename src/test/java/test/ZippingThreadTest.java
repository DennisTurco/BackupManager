package test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import backupmanager.Entities.ZippingContext;
import backupmanager.Enums.BackupTriggeredEnum;
import backupmanager.Services.ZippingThread;

class ZippingThreadTest {

    @TempDir
    Path tempDir;

    @Test
    void zipDirectory_shouldCreateZipWithFiles() throws Exception {
        Path sourceDir = tempDir.resolve("source");
        Path zipFile = tempDir.resolve("backup.zip");

        sourceDir.toFile().mkdirs();

        File file1 = sourceDir.resolve("file1.txt").toFile();
        try (FileOutputStream fos = new FileOutputStream(file1)) {
            fos.write("hello".getBytes());
        }

        ZippingContext context = ZippingContext.create(null, null, null, null, null, null, BackupTriggeredEnum.USER);

        ZippingThread.zipDirectory(sourceDir.toString(), zipFile.toString(),context);

        waitUntilExists(zipFile.toFile(), 5);

        assertTrue(zipFile.toFile().exists(), "ZIP file should exist");

        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            assertNotNull(zip.getEntry("file1.txt"));
        }
    }

    private void waitUntilExists(File file, int timeoutSeconds) throws InterruptedException {
        int waited = 0;
        while (!file.exists() && waited < timeoutSeconds * 10) {
            Thread.sleep(100);
            waited++;
        }
    }
}
