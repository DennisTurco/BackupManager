package backupmanager.Enums;
public enum LanguagesEnum {
    ITA("Italiano", "ita.json", "it"),
    ENG("English", "eng.json", "en"),
    DEU("Deutsch", "deu.json", "de"),
    ESP("Español", "esp.json", "es"),
    FRA("Français", "fra.json", "fr");

    private final String languageName;
    private final String fileName;
    private final String code;

    public String getFileName() {
        return fileName;
    }

    public String getLanguageName() {
        return languageName;
    }

    // Short ISO code used by the REST API / React UI (e.g. Settings language picker)
    public String getCode() {
        return code;
    }

    public static LanguagesEnum getDefault() {
        return ENG;
    }

    public static LanguagesEnum fromCode(String code) {
        for (LanguagesEnum lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) return lang;
        }
        throw new IllegalArgumentException("Impossible fetch language with code: " + code);
    }

    private LanguagesEnum(String languageName, String fileName, String code) {
        this.languageName = languageName;
        this.fileName = fileName;
        this.code = code;
    }
}
