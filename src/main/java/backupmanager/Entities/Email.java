package backupmanager.Entities;

import java.time.LocalDateTime;

public record Email (
    int emailId,
    int type,
    LocalDateTime insertDate,
    String appVersion,
    String payload
){ }
