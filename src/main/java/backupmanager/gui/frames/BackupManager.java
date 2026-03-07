package backupmanager.gui.frames;


import java.awt.Dimension;

import javax.swing.JFrame;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.gui.menu.MyDrawerBuilder;
import backupmanager.gui.system.FormManager;
import raven.modal.Drawer;

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
        Drawer.installDrawer(this, MyDrawerBuilder.getInstance());
        FormManager.install(this);
        setSize(new Dimension(1366, 768));
        setLocationRelativeTo(null);
    }
}
