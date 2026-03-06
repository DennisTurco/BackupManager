package backupmanager.Helpers;

import java.awt.TrayIcon;

import javax.swing.JOptionPane;

import backupmanager.gui.Controllers.TrayController;
import backupmanager.Enums.Translations.TCategory;
import backupmanager.Enums.Translations.TKey;

public class SubscriptionNotifier {
    public static void showExpiringWarning(TrayController trayController) {
        String title = TCategory.SUBSCRIPTION.getTranslation(TKey.SUBSCRIPTION_EXPIRING_TITLE);
        String message = TCategory.SUBSCRIPTION.getTranslation(TKey.SUBSCRIPTION_EXPIRING_MESSAGE);

        showMessage(trayController, title, message, TrayIcon.MessageType.WARNING);
    }

    public static void showExpiredAlert(TrayController trayController) {
        String title = TCategory.SUBSCRIPTION.getTranslation(TKey.SUBSCRIPTION_EXPIRED_TITLE);
        String message = TCategory.SUBSCRIPTION.getTranslation(TKey.SUBSCRIPTION_EXPIRED_MESSAGE);

        showMessage(trayController, title, message, TrayIcon.MessageType.ERROR);
    }

    private static void showMessage(TrayController trayController, String title, String message, TrayIcon.MessageType type) {
        if (trayController.getTrayIcon() != null) {
            trayController.getTrayIcon().displayMessage(title, message, type);
        } else {
            int messageType = (type == TrayIcon.MessageType.ERROR) ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE;
            JOptionPane.showMessageDialog(null, message, title, messageType);
        }
    }
}
