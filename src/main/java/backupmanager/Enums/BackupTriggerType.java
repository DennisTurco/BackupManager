package backupmanager.Enums;

import backupmanager.Enums.utils.CodeEnum;
import backupmanager.Enums.utils.EnumUtil;

public enum BackupTriggerType implements CodeEnum {
    USER(1),
    SCHEDULER(2);

    private final int code;

    BackupTriggerType(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    public static BackupTriggerType fromCode(int code) {
        return EnumUtil.fromCode(BackupTriggerType.class, code);
    }
}
