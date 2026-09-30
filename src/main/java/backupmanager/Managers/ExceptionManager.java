package backupmanager.Managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionManager {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionManager.class);

    // Errors are only logged (application.log / error.log): the app no longer sends emails
    public static void openExceptionMessage(String errorMessage, String stackTrace) {
        logger.error("Exception: {} | {}", errorMessage, stackTrace);
    }
}
