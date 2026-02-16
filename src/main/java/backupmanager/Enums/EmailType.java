package backupmanager.Enums;

public enum EmailType {
    WELCOME(1),
    CRITICAL_ERROR(2);

    private final int code;

    EmailType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static EmailType fromCode(int code) {
        for (EmailType status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid EmailType code: " + code);
    }
}
