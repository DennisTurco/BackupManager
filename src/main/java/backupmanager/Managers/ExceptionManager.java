package backupmanager.Managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Email.EmailSender;

public class ExceptionManager {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionManager.class);

    public static void openExceptionMessage(String errorMessage, String stackTrace) {
        logger.error("Exception: {} | {}", errorMessage, stackTrace);

        String body = errorMessage == null || errorMessage.isEmpty()
            ? stackTrace
            : errorMessage + "\n" + stackTrace;

        EmailSender.sendErrorEmail("Critical Error Report", body, errorMessage);
    }
}
