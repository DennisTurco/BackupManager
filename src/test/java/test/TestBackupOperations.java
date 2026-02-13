package test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import backupmanager.BackupOperations;

class TestBackupOperations {

    @Test
    void checkInputCorrect_shouldReturnFalse_forNonExistingPaths() {
        String path1 = "/wrong/path/file.txt";
        String path2 = "/wrong/path/dir";

        assertFalse(
            BackupOperations.checkInputCorrect("backup", path1, path2, null)
        );
    }

    @Test
    void checkInputCorrect_shouldReturnFalse_forSamePaths() throws IOException {
        File file = File.createTempFile("file", ".txt");

        assertFalse(
            BackupOperations.checkInputCorrect("backup", file.getPath(), file.getPath(), null)
        );
    }

    @Test
    void checkInputCorrect_shouldReturnTrue_forValidDifferentPaths() throws IOException {
        File tempFile1 = File.createTempFile("file1", ".txt");
        File tempFile2 = File.createTempFile("file2", ".txt");

        assertTrue(
            BackupOperations.checkInputCorrect("backup", tempFile1.getPath(), tempFile2.getPath(), null)
        );
    }
}
