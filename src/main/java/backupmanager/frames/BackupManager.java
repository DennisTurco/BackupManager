package backupmanager.frames;


import java.awt.Dimension;

import javax.swing.JFrame;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.menu.MyDrawerBuilder;
import backupmanager.system.FormManager;
import raven.modal.Drawer;

public class BackupManager extends JFrame{
    public BackupManager() {
        init();
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
