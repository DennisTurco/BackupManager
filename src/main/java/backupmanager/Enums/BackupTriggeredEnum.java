package backupmanager.Enums;

public enum BackupTriggeredEnum {
    USER(1),
    SCHEDULER(2);

    private final int code;

    BackupTriggeredEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static BackupTriggeredEnum fromCode(int code) {
        for (BackupTriggeredEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid BackupTriggeredEnum code: " + code);
    }
}
