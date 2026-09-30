package backupmanager.Enums;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import backupmanager.Managers.LanguageManager;

public class Translations {

    private static final Logger logger = LoggerFactory.getLogger(Translations.class);

    // Only categories with keys still consumed by the Java backend (email templates, CSV export
    // headers). The React UI reads the same JSON files directly via GET /api/translations/{code}
    // (see backupmanager.api.routes.TranslationsRoutes) and is not constrained by this enum —
    // its keys live under the "ReactUI" category, which this loader simply ignores.
    public enum TCategory {
        BACKUP_LIST("BackupList"),
        ;

        private final String categoryName;
        private final Map<TKey, String> translations = new HashMap<>();

        TCategory(String categoryName) {
            this.categoryName = categoryName;
        }

        public void addTranslation(TKey key, String value) {
            translations.put(key, value);
        }

        // Updated getTranslation method
        public String getTranslation(TKey key) {
            return translations.getOrDefault(key, key.getDefaultValue());
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void clearTranslations() {
            translations.clear();
        }
    }

    public enum TKey {
        // BackupList — CSV export headers (GET /api/backups/export.csv)
        BACKUP_NAME_COLUMN(TCategory.BACKUP_LIST, "BackupNameColumn", "Backup Name"),
        INITIAL_PATH_COLUMN(TCategory.BACKUP_LIST, "InitialPathColumn", "Initial Path"),
        DESTINATION_PATH_COLUMN(TCategory.BACKUP_LIST, "DestinationPathColumn", "Destination Path"),
        LAST_BACKUP_COLUMN(TCategory.BACKUP_LIST, "LastBackupColumn", "Last Backup"),
        AUTOMATIC_BACKUP_COLUMN(TCategory.BACKUP_LIST, "AutomaticBackupColumn", "Automatic Backup"),
        NEXT_BACKUP_DATE_COLUMN(TCategory.BACKUP_LIST, "NextBackupDateColumn", "Next Backup Date"),
        TIME_INTERVAL_COLUMN(TCategory.BACKUP_LIST, "TimeIntervalColumn", "Interval (gg.HH:mm)"),
        MAX_BACKUPS_COLUMN(TCategory.BACKUP_LIST, "MaxBackupsColumn", "Max Backups To Keep"),
        ;

        private final TCategory category;
        private final String keyName;
        private final String defaultValue;

        private static final Map<String, TKey> lookup = new HashMap<>();

        static {
            for (TKey key : TKey.values()) {
                lookup.put(key.keyName, key);
            }
        }

        // Constructor to assign both key and default value
        private TKey(TCategory category, String keyName, String defaultValue) {
            this.category = category;
            this.keyName = keyName;
            this.defaultValue = defaultValue;
        }

        // Lookup by keyName (JSON key)
        public static TKey fromKeyName(String keyName) {
            return lookup.get(keyName);
        }

        public TCategory getCategory() { return category; }
        public String getKeyName() { return keyName; }
        public String getDefaultValue() { return defaultValue; }
    }

    public static String get(TKey key) {
        TCategory category = key.getCategory();
        return category.getTranslation(key);
    }

    public static void loadTranslations(String filePath) throws IOException {
        Gson gson = new Gson();

        // Clear previous translations to avoid stale values when switching languages
        for (TCategory c : TCategory.values()) {
            c.clearTranslations();
        }

        try (FileReader reader = new FileReader(filePath, StandardCharsets.UTF_8)) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            for (TCategory category : TCategory.values()) {
                JsonObject categoryTranslations = jsonObject.getAsJsonObject(category.getCategoryName());

                if (categoryTranslations == null) {
                    logger.warn("Missing category in {}: {}", LanguageManager.getLanguage().getFileName(), category.getCategoryName());
                    continue;
                }

                Set<TKey> loadedKeys = new HashSet<>();
                for (Map.Entry<String, JsonElement> entry : categoryTranslations.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue().getAsString();

                    // Use fromKeyName to get the TKey from the JSON key
                    TKey translationKey = TKey.fromKeyName(key);
                    if (translationKey != null) {
                        // If value is null or empty, fall back to the default value from the enum
                        String translationValue = (value != null && !value.isEmpty()) ? value : translationKey.getDefaultValue();
                        category.addTranslation(translationKey, translationValue);
                        loadedKeys.add(translationKey);
                    }
                }

                for (TKey key : TKey.values()) {
                    if (key.getCategory() == category && !loadedKeys.contains(key)) {
                        logger.warn("Missing translation in {} -> category: {}, key: {}", LanguageManager.getLanguage().getFileName(), key.getCategory(), key.getKeyName());
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("An error occurred: {}", ex.getMessage(), ex);
        }
    }
}
