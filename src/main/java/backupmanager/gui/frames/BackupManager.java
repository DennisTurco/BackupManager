package backupmanager.gui.frames;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.formdev.flatlaf.FlatClientProperties;

import backupmanager.Enums.ConfigKey;
import backupmanager.gui.Controllers.GuiController;
import backupmanager.gui.menu.DrawerManager;
import backupmanager.gui.system.FormManager;

public class BackupManager extends JFrame {

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
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

        setTitle("Backup Manager");
        this.setIconImage(GuiController.getIcon(this.getClass()));
        getRootPane().putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);
        DrawerManager.getInstance().install(this);
        FormManager.install(this);

        setSize(new Dimension(
            Integer.parseInt(ConfigKey.GUI_WIDTH.getValue()),
            Integer.parseInt(ConfigKey.GUI_HEIGHT.getValue())
        ));
        setMinimumSize(new Dimension(
            Integer.parseInt(ConfigKey.GUI_MIN_WIDTH.getValue()),
            Integer.parseInt(ConfigKey.GUI_MIN_HEIGHT.getValue())
        ));

        setLocationRelativeTo(null);
    }
}
