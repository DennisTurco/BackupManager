package backupmanager.Services;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Email.EmailSender;
import backupmanager.Entities.Confingurations;
import backupmanager.Entities.User;
import backupmanager.Enums.LanguagesEnum;
import backupmanager.database.Repositories.UserRepository;

public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    public boolean isFirstAccess() {
        logger.debug("Checking for first access");
        User user = UserRepository.getLastUser();

        if (user == null) {
            setLanguageBasedOnPcLanguage();
            return true;
        } else {
            logger.info("Current user: " + user.toString());
            return false;
        }
    }

    public void createNewUser(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        UserRepository.insertUser(user);

        sendRegistrationEmail(user);
    }

    private void setLanguageBasedOnPcLanguage() {
        Locale defaultLocale = Locale.getDefault();
        String language = defaultLocale.getLanguage();

        logger.info("Setting default language to: " + language);

        switch (language) {
            case "en" -> Confingurations.setLanguage(LanguagesEnum.ENG);
            case "it" -> Confingurations.setLanguage(LanguagesEnum.ITA);
            case "es" -> Confingurations.setLanguage(LanguagesEnum.ESP);
            case "de" -> Confingurations.setLanguage(LanguagesEnum.DEU);
            case "fr" -> Confingurations.setLanguage(LanguagesEnum.FRA);
            default -> Confingurations.setLanguage(LanguagesEnum.ENG);
        }
    }

    private void sendRegistrationEmail(User user) {
        EmailSender.sendUserCreationEmail(user);
        EmailSender.sendConfirmEmailToUser(user);
    }
}
