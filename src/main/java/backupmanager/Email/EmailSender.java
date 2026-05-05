package backupmanager.Email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.Email;
import backupmanager.Entities.User;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.EmailType;
import backupmanager.Enums.Translations;
import backupmanager.Enums.Translations.TKey;
import backupmanager.Json.JsonConfig;
import backupmanager.database.Repositories.EmailRepository;
import backupmanager.database.Repositories.UserRepository;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SMTPAppender;

/**
 * Utility class for sending emails through logback SMTPAppender.
 */
public class EmailSender {

    private static final JsonConfig configReader = JsonConfig.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

    // Logger for sending critical error emails
    private static final Logger emailErrorLogger = LoggerFactory.getLogger("EMAIL_ERROR_LOGGER");

    // Logger for sending informational emails
    private static final Logger emailInfoLogger = LoggerFactory.getLogger("EMAIL_INFO_LOGGER");

    // Logger for sending confirmation email
    private static final Logger emailConfirmationLogger = LoggerFactory.getLogger("EMAIL_CONFIRMATION_LOGGER");

    /**
     * Sends a critical error email.
     * @param subject The email subject.
     * @param body The email body.
    */
    public static void sendErrorEmail(String subject, String body, String errorMessage) {
        User user = getCurrentUser();

        if (user == null) {
            logger.warn("User is null. Cannot send critical error email");
            return;
        }

        if (!canSend(errorMessage)) {
            return;
        }

        int rows = 300;
        String emailMessage = String.format("""
                Subject: %s

                User: %s
                Email: %s
                Language: %s
                Installed Version: %s

                Has encountered the following error:
                %s

                Last %d rows of the application.log file:
                %s
                """,
                subject,
                user.getUserCompleteName(),
                user.email(),
                user.language(),
                ConfigKey.VERSION.getValue(),
                body,
                rows,
                getTextFromLogFile(rows)
        );

        emailErrorLogger.error(emailMessage); // Log the message as ERROR, triggering the SMTPAppender

        logger.info("Error email sent with subject: " + subject);

        insertEmailInternally(EmailType.CRITICAL_ERROR, errorMessage);
    }

    /**
     * Sends an informational email.
     */
    public static void sendUserCreationEmail(User user) {
        String userDetails = "New user registered. \n\nName: " + user.getUserCompleteName()+ "\nEmail: " + user.email() + "\nLanguage: " + user.language() + "\nInstalled version: " + ConfigKey.VERSION.getValue();

        String emailMessage = "\n\n" + userDetails;

        // Should be info, but if you change it, it doesn't work
        emailInfoLogger.error(emailMessage); // Log the message as INFO, triggering the SMTPAppender

        logger.info("User creation info email sent with user: " + user.toString());

        insertEmailInternally(EmailType.WELCOME, null);
    }

    /**
     * Sends an informational email.
     */
    public static void sendConfirmEmailToUser(User user) {
        if (user == null) throw new IllegalArgumentException("User object cannot be null");

        String subject = Translations.get(TKey.EMAIL_CONFIRMATION_SUBJECT);
        String body = Translations.get(TKey.EMAIL_CONFIRMATION_BODY);

        body = body.replace("[UserName]", user.getUserCompleteName());
        body = body.replace("[SupportEmail]", ConfigKey.EMAIL.getValue());

        String emailMessage = subject + "\n\n" + body;

        updateEmailRecipient(user.email());

        // Should be info, but if you change it, it doesn't work
        emailConfirmationLogger.error(emailMessage); // Log the message as INFO, triggering the SMTPAppender

        logger.info("Confirmation registration email sent to the user: " + user.toString());
    }

    private static void insertEmailInternally(EmailType type, String payload) {
        Email email = Email.createNewEmail(type, ConfigKey.VERSION.getValue(), payload);
        EmailRepository.insertEmail(email);
    }

    private static boolean canSend(String payload) {
        int minWaitDays = configReader.getConfigValue("CriticalEmailMinWaitDays", 7);
        LocalDateTime now = LocalDateTime.now();

        if (isDuplicateError(payload)) {
            logger.info("A critical error occurred, but the email was not sent because this error has already been reported for the current version.");
            return false;
        }

        if (!hasWaitedSufficientTime(minWaitDays, now)) {
            logger.info("A critical error occurred, but the email was not sent because the minimum wait time since the last critical error email has not elapsed.");
            return false;
        }

        return true;
    }

    private static boolean isDuplicateError(String payload) {
        if (payload == null) return false;

        Email lastError = EmailRepository.getLastErrorEmailByPayloadAndVersion(payload, ConfigKey.VERSION.getValue());
        return lastError != null;
    }

    private static boolean hasWaitedSufficientTime(int minWaitDays, LocalDateTime now) {
        Email lastEmail = EmailRepository.getLastEmailByType(EmailType.CRITICAL_ERROR);
        if (lastEmail == null) return true;

        LocalDateTime lastSent = lastEmail.insertDate();
        return lastSent.plusDays(minWaitDays).isBefore(now);
    }

    private static String getTextFromLogFile(int rows) {
        Path file = Paths.get(
            ConfigKey.LOG_DIRECTORY_STRING.getValue(),
            ConfigKey.LOG_FILE_STRING.getValue()
        );

        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return "Log file does not exist or is empty.";
        }

        try {
            Deque<String> lastLines = new ArrayDeque<>(rows);

            try (Stream<String> stream = Files.lines(file, StandardCharsets.UTF_8)) {
                stream.forEach(line -> {
                    if (lastLines.size() == rows) {
                        lastLines.removeFirst();
                    }
                    lastLines.addLast(line);
                });
            }

            return String.join("\n", lastLines);

        } catch (IOException e) {
            logger.error("Error reading log file: " + e.getMessage(), e);
            return "Error reading the log file.";
        }
    }

    private static User getCurrentUser() {
        User user = UserRepository.getLastUser();

        if (user == null) {
            logger.error("Unable to retrieve user details for the email because there is no user registered");
        }

        return user;
    }

    private static void updateEmailRecipient(String newRecipient) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        SMTPAppender smtpAppender = (SMTPAppender) context.getLogger("EMAIL_CONFIRMATION_LOGGER").getAppender("EMAIL_CONFIRMATION_LOGGER");

        // if exists -> update
        if (smtpAppender != null) {
            smtpAppender.getToList().clear();
            smtpAppender.addTo(newRecipient);
        }
    }
}
