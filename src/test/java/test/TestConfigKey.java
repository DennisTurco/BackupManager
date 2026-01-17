package test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

import backupmanager.Enums.ConfigKey;

public class TestConfigKey {

    private final String LOG_FILE_STRING = "log_file";
    private final String BACKUP_LIST_FILE_STRING = "backup_list.json";
    private final String CONFIG_FILE_STRING = "config.json";
    private final String RES_DIRECTORY_STRING = "src/main/resources/res/";
    private final String DONATE_PAGE_LINK = "https://buymeacoffee.com/denno";
    private final String ISSUE_PAGE_LINK = "https://github.com/DennisTurco/BackupManager/issues";
    private final String INFO_PAGE_LINK = "https://github.com/DennisTurco/BackupManager";
    private final String SHARD_WEBSITE = "https://www.shardpc.it/";
    private final String LOGO_IMG = "/res/img/logo.png";
    private final String SHARE_LINK = "https://github.com/DennisTurco/BackupManager/releases";
    private final String EMAIL = "assistenza@shardpc.it";

    private static File tempFile;

    @Test
    public void equals_shouldReturnTrue_forJsonConfigFileNotEmpty() throws IOException {
        createAndSetupTempFileWithData();
        deleteTempFile();

        assertEquals(LOG_FILE_STRING, ConfigKey.LOG_FILE_STRING.getValue());
        assertEquals(BACKUP_LIST_FILE_STRING, ConfigKey.BACKUP_FILE_STRING.getValue());
        assertEquals(CONFIG_FILE_STRING, ConfigKey.CONFIG_FILE_STRING.getValue());
        assertEquals(RES_DIRECTORY_STRING, ConfigKey.RES_DIRECTORY_STRING.getValue());
        assertEquals(ISSUE_PAGE_LINK, ConfigKey.ISSUE_PAGE_LINK.getValue());
        assertEquals(INFO_PAGE_LINK, ConfigKey.INFO_PAGE_LINK.getValue());
        assertEquals(EMAIL, ConfigKey.EMAIL.getValue());
        assertEquals(SHARD_WEBSITE, ConfigKey.SHARD_WEBSITE.getValue());
        assertEquals(LOGO_IMG, ConfigKey.LOGO_IMG.getValue());
        assertEquals(SHARE_LINK, ConfigKey.SHARE_LINK.getValue());
    }

    @Test
    public void equals_shouldReturnFalse_forEmptyJsonObject() throws IOException {
        createAndSetupTempFileWithoutData();
        deleteTempFile();

        assertNotEquals(LOG_FILE_STRING, ConfigKey.LOG_FILE_STRING.getValue());
        assertNotEquals(BACKUP_LIST_FILE_STRING, ConfigKey.BACKUP_FILE_STRING.getValue());
    }

    private void createAndSetupTempFileWithData() throws IOException {
        String jsonContent = String.format("""
                             {
                             "LOG_FILE_STRING": "%s",
                             "BACKUP_LIST_FILE_STRING": "%s",
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
                BACKUP_LIST_FILE_STRING,
                CONFIG_FILE_STRING,
                RES_DIRECTORY_STRING,
                DONATE_PAGE_LINK,
                ISSUE_PAGE_LINK,
                INFO_PAGE_LINK,
                EMAIL,
                SHARD_WEBSITE,
                LOGO_IMG,
                SHARE_LINK);
        createFileAndLoad(jsonContent);
    }

    private void createAndSetupTempFileWithoutData() throws IOException {
        String emptyJsonContent = "{}";
        createFileAndLoad(emptyJsonContent);
    }

    private void createFileAndLoad(String jsonString) throws IOException {
        tempFile = File.createTempFile("src/test/resources/config_test", ".json");
        Files.write(tempFile.toPath(), jsonString.getBytes());
        ConfigKey.loadFromJson(tempFile.getPath());
    }

    private void deleteTempFile() {
        if (tempFile.exists()) tempFile.delete();
    }
}
