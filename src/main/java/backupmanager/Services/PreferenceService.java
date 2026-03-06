package backupmanager.Services;


import java.io.IOException;

import backupmanager.Entities.Configurations;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.Translations;

public class PreferenceService {
    public void updatePreferences(String language, String theme) throws IOException {
        Configurations.setLanguageByLanguageName(language);
        Configurations.setTheme(theme);
        Translations.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Configurations.getLanguage().getFileName());
    }
}
