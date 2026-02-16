package backupmanager.Enums;

import backupmanager.Enums.utils.CodeEnum;
import backupmanager.Enums.utils.EnumUtil;

public enum BackupStatusEnum implements CodeEnum {
    IN_PROGRESS(1),
    QUEUE(2),
    FINISHED(3),
    TERMINATED(4);

    private final int code;

    BackupStatusEnum(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    public static BackupStatusEnum fromCode(int code) {
        return EnumUtil.fromCode(BackupStatusEnum.class, code);
    }
}
