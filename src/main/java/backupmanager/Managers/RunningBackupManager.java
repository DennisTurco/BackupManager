package backupmanager.Managers;

public final class RunningBackupManager {

    private static volatile RunningBackupManager instance;
    private volatile boolean running = false;

    private RunningBackupManager() {}

    public static RunningBackupManager getInstance() {
        RunningBackupManager result = instance;

        if (result != null)
            return result;

        synchronized (RunningBackupManager.class) {
            if (instance == null)
                instance = new RunningBackupManager();
            return instance;
        }
    }

    public synchronized boolean startBackup() {
        if (running) {
            return false;
        }
        running = true;
        return true;
    }

    public synchronized void finishBackup() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
