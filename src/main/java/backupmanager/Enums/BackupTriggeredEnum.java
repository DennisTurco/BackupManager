package backupmanager.Enums;

import backupmanager.Enums.utils.CodeEnum;
import backupmanager.Enums.utils.EnumUtil;

public enum BackupTriggeredEnum implements CodeEnum {
    USER(1),
    SCHEDULER(2);

    private final int code;

    BackupTriggeredEnum(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    public static BackupTriggeredEnum fromCode(int code) {
        return EnumUtil.fromCode(BackupTriggeredEnum.class, code);
    }
}
