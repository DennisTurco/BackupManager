package backupmanager.Enums;

import lombok.Getter;

public enum LanguagesEnum {
    ITA("Italiano", "ita.json"),
    ENG("English", "eng.json"),
    DEU("Deutsch", "deu.json"),
    ESP("Español", "esp.json"),
    FRA("Français", "fra.json");

    @Getter private final String languageName;
    @Getter private final String fileName;

    private LanguagesEnum(String languageName, String fileName) {
        this.languageName = languageName;
        this.fileName = fileName;
    }
}