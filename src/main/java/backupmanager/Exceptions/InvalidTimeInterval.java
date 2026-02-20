package backupmanager.Exceptions;

public class InvalidTimeInterval extends Exception {

    public InvalidTimeInterval() {
        super();
    }

    public InvalidTimeInterval(String message) {
        super(message);
    }

    public InvalidTimeInterval(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTimeInterval(Throwable cause) {
        super(cause);
    }
}
