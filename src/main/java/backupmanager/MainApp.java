package backupmanager;

import java.io.IOException;
import java.util.Arrays;
import org.json.simple.parser.ParseException;

import static backupmanager.GUI.BackupManagerGUI.OpenExceptionMessage;
import backupmanager.Entities.Preferences;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.TranslationLoaderEnum;
import backupmanager.GUI.BackupManagerGUI;
import backupmanager.Json.JSONConfigReader;
import backupmanager.Services.BackupService;
import backupmanager.Logger.LogLevel;

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
        } catch (IOException | ParseException ex) {
            Logger.logMessage("An error occurred during loading preferences: ", LogLevel.DEBUG, ex);
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
            BackupService service = new BackupService();
            try {
                service.startService();
            } catch (IOException ex) {
                Logger.logMessage("An error occurred: " + ex.getMessage(), Logger.LogLevel.ERROR, ex);
                OpenExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
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
