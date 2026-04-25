package backupmanager.gui.Controllers;

import javax.swing.JOptionPane;

import backupmanager.Entities.TimeInterval;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.gui.simple.TimePickerDialog;

public class TimePickerController {

    public TimePickerController() { }

    public TimeInterval getTimeIntervalIfPossible(TimePickerDialog dialog, int days, int hours, int minutes) {
        if (isLongTimeCorrect(days, hours, minutes)) {
            if (isShortTimeCorrect(days, hours) && !showWarningMessageForShortTimeAndGetIfItOkayResponse(null))
                return null;

            return new TimeInterval(days, hours, minutes);
        }
        else
            showErrorMessageForLongTime(null);
        return null;
    }

    private boolean isLongTimeCorrect(int days, int hours, int minutes) {
        return days >= 0 && hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59 &&
            (days != 0 || hours != 0 || minutes != 0);
    }

    private boolean isShortTimeCorrect(int days, int hours) {
        return days == 0 && hours == 0;
    }

    private void showErrorMessageForLongTime(javax.swing.JDialog dialog) {
        JOptionPane.showMessageDialog(dialog, Translations.get(TKey.ERROR_WRONG_TIME_INTERVAL), Translations.get(TKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
    }

    private boolean showWarningMessageForShortTimeAndGetIfItOkayResponse(javax.swing.JDialog dialog) {
        int response = JOptionPane.showConfirmDialog(dialog, Translations.get(TKey.WARNING_SHORT_TIME_INTERVAL_MESSAGE), Translations.get(TKey.WARNING_GENERIC_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return response == JOptionPane.YES_OPTION;
    }
}
