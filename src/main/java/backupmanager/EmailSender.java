package backupmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for sending emails through logback SMTPAppender.
 */
public class EmailSender {

    // Logger for sending critical error emails
    private static final Logger emailErrorLogger = LoggerFactory.getLogger("EMAIL_ERROR_LOGGER");
    
    // Logger for sending informational emails
    private static final Logger emailInfoLogger = LoggerFactory.getLogger("EMAIL_INFO_LOGGER");

    /**
     * Sends a critical error email.
     * @param subject The email subject.
     * @param body The email body.
     */
    public static void sendErrorEmail(String subject, String body) {
        String emailMessage = "Subject: " + subject + "\n\n" + body;

        emailErrorLogger.error(emailMessage); // Log the message as ERROR, triggering the SMTPAppender
    }

    /**
     * Sends an informational email.
     * @param subject The email subject.
     * @param body The email body.
     */
    public static void sendInfoEmail(String subject, String body) {
        String emailMessage = "Subject: " + subject + "\n\n" + body;

        // Should be info, but if you change it, it doesn't work
        emailInfoLogger.error(emailMessage); // Log the message as INFO, triggering the SMTPAppender
    }
}
