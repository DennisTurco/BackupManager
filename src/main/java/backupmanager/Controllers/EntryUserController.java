package backupmanager.Controllers;

import javax.swing.JOptionPane;

import backupmanager.Email.EmailValidator;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;

public class EntryUserController {
    public boolean isInputOkAndShowErrorIfNecessary(javax.swing.JDialog dialog, String name, String surname, String email) {
        if (name.isEmpty() || surname.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, TranslationCategory.USER_DIALOG.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_MISSING_DATA), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (!EmailValidator.isValidEmail(email)) {
            JOptionPane.showMessageDialog(dialog, TranslationCategory.USER_DIALOG.getTranslation(TranslationKey.ERROR_MESSAGE_FOR_WRONG_EMAIL), TranslationCategory.DIALOGS.getTranslation(TranslationKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}
