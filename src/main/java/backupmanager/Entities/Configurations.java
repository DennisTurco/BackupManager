package backupmanager.Entities;

import backupmanager.database.Repositories.ConfigurationRepository;

public class Configurations {
    private static boolean subscriptionNedded; // if true the subscription is needed to use the backuground service

    public static void loadAllConfigurations() {
        setSubscriptionNedded(ConfigurationRepository.getConfigurationValueByCode("SubscriptionNedded"));
    }

    public static void setSubscriptionNedded(boolean isNedded) {
        subscriptionNedded = isNedded;
    }

    private static void setSubscriptionNedded(String subscriptionValue) {
        if (subscriptionValue == null) {
            subscriptionNedded = false;
            return;
        }
        subscriptionValue = subscriptionValue.trim().toLowerCase();
        subscriptionNedded = subscriptionValue.equals("true") || subscriptionValue.equals("1");
    }

    public static boolean isSubscriptionNedded() {
        return subscriptionNedded;
    }
}
