package backupmanager.Json;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import backupmanager.Enums.ConfigKey;

// Singleton class
public class JsonConfig {
    private static final Logger logger = LoggerFactory.getLogger(JsonConfig.class);

    // The field must be declared volatile so that double check lock would work correctly.
    private static volatile JsonConfig instance;

    private final String filename;
    private final String directoryPath;
    private JsonObject config;

    // The approach taken here is called double-checked locking (DCL). It
    // exists to prevent race condition between multiple threads that may
    // attempt to get singleton instance at the same time, creating separate
    // instances as a result.
    public static JsonConfig getInstance() {
        JsonConfig result = instance;

        if (result != null)
            return result;

        synchronized (JsonConfig.class) {
            if (instance == null)
                instance = new JsonConfig();
            return instance;
        }
    }

    private JsonConfig() {
        filename = ConfigKey.CONFIG_FILE_STRING.getValue();
        directoryPath = ConfigKey.CONFIG_DIRECTORY_STRING.getValue();
        loadConfig(); // Load configuration at instantiation
    }

    public boolean isMenuItemEnabled(String menuItem) {
        if (config == null) {
            logger.warn("Configuration not loaded. Cannot check menu items");
            return false;
        }

        JsonObject menuService = config.getAsJsonObject("MenuItems");
        if (menuService != null) {
            JsonElement isEnabled = menuService.get(menuItem);
            return isEnabled != null && isEnabled.getAsBoolean();
        }
        return true; // Default to true
    }

    public int readCheckForBackupTimeInterval() throws IOException {
        JsonObject backupService = getConfig("BackupService");

        if (backupService == null) {
            logger.warn("BackupService config missing, defaulting to 5 minutes");
            return 5;
        }

        JsonElement interval = backupService.get("value");

        int timeInterval = (interval != null && !interval.isJsonNull()) ? interval.getAsInt() : 5;

        logger.info("Time interval set to " + timeInterval + " minutes");
        return timeInterval;
    }

    public int getConfigValue(String key, int defaultValue) {
        try {
            JsonObject logService = getConfig(key);

            if (logService == null) {
                logger.warn("Missing config for {}, using default {}", key, defaultValue);
                return defaultValue;
            }

            JsonElement value = logService.get(key);

            if (value == null || value.isJsonNull() || !value.isJsonPrimitive()) {
                return defaultValue;
            }

            return value.getAsInt();

        } catch (IOException e) {
            logger.error("Error retrieving config value for {}", key, e);
            return defaultValue;
        }
    }

    private void loadConfig() {
        String filePath = directoryPath + filename;
        try (FileReader reader = new FileReader(filePath, StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            config = gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            logger.error("Failed to load configuration: " + e.getMessage(), e);
        }
    }

    private JsonObject getConfig(String key) throws IOException {
        if (config == null) {
            throw new IOException("Configuration not loaded.");
        }
        return config.getAsJsonObject(key);
    }
}
