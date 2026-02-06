package backupmanager.Entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Subscription (
    int subscriptionId,
    LocalDateTime insertDate,
    LocalDate startDate,
    LocalDate endDate
) { }
