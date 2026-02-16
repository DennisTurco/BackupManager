package backupmanager.Enums;

import backupmanager.Enums.utils.CodeEnum;
import backupmanager.Enums.utils.EnumUtil;

public enum EmailType implements CodeEnum {
    WELCOME(1),
    CRITICAL_ERROR(2);

    private final int code;

    EmailType(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    public static EmailType fromCode(int code) {
        return EnumUtil.fromCode(EmailType.class, code);
    }
}
