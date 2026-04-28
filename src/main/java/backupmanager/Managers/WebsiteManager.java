package backupmanager.Managers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Utils.ToastUtils;

public class WebsiteManager {
    private static final Logger logger = LoggerFactory.getLogger(WebsiteManager.class);

    public static void openWebSite(JFrame parent, String reportUrl) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(reportUrl));
                }
            }
        } catch (IOException | URISyntaxException e) {
            logger.error("Failed to open the web page: " + e.getMessage(), e);
            ToastUtils.showError(parent, Translations.get(TKey.TOAST_OPENING_WEBSITE_ERROR));
        }
    }

    public static void sendEmail(JFrame parent) {
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
                    ToastUtils.showError(parent, Translations.get(TKey.TOAST_UNABLE_TO_SEND_EMAIL));
                }
            } else {
                logger.warn("Mail action is unsupported in your system's desktop environment.");
                ToastUtils.showError(parent, Translations.get(TKey.TOAST_NOT_SUPPORTED_EMAIL));
            }
        } else {
            logger.warn("Desktop integration is unsupported on this system.");
            ToastUtils.showError(parent, Translations.get(TKey.TOAST_NOT_SUPPORTED_EMAIL_GENERIC));
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

