package backupmanager.Email;

import ch.qos.logback.core.PropertyDefinerBase;

public class EncryptedPasswordDefiner extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        try {
            String encryptedPassword = System.getenv("BACKUPMANAGER_SMTP_PASSWORD");
            return backupmanager.Email.DecryptPassword.decrypt(encryptedPassword);
        } catch (Exception e) {
            System.err.println("Error decrypting SMTP password: " + e.getMessage());
            return "";
        }
    }
}