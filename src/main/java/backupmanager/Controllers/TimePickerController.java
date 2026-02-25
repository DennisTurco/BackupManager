package backupmanager.Controllers;

import javax.swing.JOptionPane;

import backupmanager.Entities.TimeInterval;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;

public class TimePickerController {

    private TimeInterval timeInterval;
    private boolean closeOk;

    public TimePickerController(TimeInterval timeInterval, boolean closeOk) {
        this.timeInterval = timeInterval;
        this.closeOk = closeOk;
    }

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

    public TimeInterval getTimeInterval() {
        if (closeOk) return timeInterval;
        return null;
    }
    public void setCloseOk(boolean closeOk) { this.closeOk = closeOk; }


    private boolean isLongTimeCorrect(int days, int hours, int minutes) {
        return days >= 0 && hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59 &&
            (days != 0 || hours != 0 || minutes != 0);
    }

    private boolean isShortTimeCorrect(int days, int hours) {
        return days == 0 && hours == 0;
    }

    private void showErrorMessageForLongTime(javax.swing.JDialog dialog) {
        JOptionPane.showMessageDialog(dialog, TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_WRONG_TIME_INTERVAL), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
    }

    private boolean showWarningMessageForShortTimeAndGetIfItOkayResponse(javax.swing.JDialog dialog) {
        int response = JOptionPane.showConfirmDialog(dialog, TranslationCategory.DIALOGS.getTranslation(TranslationKey.WARNING_SHORT_TIME_INTERVAL_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.WARNING_GENERIC_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return response == JOptionPane.YES_OPTION;
    }
}
