package backupmanager.Json;

import java.io.IOException;
import java.io.FileReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import backupmanager.Logger;

public class JSONConfigReader {

    private final String filename;
    private final String directoryPath;
    private JSONObject config;

    public JSONConfigReader(String filename, String directoryPath) {
        this.filename = filename;
        this.directoryPath = directoryPath;
        loadConfig(); // Load configuration at instantiation
    }

    public boolean isLogLevelEnabled(String level) {
        if (config == null) {
            Logger.logMessage("Configuration not loaded. Cannot check log level", Logger.LogLevel.ERROR);
            return false;
        }

        JSONObject logService = (JSONObject) config.get("LogService");
        if (logService != null) {
            Boolean isEnabled = (Boolean) logService.get(level);
            return isEnabled != null && isEnabled;
        }
        return false; // Default to false if LogService or level is missing
    }
    
    public boolean isMenuItemEnabled(String menuItem) {
        if (config == null) {
            Logger.logMessage("Configuration not loaded. Cannot check menu items", Logger.LogLevel.ERROR);
            return false;
        }

        JSONObject menuService = (JSONObject) config.get("MenuItems");
        if (menuService != null) {
            Boolean isEnabled = (Boolean) menuService.get(menuItem);
            return isEnabled != null && isEnabled;
        }
        return true; // Default to true
    }

    public int getMaxLines() {
        return getConfigValue("MaxLines", 1500); // Default to 1500
    }

    public int getLinesToKeepAfterFileClear() {
        return getConfigValue("LinesToKeepAfterFileClear", 150); // Default to 150
    }

    public int getMaxCountForSameBackup() {
        return getConfigValue("MaxCountForSameBackup", 3); // Default to 3
    }

    public int readCheckForBackupTimeInterval() throws IOException {
        int timeInterval;
        try {
            JSONObject backupService = getBackupServiceConfig();
            Long interval = (Long) backupService.get("value");

            // if the interval is null, set to default of 5 minutes
            timeInterval = (interval != null) ? interval.intValue() : 5;
        } catch (NullPointerException e) {
            Logger.logMessage("Error retrieving backup time interval, defaulting to 5 minutes: " + e.getMessage(), Logger.LogLevel.ERROR);
            timeInterval = 5;
        }

        Logger.logMessage("Time interval set to " + timeInterval + " minutes", Logger.LogLevel.INFO);
        return timeInterval;
    }
    
    private int getConfigValue(String key, int defaultValue) {
        try {
            JSONObject logService = getLogServiceConfig();
            JSONObject configValue = (JSONObject) logService.get(key);
            Long value = (Long) configValue.get("value");
            return (value != null) ? value.intValue() : defaultValue;
        } catch (IOException | NullPointerException e) {
            Logger.logMessage("Error retrieving config value for " + key + ": " + e.getMessage(), Logger.LogLevel.ERROR);
            return defaultValue;
        }
    }
    
    private void loadConfig() {
        String filePath = directoryPath + filename;
        try (FileReader reader = new FileReader(filePath)) {
            JSONParser parser = new JSONParser();
            config = (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            Logger.logMessage("Failed to load configuration: " + e.getMessage(), Logger.LogLevel.ERROR);
        }
    }

    private JSONObject getLogServiceConfig() throws IOException {
        if (config == null) {
            throw new IOException("Configuration not loaded.");
        }
        return (JSONObject) config.get("LogService");
    }

    private JSONObject getBackupServiceConfig() throws IOException {
        if (config == null) {
            throw new IOException("Configuration not loaded.");
        }
        return (JSONObject) config.get("BackupService");
    }
}
