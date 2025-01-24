package backupmanager.Email;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.User;
import backupmanager.Enums.ConfigKey;
import backupmanager.Json.JsonUser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

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
        
        int rows = 300;
        String emailMessage = String.format(
            "Subject: %s\n\nUser: %s \nEmail: %s \nLanguage: %s \n\nHas encountered the following error:\n%s \n\nLast %d rows of the application.log file:\n%s",
            subject,
            user.getUserCompleteName(),
            user.email,
            user.language,
            body,
            rows,
            getTextFromLogFile(rows)
        );

        emailErrorLogger.error(emailMessage); // Log the message as ERROR, triggering the SMTPAppender

        logger.info("Error email sent with subject: " + subject);
    }

    /**
     * Sends an informational email.
     */
    public static void sendUserCreationEmail() {
        User user = getCurrentUser();

        String userDetails = (user != null) 
            ? "New user registered. \n\nName: " + user.getUserCompleteName()+ "\nEmail: " + user.email + "\nLanguage: " + user.language
            : "New user registered, but user details are unavailable.";

        String emailMessage = "\n\n" + userDetails;

        // Should be info, but if you change it, it doesn't work
        emailInfoLogger.error(emailMessage); // Log the message as INFO, triggering the SMTPAppender

        logger.info("User creation info email sent with user: " + (user != null ? user.toString() : "Unknown user"));
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
    
    public static String getTextFromLogFile(int rows) {
        File file = new File(ConfigKey.LOG_DIRECTORY_STRING.getValue() + ConfigKey.LOG_FILE_STRING.getValue());

        if (!file.exists() || !file.isFile() || file.length() == 0) {
            return "Log file does not exist or is empty.";
        }

        List<String> lastLines = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (lastLines.size() == rows) {
                    lastLines.remove(0); // remove the older
                }
                lastLines.add(line);
            }
        } catch (IOException e) {
            logger.error("An error occurred during reading the log file for getting the last rows: " + e.getMessage(), e);
            return "Error reading the log file.";
        }

        return String.join("\n", lastLines);
    }
}
