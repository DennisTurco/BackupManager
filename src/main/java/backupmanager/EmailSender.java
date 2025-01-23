package backupmanager;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.User;
import backupmanager.Enums.ConfigKey;
import backupmanager.Json.JsonUser;

/**
 * Utility class for sending emails through logback SMTPAppender.
 */
public class EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

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
        User user = getCurrentUser();

        if (user == null) {
            logger.warn("User is null, using a default user for the email");
            user = User.getDefaultUser();
        }

        String emailMessage = String.format(
            "Subject: %s\n\nUser: %s \nHas encountered the following error:\n\n%s",
            subject,
            user.toString(),
            body
        );

        emailErrorLogger.error(emailMessage); // Log the message as ERROR, triggering the SMTPAppender

        logger.info("Error email sent with subject: " + subject);
    }

    /**
     * Sends an informational email.
     * @param subject The email subject.
     * @param body The email body.
     */
    public static void sendUserCreationEmail() {
        User user = getCurrentUser();

        String userDetails = (user != null) 
            ? "New user registered with name: " + user.toString()
            : "New user registered, but user details are unavailable.";

        String emailMessage = "\n\n" + userDetails;

        // Should be info, but if you change it, it doesn't work
        emailInfoLogger.error(emailMessage); // Log the message as INFO, triggering the SMTPAppender

        logger.info("User creation info email sent with user: " + (user != null ? user.name + " " + user.surname : "Unknown user"));
    }


    private static User getCurrentUser() {
        try {
            User user = JsonUser.readUserFromJson(
                ConfigKey.USER_FILE_STRING.getValue(),
                ConfigKey.CONFIG_DIRECTORY_STRING.getValue()
            );

            return user;
        } catch (IOException e) {
            logger.error("Unable to retrieve user details for the email: " + e.getMessage(), e);
        }

        return null;
    }
}
