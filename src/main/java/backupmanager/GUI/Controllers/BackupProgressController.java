package backupmanager.GUI.Controllers;

import javax.swing.JOptionPane;

import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import backupmanager.Services.ZippingThread;

public class BackupProgressController {

    public void handleCancelButtonRequest(javax.swing.JDialog dialog) {
        int response = JOptionPane.showConfirmDialog(dialog, TranslationCategory.DIALOGS.getTranslation(TranslationKey.INTERRUPT_BACKUP_PROCESS_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            cancelBackup();
            dialog.dispose();
        }
    }

    private void cancelBackup() {
        ZippingThread.stopExecutorService(1);
    }
}
