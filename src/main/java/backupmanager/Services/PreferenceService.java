package backupmanager.Services;


import java.io.IOException;

import backupmanager.Entities.Confingurations;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.TranslationLoaderEnum;

public class PreferenceService {
    public void updatePreferences(String language, String theme) throws IOException {
        Confingurations.setLanguageByLanguageName(language);
        Confingurations.setTheme(theme);
        TranslationLoaderEnum.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + Confingurations.getLanguage().getFileName());
    }
}
