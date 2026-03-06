package backupmanager.gui.Controllers;

import javax.swing.JOptionPane;

import backupmanager.Entities.TimeInterval;
import backupmanager.Enums.Translations.TCategory;
import backupmanager.Enums.Translations.TKey;
import backupmanager.gui.simple.TimePickerDialog;

public class TimePickerController {

    private TimeInterval timeInterval;
    private boolean closeOk;

    public TimePickerController(TimeInterval timeInterval, boolean closeOk) {
        this.timeInterval = timeInterval;
        this.closeOk = closeOk;
    }

    @Deprecated
    public void handleOkButton(javax.swing.JDialog dialog, int days, int hours, int minutes) {
        if (isLongTimeCorrect(days, hours, minutes)) {
            if (isShortTimeCorrect(days, hours) && !showWarningMessageForShortTimeAndGetIfItOkayResponse(dialog))
                return;

            timeInterval = new TimeInterval(days, hours, minutes);
            closeOk = true;
            dialog.dispose();
        }
        else
            showErrorMessageForLongTime(dialog);
    }

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

    @Deprecated
    public TimeInterval getTimeInterval() {
        if (closeOk) return timeInterval;
        return null;
    }

    @Deprecated
    public void setCloseOk(boolean closeOk) { this.closeOk = closeOk; }

    private boolean isLongTimeCorrect(int days, int hours, int minutes) {
        return days >= 0 && hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59 &&
            (days != 0 || hours != 0 || minutes != 0);
    }

    private boolean isShortTimeCorrect(int days, int hours) {
        return days == 0 && hours == 0;
    }

    private void showErrorMessageForLongTime(javax.swing.JDialog dialog) {
        JOptionPane.showMessageDialog(dialog, TCategory.DIALOGS.getTranslation(TKey.ERROR_WRONG_TIME_INTERVAL), TCategory.DIALOGS.getTranslation(TKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
    }

    private boolean showWarningMessageForShortTimeAndGetIfItOkayResponse(javax.swing.JDialog dialog) {
        int response = JOptionPane.showConfirmDialog(dialog, TCategory.DIALOGS.getTranslation(TKey.WARNING_SHORT_TIME_INTERVAL_MESSAGE), TCategory.DIALOGS.getTranslation(TKey.WARNING_GENERIC_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return response == JOptionPane.YES_OPTION;
    }
}
