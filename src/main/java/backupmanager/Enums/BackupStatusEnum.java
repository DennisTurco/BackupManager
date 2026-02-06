package backupmanager.Enums;

public enum BackupStatusEnum {
    IN_PROGRESS(1),
    QUEUE(2),
    FINISHED(3),
    TERMINATED(4);

    private final int code;

    BackupStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static BackupStatusEnum fromCode(int code) {
        for (BackupStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid BackupStatusEnum code: " + code);
    }
}