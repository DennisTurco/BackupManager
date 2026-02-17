package backupmanager.Entities;

import java.time.LocalDateTime;

import backupmanager.Enums.EmailType;

public record Email (
    int emailId,
    EmailType type,
    LocalDateTime insertDate,
    String appVersion,
    String payload
)
{
    public static Email createNewEmail(EmailType type, String appVersion, String payload) {
        return new Email(0, type, LocalDateTime.now(), appVersion, payload);
    }
}
