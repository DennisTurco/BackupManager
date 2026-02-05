package backupmanager.Helpers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import backupmanager.Entities.TimeInterval;

public class SqlHelper {

    public static long toMilliseconds(LocalDateTime date) {
        return date != null ? date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : 0;
    }

    public static long toMilliseconds(LocalDateTime date, LocalDateTime fallbackValue) {
        if (fallbackValue == null) throw new IllegalArgumentException("Cannot pass the fallback value as null");
        return date != null ? date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : fallbackValue.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static LocalDateTime toLocalDateTime(Long millis) {
        LocalDateTime date = (millis != null && millis > 0)
            ? LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
            : null;
        return date;
    }

    public static LocalDate toLocalDate(Long millis) {
        return (millis != null && millis > 0)
            ? Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            : null;
    }

    public static TimeInterval toTimeInterval(String str) {
        return str != null ? TimeInterval.getTimeIntervalFromString(str) : null;
    }

    public static String toString(Object obj) {
        return obj != null ? obj.toString() : null;
    }
}
