package backupmanager;

import java.io.IOException;
import java.util.Arrays;

import backupmanager.Entities.Preferences;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.TranslationLoaderEnum;
import backupmanager.GUI.BackupManagerGUI;
import static backupmanager.GUI.BackupManagerGUI.openExceptionMessage;
import backupmanager.Json.JSONConfigReader;
import backupmanager.Logger.LogLevel;
import backupmanager.Services.BackugrundService;

public class MainApp {
    private static final String CONFIG = "src/main/resources/res/config/config.json";

    public static void main(String[] args) {
        // load config keys
        ConfigKey.loadFromJson(CONFIG);
        Logger.configReader = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());

        // load preferred language
        try {
            Preferences.loadPreferencesFromJSON();
            TranslationLoaderEnum.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Preferences.getLanguage().getFileName());
        } catch (IOException ex) {
            Logger.logMessage("An error occurred during loading preferences: " + ex.getMessage(), LogLevel.ERROR, ex);
        }

        boolean isBackgroundMode = args.length > 0 && args[0].equalsIgnoreCase("--background");

        // check argument correction
        if (!isBackgroundMode && args.length > 0) {
            Logger.logMessage("Argument \""+ args[0] +"\" not valid!", Logger.LogLevel.WARN);
            throw new IllegalArgumentException("Argument passed is not valid!");
        }
        
        Logger.logMessage("Application started", Logger.LogLevel.INFO);
        Logger.logMessage("Background mode: " + isBackgroundMode, Logger.LogLevel.DEBUG);
        
        if (isBackgroundMode) {
            Logger.logMessage("Backup service starting in the background", Logger.LogLevel.INFO);
            BackugrundService service = new BackugrundService();
            try {
                service.startService();
            } catch (IOException ex) {
                Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
                openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
            }
        }
        else if (!isBackgroundMode) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                BackupManagerGUI gui = new BackupManagerGUI();
                gui.showWindow();
            });
        }
    }
}
