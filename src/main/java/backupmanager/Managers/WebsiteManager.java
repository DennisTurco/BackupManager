package backupmanager.Managers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.Translations.TCategory;
import backupmanager.Enums.Translations.TKey;

public class WebsiteManager {
    private static final Logger logger = LoggerFactory.getLogger(WebsiteManager.class);

    public static void openWebSite(String reportUrl) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(reportUrl));
                }
            }
        } catch (IOException | URISyntaxException e) {
            logger.error("Failed to open the web page: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(null, TCategory.DIALOGS.getTranslation(TKey.ERROR_MESSAGE_OPENING_WEBSITE), TCategory.DIALOGS.getTranslation(TKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void sendEmail() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();

            if (desktop.isSupported(Desktop.Action.MAIL)) {
                String subject = "Support - Backup Manager";
                String mailTo = "mailto:" + ConfigKey.EMAIL.getValue() + "?subject=" + encodeURI(subject);

                try {
                    URI uri = new URI(mailTo);
                    desktop.mail(uri);
                } catch (IOException | URISyntaxException ex) {
                    logger.error("Failed to send email: " + ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(null, TCategory.DIALOGS.getTranslation(TKey.ERROR_MESSAGE_UNABLE_TO_SEND_EMAIL), TCategory.DIALOGS.getTranslation(TKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
                }
            } else {
                logger.warn("Mail action is unsupported in your system's desktop environment.");
                JOptionPane.showMessageDialog(null, TCategory.DIALOGS.getTranslation(TKey.ERROR_MESSAGE_NOT_SUPPORTED_EMAIL), TCategory.DIALOGS.getTranslation(TKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            logger.warn("Desktop integration is unsupported on this system.");
            JOptionPane.showMessageDialog(null, TCategory.DIALOGS.getTranslation(TKey.ERROR_MESSAGE_NOT_SUPPORTED_EMAIL_GENERIC), TCategory.DIALOGS.getTranslation(TKey.ERROR_GENERIC_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to properly encode the URI with special characters (spaces, symbols, etc.)
    private static String encodeURI(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20");
        } catch (IOException e) {
            return value; // If encoding fails, return the original value
        }
    }
}

