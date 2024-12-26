package test;

import backupmanager.Enums.ConfigKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.io.File;
import java.io.IOException;

public class TestConfigKey {
    private final String LOG_FILE_STRING = "log_file";
    private final String BACKUP_FILE_STRING = "backup_list.json";
    private final String CONFIG_FILE_STRING = "config.json";
    private final String RES_DIRECTORY_STRING = "src/main/resources/res/";
    private final String DONATE_PAGE_LINK = "https://buymeacoffee.com/denno";
    private final String ISSUE_PAGE_LINK = "https://github.com/DennisTurco/BackupManager/issues";
    private final String INFO_PAGE_LINK = "https://github.com/DennisTurco/BackupManager";
    private final String SHARD_WEBSITE = "https://www.shardpc.it/";
    private final String LOGO_IMG = "/res/img/logo.png";
    private final String SHARE_LINK = "https://github.com/DennisTurco/BackupManager/releases";
    private final String EMAIL = "assistenza@shardpc.it";

    private static File temp_file;

    @BeforeEach
    void setup() throws IOException {
        // json test
        String jsonContent = String.format("""
                             {
                             "LOG_FILE_STRING": "%s",
                             "BACKUP_FILE_STRING": "%s",
                             "CONFIG_FILE_STRING": "%s",
                             "RES_DIRECTORY_STRING": "%s",
                             "DONATE_PAGE_LINK": "%s",
                             "ISSUE_PAGE_LINK": "%s",
                             "INFO_PAGE_LINK": "%s",
                             "EMAIL": "%s",
                             "SHARD_WEBSITE": "%s",
                             "LOGO_IMG": "%s",
                             "SHARE_LINK": "%s"
                             }""",
                             LOG_FILE_STRING,
                             BACKUP_FILE_STRING,
                             CONFIG_FILE_STRING,
                             RES_DIRECTORY_STRING,
                             DONATE_PAGE_LINK,
                             ISSUE_PAGE_LINK,
                             INFO_PAGE_LINK,
                             EMAIL,
                             SHARD_WEBSITE,
                             LOGO_IMG,
                             SHARE_LINK);

        // write json test
        temp_file = File.createTempFile("src/test/resources/config_test", ".json");
        Files.write(temp_file.toPath(), jsonContent.getBytes());
    }

    @Test
    void testLoadFromJson() {
        // load the values from JSON test 
        ConfigKey.loadFromJson(temp_file.getPath());

        // check if the values are correctly loaded
        assertEquals(LOG_FILE_STRING, ConfigKey.LOG_FILE_STRING.getValue());
        assertEquals(BACKUP_FILE_STRING, ConfigKey.BACKUP_FILE_STRING.getValue());
        assertEquals(CONFIG_FILE_STRING, ConfigKey.CONFIG_FILE_STRING.getValue());
        assertEquals(RES_DIRECTORY_STRING, ConfigKey.RES_DIRECTORY_STRING.getValue());
        assertEquals(DONATE_PAGE_LINK, ConfigKey.DONATE_PAGE_LINK.getValue());
        assertEquals(ISSUE_PAGE_LINK, ConfigKey.ISSUE_PAGE_LINK.getValue());
        assertEquals(INFO_PAGE_LINK, ConfigKey.INFO_PAGE_LINK.getValue());
        assertEquals(EMAIL, ConfigKey.EMAIL.getValue());
        assertEquals(SHARD_WEBSITE, ConfigKey.SHARD_WEBSITE.getValue());
        assertEquals(LOGO_IMG, ConfigKey.LOGO_IMG.getValue());
        assertEquals(SHARE_LINK, ConfigKey.SHARE_LINK.getValue());
    }

    //@Test
    void testMissingKeys() {
        String jsonContent = String.format("""
                             {
                             "LOG_FILE_STRING": "log_file",
                             "BACKUP_FILE_STRING": "backup_list.json"
                             }""",
                             LOGO_IMG,
                             BACKUP_FILE_STRING);

        try {
            Files.write(temp_file.toPath(), jsonContent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // load the values
        ConfigKey.loadFromJson(temp_file.getPath());

        // checks
        assertEquals(CONFIG_FILE_STRING, ConfigKey.CONFIG_FILE_STRING.getValue());
        assertEquals(RES_DIRECTORY_STRING, ConfigKey.RES_DIRECTORY_STRING.getValue());
        assertEquals(DONATE_PAGE_LINK, ConfigKey.DONATE_PAGE_LINK.getValue());
    }

    @Test
    void testEmptyJsonFile() {
        String emptyJsonContent = "{}";
        try {
            Files.write(temp_file.toPath(), emptyJsonContent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ConfigKey.loadFromJson(temp_file.getPath());

        assertEquals(LOG_FILE_STRING, ConfigKey.LOG_FILE_STRING.getValue());
        assertEquals(BACKUP_FILE_STRING, ConfigKey.BACKUP_FILE_STRING.getValue());
    }

//   @Test
//   void testJsonParsingException() {
//       // Test JSON error
//       String malformedJson = String.format("""
//                              {
//                              "LOG_FILE_STRING": "log_file",
//                              "BACKUP_FILE_STRING": "backup_list.json"
//                              """,
//                              LOG_FILE_STRING,
//                              BACKUP_FILE_STRING
//                               ); // JSON error ('}' is missing)
//
//       try {
//           Files.write(temp_file.toPath(), malformedJson.getBytes());
//       } catch (IOException e) {
//           e.printStackTrace();
//       }
//
//       ConfigKey.loadFromJson(temp_file.getPath());
//       assertEquals(LOG_FILE_STRING, ConfigKey.LOG_FILE_STRING.getValue());
//       assertEquals(BACKUP_FILE_STRING, ConfigKey.BACKUP_FILE_STRING.getValue());
//   }
}
