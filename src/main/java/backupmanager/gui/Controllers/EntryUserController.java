package backupmanager.gui.Controllers;

import javax.swing.JOptionPane;

import backupmanager.Email.EmailValidator;
import backupmanager.Enums.Translations.TCategory;
import backupmanager.Enums.Translations.TKey;

public class EntryUserController {
    public boolean isInputOkAndShowErrorIfNecessary(javax.swing.JDialog dialog, String name, String surname, String email) {
        if (name.isEmpty() || surname.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, TCategory.USER_DIALOG.getTranslation(TKey.ERROR_MESSAGE_FOR_MISSING_DATA), TCategory.DIALOGS.getTranslation(TKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (!EmailValidator.isValidEmail(email)) {
            JOptionPane.showMessageDialog(dialog, TCategory.USER_DIALOG.getTranslation(TKey.ERROR_MESSAGE_FOR_WRONG_EMAIL), TCategory.DIALOGS.getTranslation(TKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}
