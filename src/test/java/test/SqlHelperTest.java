package test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.TimeInterval;
import backupmanager.Helpers.SqlHelper;

public class SqlHelperTest {

    private static final LocalDateTime TestDate = LocalDate.of(2026, Month.APRIL, 8).atStartOfDay();
    private static final long MILLISECONDS = 1_775_599_200_000L;

    @Test
    void toMilliseconds_shouldBeEquals_forValidLocalDate() {
        long mills = SqlHelper.toMilliseconds(TestDate);
        assertEquals(mills, MILLISECONDS);
    }

    @Test
    void toMilliseconds_shouldReturnTrue_forNullLocalDate() {
        long mills = SqlHelper.toMilliseconds(null);
        assertTrue(mills == 0);
    }

    @Test
    void toMillisecondsWithFallback_shouldBeEquals_forValidLocalDate() {
        long mills = SqlHelper.toMilliseconds(TestDate, LocalDateTime.now());
        assertEquals(mills, MILLISECONDS);
    }

    @Test
    void toMillisecondsWithFallback_shouldReturnTrue_forNullLocalDate() {
        long mills = SqlHelper.toMilliseconds(null, LocalDateTime.now());
        assertTrue(mills > 0);
    }

    @Test
    public void toMillisecondsWithFallback_shouldThrowException_whenFallbackValueIsNull() {
        IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> SqlHelper.toMilliseconds(null, null));
        assertEquals("Cannot pass the fallback value as null", ex.getMessage());
    }

    @Test
    void toLocalDateTime_shouldBeEquals_forValidMills() {
        LocalDateTime dateTime = SqlHelper.toLocalDateTime(MILLISECONDS);
        assertEquals(dateTime, TestDate);
    }

    @Test
    void toLocalDateTime_shouldReturnTrue_forNullOrNotValidMillsValue() {
        LocalDateTime dateTime = SqlHelper.toLocalDateTime(null);
        LocalDateTime dateTime2 = SqlHelper.toLocalDateTime(Long.getLong("-2"));
        assertTrue(dateTime == null && dateTime2 == null);
    }

    @Test
    void toLocalDate_shouldBeEquals_forValidMills() {
        LocalDate date = SqlHelper.toLocalDate(MILLISECONDS);
        assertEquals(date, TestDate.toLocalDate());
    }

    @Test
    void toLocalDate_shouldReturnTrue_forNullOrNotValidMillsValue() {
        LocalDate date = SqlHelper.toLocalDate(null);
        LocalDate date2 = SqlHelper.toLocalDate(Long.getLong("-2"));
        assertTrue(date == null && date2 == null);
    }

    @Test
    void millisecondsAndDateConversion_equal_forSameLocalDate() {
        LocalDateTime inputDate = LocalDate.of(2026, Month.APRIL, 8).atStartOfDay();

        long mills = SqlHelper.toMilliseconds(inputDate);
        LocalDateTime dateTime = SqlHelper.toLocalDateTime(mills);

        assertEquals(inputDate, dateTime);
    }

    @Test
    void toTimeInterval_equal_forSameTimeInterval() {
        String time = "10.5:34";
        TimeInterval timeInterval = SqlHelper.toTimeInterval(time);
        assertEquals(time, timeInterval.toString());
    }

    @Test
    void toTimeInterval_shouldReturnTrue_forNullString() {
        TimeInterval timeInterval = SqlHelper.toTimeInterval(null);
        assertTrue(timeInterval == null);
    }

    @Test
    void toString_equal_forValidObject() {
        TimeInterval timeInterval = new TimeInterval(5, 10, 5);
        String timeIntervalStr = SqlHelper.toString(timeInterval);
        assertEquals(timeInterval.toString(), timeIntervalStr);
    }

    @Test
    void toString_shouldReturnTrue_forNullString() {
        String timeIntervalStr = SqlHelper.toString(null);
        assertTrue(timeIntervalStr == null);
    }
}
