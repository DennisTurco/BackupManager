package backupmanager.Managers;

import java.io.IOException;
import java.util.Locale;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.Translations;
import backupmanager.database.Repositories.ConfigurationRepository;

/**
 * The active language is stored in the database (Configurations.LANGUAGE), the same value the
 * Settings page reads and writes. It is resolved once at startup, before the REST API starts, so
 * the UI always finds it already set.
 */
public class LanguageManager {
    private static final Logger logger = LoggerFactory.getLogger(LanguageManager.class);

    private static final String LANGUAGE_CONFIG_CODE = "LANGUAGE";

    // Where the old Swing app kept the language (Windows registry), read only to migrate it
    private static final String LEGACY_PREFERENCES_NODE = "/BackupManager";
    private static final String LEGACY_PREFERENCES_KEY = "language";

    private static LanguagesEnum current = LanguagesEnum.getDefault();

    /**
     * Loads the stored language. On the first start (nothing stored yet) it picks the language of
     * the old app if there was one, otherwise the PC language, and saves it to the database.
     */
    public static void initialize() {
        LanguagesEnum stored = fromCodeOrNull(ConfigurationRepository.getConfigurationValueByCode(LANGUAGE_CONFIG_CODE));
        if (stored != null) {
            current = stored;
        } else {
            LanguagesEnum legacy = readLegacyPreference();
            current = legacy != null ? legacy : detectPcLanguage();
            ConfigurationRepository.updateConfigurationValueByCode(LANGUAGE_CONFIG_CODE, current.getCode());
            logger.info("Initial language set to {} ({})", current.getLanguageName(),
                legacy != null ? "migrated from the previous version" : "from the PC language");
        }
        loadPreferredLanguage();
    }

    // Used by the REST API / React Settings page, which deals in short ISO codes (e.g. "it");
    // the caller persists the value, this only switches the active translations
    public static void setLanguageByCode(String code) {
        setLanguage(LanguagesEnum.fromCode(code));
    }

    public static void setLanguage(LanguagesEnum language) {
        current = language;
        logger.info("Language setted to: {}", language.getLanguageName());
        loadPreferredLanguage();
    }

    public static void loadPreferredLanguage() {
        try {
            Translations.loadTranslations(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue() + current.getFileName());
        } catch (IOException ex) {
            logger.error("An error occurred during loading preferences: {}", ex.getMessage(), ex);
        }
    }

    public static LanguagesEnum getLanguage() {
        return current;
    }

    // Java takes this from the Windows regional format, which normally matches the display language
    private static LanguagesEnum detectPcLanguage() {
        LanguagesEnum detected = fromCodeOrNull(Locale.getDefault().getLanguage());
        return detected != null ? detected : LanguagesEnum.getDefault();
    }

    private static LanguagesEnum readLegacyPreference() {
        try {
            String fileName = Preferences.userRoot().node(LEGACY_PREFERENCES_NODE).get(LEGACY_PREFERENCES_KEY, null);
            if (fileName == null) return null;
            for (LanguagesEnum lang : LanguagesEnum.values()) {
                if (lang.getFileName().equalsIgnoreCase(fileName)) return lang;
            }
        } catch (RuntimeException e) {
            logger.warn("Could not read the previous language preference: {}", e.getMessage());
        }
        return null;
    }

    private static LanguagesEnum fromCodeOrNull(String code) {
        if (code == null || code.isBlank()) return null;
        try {
            return LanguagesEnum.fromCode(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
