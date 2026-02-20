package backupmanager.Exceptions;

public class BackupAlreadyRunningException extends Exception {

    public BackupAlreadyRunningException() {
        super();
    }

    public BackupAlreadyRunningException(String message) {
        super(message);
    }

    public BackupAlreadyRunningException(String message, Throwable cause) {
        super(message, cause);
    }

    public BackupAlreadyRunningException(Throwable cause) {
        super(cause);
    }
}
