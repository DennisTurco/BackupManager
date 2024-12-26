package backupmanager.Entities;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.ThemesEnum;
import backupmanager.Logger;
import backupmanager.Logger.LogLevel;

import static backupmanager.GUI.BackupManagerGUI.OpenExceptionMessage;

public class Preferences {
    private static LanguagesEnum language;
    private static ThemesEnum theme;
    private static BackupList backupList;

    public static void loadPreferencesFromJSON() {
        try (FileReader reader = new FileReader(ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.PREFERENCES_FILE_STRING.getValue())) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            
            language = getLanguageFromJson(jsonObject);
            theme = getThemeFromJson(jsonObject);
            backupList = getBackupListFromJson(jsonObject);

            updatePreferencesToJSON();

        } catch (FileNotFoundException e) {
            Logger.logMessage("Preferences file not found. Using default preferences.", Logger.LogLevel.WARN);
            updatePreferencesToJSON(); // Create the JSON file with default preferences
        } catch (Exception ex) {
            Logger.logMessage("An error occurred while loading preferences: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            OpenExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    public static void updatePreferencesToJSON() {
        try (FileWriter writer = new FileWriter(ConfigKey.CONFIG_DIRECTORY_STRING.getValue() + ConfigKey.PREFERENCES_FILE_STRING.getValue())) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("Language", language.getFileName());
            jsonObject.addProperty("Theme", theme.getThemeName());

            JsonObject backupListObject = new JsonObject();
            backupListObject.addProperty("Directory", backupList.getDirectory());
            backupListObject.addProperty("File", backupList.getFile());  

            jsonObject.add("BackupList", backupListObject);

            // Convert JsonObject to JSON string using Gson
            Gson gson = new Gson();
            gson.toJson(jsonObject, writer);

        } catch (IOException ex) {
            Logger.logMessage("An error occurred during updating preferences to json operation: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            OpenExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    private static LanguagesEnum getLanguageFromJson(JsonObject jsonObject) {
        if (jsonObject.has("Language") && !jsonObject.get("Language").isJsonNull()) {
            String languageFileName = jsonObject.get("Language").getAsString();
            for (LanguagesEnum lang : LanguagesEnum.values()) {
                if (lang.getFileName().equals(languageFileName)) {
                    return lang;
                }
            }
        }
        return LanguagesEnum.ENG;
    }
    
    private static ThemesEnum getThemeFromJson(JsonObject jsonObject) {
        if (jsonObject.has("Theme") && !jsonObject.get("Theme").isJsonNull()) {
            String themeName = jsonObject.get("Theme").getAsString();
            for (ThemesEnum t : ThemesEnum.values()) {
                if (t.getThemeName().equals(themeName)) {
                    return t;
                }
            }
        }
        return ThemesEnum.INTELLIJ;
    }
    
    private static BackupList getBackupListFromJson(JsonObject jsonObject) {
        if (jsonObject.has("BackupList") && !jsonObject.get("BackupList").isJsonNull()) {
            JsonObject backupListObject = jsonObject.getAsJsonObject("BackupList");

            String directory = backupListObject.has("Directory") && !backupListObject.get("Directory").isJsonNull()
                ? backupListObject.get("Directory").getAsString()
                : ConfigKey.RES_DIRECTORY_STRING.getValue();

            String file = backupListObject.has("File") && !backupListObject.get("File").isJsonNull()
                ? backupListObject.get("File").getAsString()
                : ConfigKey.BACKUP_FILE_STRING.getValue() + ConfigKey.VERSION.getValue() + ".json";

            return new BackupList(directory, file);
        }
        return getDefaultBackupList();
    }

    public static LanguagesEnum getLanguage() {
        return language;
    }
    public static ThemesEnum getTheme() {
        return theme;
    }
    public static BackupList getBackupList() {
        return backupList;
    }
    public static BackupList getDefaultBackupList() {
        return new BackupList(
            ConfigKey.RES_DIRECTORY_STRING.getValue(),
            ConfigKey.BACKUP_FILE_STRING.getValue() + ConfigKey.VERSION.getValue() + ".json"
        );
    }
    public static void setLanguage(LanguagesEnum language) {
        Preferences.language = language;
    }
    public static void setTheme(ThemesEnum theme) {
        Preferences.theme = theme;
    }
    public static void setBackupList(BackupList backupList) {
        Preferences.backupList = backupList;
    }
    public static void setLanguage(String selectedLanguage) {
        try {
            for (LanguagesEnum lang : LanguagesEnum.values()) {
                if (lang.getLanguageName().equalsIgnoreCase(selectedLanguage)) {
                    language = lang;
                    Logger.logMessage("Language set to: " + language.getLanguageName(), LogLevel.INFO);
                    return;
                }
            }
            Logger.logMessage("Invalid language name: " + selectedLanguage, LogLevel.WARN);
        } catch (Exception ex) {
            Logger.logMessage("An error occurred during setting language operation: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            OpenExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
    public static void setTheme(String selectedTheme) {
        try {
            for (ThemesEnum t : ThemesEnum.values()) {
                if (t.getThemeName().equalsIgnoreCase(selectedTheme)) {
                    theme = t;
                    Logger.logMessage("Theme set to: " + theme.getThemeName(), LogLevel.INFO);
                    return;
                }
            }
            Logger.logMessage("Invalid theme name: " + selectedTheme, LogLevel.WARN);
        } catch (Exception ex) {
            Logger.logMessage("An error occurred during setting theme operation: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
            OpenExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
}
