package backupmanager.Entities;

public class BackupList {
    private final String directory;
    private final String file;

    public BackupList(String directory, String file) {
        this.directory = directory;
        this.file = file;
    }

    public String getDirectory() {
        return directory;
    }
    public String getFile() {
        return file;
    }
}
