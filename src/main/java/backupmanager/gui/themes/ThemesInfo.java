package backupmanager.gui.themes;

public record ThemesInfo (
    String name,
    String resourceName,
    boolean dark,
    String license,
    String licenseFile,
    String sourceCodeUrl,
    String sourceCodePath,
    String lafClassName
) { }
