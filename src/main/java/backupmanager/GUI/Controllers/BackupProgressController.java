package backupmanager.GUI.Controllers;

import backupmanager.Services.ZippingThread;

public class BackupProgressController {
    public void cancelBackup() {
        ZippingThread.stopExecutorService(1);
    }
}
