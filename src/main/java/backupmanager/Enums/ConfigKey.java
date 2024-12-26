package backupmanager.Enums;

import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public enum ConfigKey {
    LOG_FILE_STRING,
    BACKUP_FILE_STRING,
    CONFIG_FILE_STRING,
    PREFERENCES_FILE_STRING,
    RES_DIRECTORY_STRING,
    LANGUAGES_DIRECTORY_STRING,
    CONFIG_DIRECTORY_STRING,
    DONATE_PAGE_LINK,
    ISSUE_PAGE_LINK,
    INFO_PAGE_LINK,
    EMAIL,
    SHARD_WEBSITE,
    LOGO_IMG,
    SHARE_LINK,
    VERSION,
    GUI_WIDTH,
    GUI_HEIGHT;

    private static final Map<ConfigKey, String> configValues = new EnumMap<>(ConfigKey.class);

    public static void loadFromJson(String filePath) {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(filePath)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            for (ConfigKey key : ConfigKey.values()) {
                if (jsonObject.containsKey(key.name())) {
                    configValues.put(key, (String) jsonObject.get(key.name()));
                }
            }
        } catch (IOException | ParseException ex) {
            ex.printStackTrace();
        }
    }

    public String getValue() {
        return configValues.get(this);
    }
}