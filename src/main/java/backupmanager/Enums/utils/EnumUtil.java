package backupmanager.Enums.utils;

public class EnumUtil {
    public static <Type extends Enum<Type> & CodeEnum> Type fromCode(Class<Type> enumClass, int code) {
        for (Type constants : enumClass.getEnumConstants()) {
            if (constants.getCode() == code) {
                return constants;
            }
        }
        throw new IllegalArgumentException("Invalid code " + code + " for enum " + enumClass.getSimpleName());
    }
}
