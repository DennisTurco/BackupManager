package backupmanager.gui.frames.Controllers;

import javax.swing.JOptionPane;

import backupmanager.Enums.Translations.TCategory;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Services.ZippingThread;

public class BackupProgressController {

    public void handleCancelButtonRequest(javax.swing.JDialog dialog) {
        int response = JOptionPane.showConfirmDialog(dialog, TCategory.DIALOGS.getTranslation(TKey.INTERRUPT_BACKUP_PROCESS_MESSAGE), TCategory.DIALOGS.getTranslation(TKey.CONFIRMATION_REQUIRED_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            cancelBackup();
            dialog.dispose();
        }
    }

    private void cancelBackup() {
        ZippingThread.stopExecutorService(1);
    }
}
