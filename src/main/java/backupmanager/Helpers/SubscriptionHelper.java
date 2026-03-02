package backupmanager.Helpers;

import java.time.LocalDate;

import backupmanager.Entities.Confingurations;
import backupmanager.Entities.Subscription;
import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.SubscriptionStatus;
import backupmanager.Enums.TranslationLoaderEnum.TranslationCategory;
import backupmanager.Enums.TranslationLoaderEnum.TranslationKey;
import backupmanager.Json.JSONConfigReader;
import backupmanager.database.Repositories.SubscriptionRepository;

public class SubscriptionHelper {
    private static final JSONConfigReader configReader = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());

    public static SubscriptionStatus getSubscriptionStatus() {
        if (!Confingurations.isSubscriptionNedded())
            return SubscriptionStatus.NONE;

        Subscription subscription = SubscriptionRepository.getAnySubscriptionValid();
        if (subscription == null)
            return SubscriptionStatus.EXPIRED;
        if (isSubscriptionExpiringSoon(subscription))
            return SubscriptionStatus.EXPIRATION;

        return SubscriptionStatus.ACTIVE;
    }

    public static String getSubscriptionStatusTranslated(SubscriptionStatus status) {
        String statusTranslation;
        switch (status) {
            case EXPIRED -> statusTranslation = TranslationCategory.SUBSCRIPTION.getTranslation(TranslationKey.SUBSCRIPTION_EXPIRED);
            case ACTIVE -> statusTranslation = TranslationCategory.SUBSCRIPTION.getTranslation(TranslationKey.SUBSCRIPTION_ACTIVE);
            case EXPIRATION -> statusTranslation = TranslationCategory.SUBSCRIPTION.getTranslation(TranslationKey.SUBSCRIPTION_EXPIRING);
            default -> statusTranslation = "";
        }
        return statusTranslation;
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
