package backupmanager.Entities;

import lombok.Getter;

public class BackupList {
    @Getter private final String directory;
    @Getter private final String file;

    public BackupList(String directory, String file) {
        this.directory = directory;
        this.file = file;
    }
}
