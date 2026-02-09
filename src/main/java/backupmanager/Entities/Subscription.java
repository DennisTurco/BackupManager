package backupmanager.Entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import backupmanager.Enums.SubscriptionCreationType;

public record Subscription (
    int subscriptionId,
    LocalDateTime insertDate,
    LocalDate startDate,
    LocalDate endDate,
    SubscriptionCreationType creationType
) { }
