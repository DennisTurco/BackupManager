package backupmanager.Helpers;

import java.time.LocalDate;

import backupmanager.Entities.Configurations;
import backupmanager.Entities.Subscription;
import backupmanager.Enums.SubscriptionStatus;
import backupmanager.Json.JsonConfig;
import backupmanager.database.Repositories.SubscriptionRepository;

public class SubscriptionHelper {
    private static final JsonConfig configReader = JsonConfig.getInstance();

    public static SubscriptionStatus getSubscriptionStatus() {
        if (!Configurations.isSubscriptionNedded())
            return SubscriptionStatus.NONE;

        Subscription subscription = SubscriptionRepository.getAnySubscriptionValid();
        if (subscription == null)
            return SubscriptionStatus.EXPIRED;
        if (isSubscriptionExpiringSoon(subscription))
            return SubscriptionStatus.EXPIRATION;

        return SubscriptionStatus.ACTIVE;
    }

    public static Subscription getLastValidSubscription() {
        return SubscriptionRepository.getAnySubscriptionValid();
    }

    private static boolean isSubscriptionExpiringSoon(Subscription subscription) {
        int days = configReader.getConfigValue("SubscriptionWarningDays", 7);
        LocalDate now = LocalDate.now();
        LocalDate endMinusDays = subscription.endDate().minusDays(days);
        return now.isAfter(endMinusDays);
    }
}
