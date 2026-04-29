package backupmanager.gui.Controllers;

import javax.swing.JComponent;

import backupmanager.Email.EmailValidator;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Utils.ToastUtils;

public class EntryUserController {
    public boolean isInputOkAndShowErrorIfNecessary(JComponent component, String name, String surname, String email) {
        if (name.isEmpty() || surname.isEmpty() || email.isEmpty()) {
            ToastUtils.showError(component, Translations.get(TKey.TOAST_MISSING_DATA_LOGIN_ERROR));
            return false;
        } else if (!EmailValidator.isValidEmail(email)) {
            ToastUtils.showError(component, Translations.get(TKey.TOAST_WRONG_EMAIL_LOGIN_ERROR));
            return false;
        }
        ToastUtils.showSuccess(component, Translations.get(TKey.TOAST_LOGIN));
        return true;
    }
}
