// package test;

// import backupmanager.Json.JSONConfigReader;
// import backupmanager.BackupOperations;

// import org.junit.jupiter.api.*;
// import org.mockito.Mock;

// import static org.junit.Assert.assertFalse;
// import static org.junit.Assert.assertTrue;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.when;

// import java.io.File;
// import java.io.IOException;

// public class TestBackupOperations {
    
//     private static File temp_file;

//     @Mock
//     private static JSONConfigReader mockConfigReader;

//     @BeforeAll
//     static void setUpBeforeClass() throws IOException {
//         // Create test configuration file
//         temp_file = File.createTempFile("src/test/resources/config_test", ".json");

//         // Set up the mock config reader
//         mockConfigReader = mock(JSONConfigReader.class);
//         when(mockConfigReader.getMaxLines()).thenReturn(100);
//         when(mockConfigReader.getLinesToKeepAfterFileClear()).thenReturn(50);
//         when(mockConfigReader.isLogLevelEnabled("INFO")).thenReturn(true);
//         when(mockConfigReader.isLogLevelEnabled("DEBUG")).thenReturn(true);
//         when(mockConfigReader.isLogLevelEnabled("WARN")).thenReturn(true);
//         when(mockConfigReader.isLogLevelEnabled("ERROR")).thenReturn(true);

//         Logger.configReader = mockConfigReader;

//         Logger.setLogFilePath(temp_file.getPath());
//     }

//     @BeforeEach
//     void setup() throws IOException {
//         // Reset the console logging flag before each test
//         temp_file = File.createTempFile("src/test/resources/config_test", ".json");
//         Logger.setConsoleLoggingEnabled(false);
//     }

//     @Test
//     void testCheckInputCorrectWrongFilePaths() {
//         String path1 = "/wrong/path/file.txt";
//         String path2 = "/wrong/path/dir";    
//         assertFalse(BackupOperations.CheckInputCorrect("backup", path1, path2, null));
//     }

//     @Test
//     void testCheckInputCorrectSameFilePaths() throws IOException {
//         File file1 = File.createTempFile("file1", ".csv");
//         File file2 = file1;
//         assertFalse(BackupOperations.CheckInputCorrect("backup", file1.getPath(), file2.getPath(), null));
//     }

//     @Test
//     void testCheckInputCorrectCorrectFilePaths() throws IOException {
//         File tempFile1 = File.createTempFile("file1", ".txt");
//         File tempFile2 = File.createTempFile("file2", ".txt");  
//         assertTrue(BackupOperations.CheckInputCorrect("backup", tempFile1.getPath(), tempFile2.getPath(), null));
//     }
// }
