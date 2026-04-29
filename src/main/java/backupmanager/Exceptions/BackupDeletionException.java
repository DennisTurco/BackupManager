package backupmanager.Exceptions;

public class BackupDeletionException extends Exception {
    public BackupDeletionException() {
        super();
    }

    public BackupDeletionException(String message) {
        super(message);
    }

    public BackupDeletionException(String message, Throwable cause) {
        super(message, cause);
    }

    public BackupDeletionException(Throwable cause) {
        super(cause);
    }
}
