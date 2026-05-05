package backupmanager.Managers;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.Enums.Translations;
import backupmanager.interfaces.ITranslatable;
import backupmanager.Utils.AppPreferences;

// Observer class -> every time the language is changed, it notify all the components registered
public class LanguageManager {
    private static final Logger logger = LoggerFactory.getLogger(LanguageManager.class);

    private static final List<WeakReference<ITranslatable>> listeners = new ArrayList<>();

    public static void setLanguage(String language) {
        var lang = getLanguageByLanguageName(language);
        setLanguage(lang);
    }

    public static void setLanguage(LanguagesEnum language) {
        AppPreferences.setLanguage(language.getFileName());
        logger.info("Language setted to: {}", language.getLanguageName());
        loadPreferredLanguage();
        notifyLanguageChanged();
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

    public static void register(ITranslatable t) {
        listeners.add(new WeakReference<>(t));
    }

    public static void notifyLanguageChanged() {
        SwingUtilities.invokeLater(() -> {
            listeners.removeIf(ref -> ref.get() == null);
            for (WeakReference<ITranslatable> ref : listeners) {
                ITranslatable t = ref.get();
                if (t != null) {
                    t.setTranslations();
                }
            }
        });
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
