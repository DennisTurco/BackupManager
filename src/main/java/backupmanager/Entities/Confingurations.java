package backupmanager.Entities;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.ThemesEnum;
import backupmanager.Managers.ExceptionManager;
import backupmanager.database.Repositories.ConfigurationRepository;

public class Confingurations {
    private static final Logger logger = LoggerFactory.getLogger(Confingurations.class);
    private static LanguagesEnum language;
    private static ThemesEnum theme;
    private static boolean subscriptionNedded; // if true the subscription is needed to use the backuground service

    public static void loadAllConfigurations() {
        setLanguageByFileName(ConfigurationRepository.getConfigurationValueByCode("Language"));
        setTheme(ConfigurationRepository.getConfigurationValueByCode("Theme"));
        setSubscriptionNedded(ConfigurationRepository.getConfigurationValueByCode("SubscriptionNedded"));
    }

    // i don't want to update the subscription value from the code. for now the only method is doing manually
    public static void updateAllConfigurations() {
        ConfigurationRepository.updateConfigurationValueByCode("Language", language.getFileName());
        ConfigurationRepository.updateConfigurationValueByCode("Theme", theme.getThemeName());
    }

    public static void setLanguage(LanguagesEnum language) {
        Confingurations.language = language;
    }

    public static void setLanguageByLanguageName(String selectedLanguage) {
        try {
            for (LanguagesEnum lang : LanguagesEnum.values()) {
                if (lang.getLanguageName().equalsIgnoreCase(selectedLanguage)) {
                    language = lang;
                    logger.info("Language setted to: " + language.getLanguageName());
                    return;
                }
            }
            logger.warn("Invalid language name: " + selectedLanguage);
        } catch (Exception ex) {
            logger.error("An error occurred during setting language operation: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    private static void setLanguageByFileName(String filename) {
        try {
            for (LanguagesEnum lang : LanguagesEnum.values()) {
                if (lang.getFileName().equalsIgnoreCase(filename)) {
                    language = lang;
                    logger.info("Language setted to: " + language.getLanguageName());
                    return;
                }
            }
            logger.warn("Invalid file name: " + filename);
        } catch (Exception ex) {
            logger.error("An error occurred during setting language operation: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    public static void setTheme(ThemesEnum theme) {
        Confingurations.theme = theme;
    }
    public static void setTheme(String selectedTheme) {
        try {
            for (ThemesEnum t : ThemesEnum.values()) {
                if (t.getThemeName().equalsIgnoreCase(selectedTheme)) {
                    theme = t;
                    logger.info("Theme set to: " + theme.getThemeName());
                    return;
                }
            }
            logger.warn("Invalid theme name: " + selectedTheme);
        } catch (Exception ex) {
            logger.error("An error occurred during setting theme operation: " + ex.getMessage(), ex);
            ExceptionManager.openExceptionMessage(ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }

    private static void setSubscriptionNedded(String subscriptionValue) {
        subscriptionValue = subscriptionValue.trim().toLowerCase();
        subscriptionNedded = subscriptionValue.equals("true") || subscriptionValue.equals("1");
    }

    public static LanguagesEnum getLanguage() {
        return language;
    }

    public static ThemesEnum getTheme() {
        return theme;
    }

    public static boolean isSubscriptionNedded() {
        return subscriptionNedded;
    }
}
