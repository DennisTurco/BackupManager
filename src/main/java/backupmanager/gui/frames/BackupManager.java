package backupmanager.gui.frames;


import java.awt.Dimension;

import javax.swing.JFrame;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.gui.menu.DrawerManager;
import backupmanager.gui.system.FormManager;

public class BackupManager extends JFrame{

    private static BackupManager instance;

    private BackupManager() {
        init();
    }

    public static synchronized BackupManager getInstance() {
        if (instance == null) {
            instance = new BackupManager();
        }
        return instance;
    }

    private void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getRootPane().putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);
        DrawerManager.getInstance().install(this);
        FormManager.install(this);
        setSize(new Dimension(1366, 768));
        setLocationRelativeTo(null);
    }
}
