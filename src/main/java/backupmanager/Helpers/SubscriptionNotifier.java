package backupmanager.Helpers;

import java.awt.TrayIcon;

import javax.swing.JOptionPane;

import backupmanager.Controllers.TrayController;

public class SubscriptionNotifier {
    public static void showExpiringWarning(TrayController trayController) {
        String title = "Abbonamento di BackupManager in scadenza";
        String message = """
                         Il tuo abbonamento a BackupManager sta per scadere.
                         I backup automatici continueranno a funzionare fino alla scadenza.
                         Contatta l'assistenza per rinnovarlo.""";

        showMessage(trayController, title, message, TrayIcon.MessageType.WARNING);
    }

    public static void showExpiredAlert(TrayController trayController) {
        String title = "Abbonamento di BackupManager scaduto";
        String message = """
                         Il tuo abbonamento a BackupManager \u00e8 scaduto.
                         I backup automatici non funzioneranno pi\u00f9.
                         Contatta l'assistenza per riattivarlo.""";

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

