package backupmanager.Managers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.Translations;
import backupmanager.Utils.AppPreferences;

public class LanguageManager {
    private static final Logger logger = LoggerFactory.getLogger(LanguageManager.class);

    public static void setLanguage(String language) {
        var lang = getLanguageByLanguageName(language);
        setLanguage(lang);
    }

    // Used by the REST API / React Settings page, which deals in short ISO codes (e.g. "it") rather than display names
    public static void setLanguageByCode(String code) {
        setLanguage(LanguagesEnum.fromCode(code));
    }

    public static void setLanguage(LanguagesEnum language) {
        AppPreferences.setLanguage(language.getFileName());
        logger.info("Language setted to: {}", language.getLanguageName());
        loadPreferredLanguage();
    }

    public static void loadPreferredLanguage() {
        try {
            Translations.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + LanguageManager.getLanguage().getFileName());
        } catch (IOException ex) {
            logger.error("An error occurred during loading preferences: {}", ex.getMessage(), ex);
        }
    }

    public static LanguagesEnum getLanguage() {
        return getLanguageByFileName(AppPreferences.getLanguage());
    }

    private static LanguagesEnum getLanguageByFileName(String filename) {
        for (LanguagesEnum lang : LanguagesEnum.values()) {
            if (lang.getFileName().equalsIgnoreCase(filename))
                return lang;
        }
        throw new IllegalArgumentException("Impossible fetch language with filename: " + filename);
    }

    private static LanguagesEnum getLanguageByLanguageName(String language) {
        for (LanguagesEnum lang : LanguagesEnum.values()) {
            if (lang.getLanguageName().equalsIgnoreCase(language))
                return lang;
        }
        throw new IllegalArgumentException("Impossible fetch language with name: " + language);
    }
}
