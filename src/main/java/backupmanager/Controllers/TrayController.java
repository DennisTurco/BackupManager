package backupmanager.Controllers;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;

public class TrayController {

    private static final Logger logger = LoggerFactory.getLogger(TrayController.class);

    private TrayIcon trayIcon;

    private final Runnable onOpen;
    private final Runnable onExit;


    public TrayController(Runnable onOpen, Runnable onExit) {
        this.onOpen = onOpen;
        this.onExit = onExit;
    }

    public void start() {
        createHiddenIcon();
    }

    private void createHiddenIcon() {
        if (!SystemTray.isSupported()) {
            logger.warn("System tray is not supported!");
            return;
        }

        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(ConfigKey.LOGO_IMG.getValue()));

        PopupMenu popup = setupAndGetPopupMenu();

        trayIcon = new TrayIcon(image, "Remind Service", popup);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
            logger.info("TrayIcon added");
        } catch (AWTException e) {
            logger.error("TrayIcon could not be added", e);
        }

        trayIcon.addActionListener((ActionEvent e) -> onOpen.run());

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    onOpen.run();
                }
            }
        });
    }

    private PopupMenu setupAndGetPopupMenu() {
        PopupMenu popup = new PopupMenu();

        MenuItem openItem = new MenuItem(TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.OPEN_ACTION));
        MenuItem exitItem = new MenuItem(TranslationCategory.TRAY_ICON.getTranslation(TranslationKey.EXIT_ACTION));


        popup.add(openItem);
        popup.addSeparator();
        popup.addSeparator();
        popup.add(exitItem);

        openItem.addActionListener(e -> onOpen.run());

        exitItem.addActionListener(e -> {
            onExit.run();
        });

        return popup;
    }

    public void removeTrayIcon() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
            logger.info("TrayIcon removed");
        }
    }

    public TrayIcon geTrayIcon() {
        return trayIcon;
    }
}
