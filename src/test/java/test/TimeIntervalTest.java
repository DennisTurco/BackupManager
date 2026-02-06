package test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import backupmanager.Entities.TimeInterval;

public class TimeIntervalTest {
    @Test
    public void equals_shouldReturnTrue_forSameTimeInterval() {
        TimeInterval ti1 = new TimeInterval(1, 12, 30);
        TimeInterval ti2 = new TimeInterval(1, 12, 30);

        assertTrue(ti1.equals(ti2));
    }

    @Test
    public void notEquals_shouldReturnTrue_forDifferentTimeInterval() {
        TimeInterval ti1 = new TimeInterval(1, 12, 30);
        TimeInterval ti2 = new TimeInterval(2, 12, 30);

        assertTrue(!ti1.equals(ti2));
    }

    @Test
    public void toString_shouldReturnCorrectStringRepresentation() {
        TimeInterval ti = new TimeInterval(1, 12, 30);
        String expected = "1.12:30";
        String actual = ti.toString();
        assertTrue(expected.equals(actual));
    }

    @Test
    public void fromString_shouldReturnCorrectTimeInterval() {
        String timeString = "1.12:30";
        TimeInterval expected = new TimeInterval(1, 12, 30);
        TimeInterval actual = TimeInterval.getTimeIntervalFromString(timeString);
        assertTrue(expected.equals(actual));
    }

    @Test
    public void isValid_shouldReturnTrue_forValidTimeInterval() {
        String timeString = "1.12:30";
        TimeInterval ti = TimeInterval.getTimeIntervalFromString(timeString);
        assertTrue(ti != null);
    }
}
