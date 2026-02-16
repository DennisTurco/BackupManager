package backupmanager.Entities;

import java.time.LocalDateTime;

import backupmanager.Enums.EmailType;

public record Email (
    int emailId,
    EmailType type,
    LocalDateTime insertDate,
    String appVersion,
    String payload
){ }
