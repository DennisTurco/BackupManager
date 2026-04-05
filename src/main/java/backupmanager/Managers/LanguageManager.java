package backupmanager.Managers;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Enums.LanguagesEnum;
import backupmanager.utils.AppPreferences;

public class LanguageManager {
    private static final Logger logger = LoggerFactory.getLogger(LanguageManager.class);

    public static void setLanguage(LanguagesEnum language) {
        String fileName = language.getFileName();
        AppPreferences.setLanguage(fileName);
        logger.info("Language setted to: {}", language.getLanguageName());
    }

    public static void setLanguage(String language) {
        var lang = getLanguageByLanguageName(language);
        setLanguage(lang);
    }


    public static LanguagesEnum getLanguage() {
        return getLanguageInPreferences();
    }

    private static LanguagesEnum getLanguageInPreferences() {
        String langPref = AppPreferences.getLanguage();
        String filename = !langPref.isEmpty() ? langPref : LanguagesEnum.getDefault().getFileName();

        try {
            return getLanguageByFileName(filename);
        } catch (Exception ex) {
            logger.error("An error occurred during fetching language: {}", ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
        return LanguagesEnum.getDefault();
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
