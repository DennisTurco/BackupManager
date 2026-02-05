package backupmanager.Helpers;

import java.awt.TrayIcon;

import javax.swing.JOptionPane;

import backupmanager.Controllers.TrayController;

public class SubscriptionNotifier {
    public static void showExpiringWarning(TrayController trayController) {
        String title = "Subscription in scadenza";
        String message = "La tua subscription sta per scadere.\n"
                       + "I backup automatici continueranno a funzionare fino alla scadenza.\n"
                       + "Contatta l'assistenza per rinnovarla.";

        showMessage(trayController, title, message, TrayIcon.MessageType.WARNING);
    }

    public static void showExpiredAlert(TrayController trayController) {
        String title = "Subscription scaduta";
        String message = "La tua subscription è scaduta.\n"
                       + "I backup automatici non funzioneranno più.\n"
                       + "Contatta l'assistenza per riattivarla.";

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

