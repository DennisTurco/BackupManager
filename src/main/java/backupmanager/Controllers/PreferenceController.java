package backupmanager.Controllers;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.GUI.BackupManagerGUI;
import backupmanager.Managers.ExceptionManager;
import backupmanager.Services.PreferenceService;

public record PreferenceController (PreferenceService service, BackupManagerGUI mainGui) {
    private static final Logger logger = LoggerFactory.getLogger(PreferenceController.class);

    public void applyPreferences(String language, String theme) {
        logger.info("Updating preferences -> Language:  {}; Theme: ()", language, theme);

        try {
            service.updatePreferences(language, theme);
            mainGui.reloadPreferences();
        } catch (IOException ex) {
            logger.error("An error occurred during applying preferences: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
}
