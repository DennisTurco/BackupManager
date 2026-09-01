package backupmanager.Utils;

import java.util.prefs.Preferences;

import backupmanager.Enums.LanguagesEnum;

public class AppPreferences {

    public static final String PREFERENCES_ROOT_PATH = "/BackupManager";
    public static final String KEY_LANGUAGE = "language";
    private static Preferences state;

    public static Preferences getState() {
        return state;
    }

    public static void init() {
        state = Preferences.userRoot().node(PREFERENCES_ROOT_PATH);
    }

    public static String getLanguage() {
        return state.get(KEY_LANGUAGE, LanguagesEnum.getDefault().getFileName());
    }

    public static void setLanguage(String lang) {
        state.put(KEY_LANGUAGE, lang);
    }
}
