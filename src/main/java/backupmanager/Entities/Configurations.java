package backupmanager.Entities;

import backupmanager.database.Repositories.ConfigurationRepository;

public class Configurations {
    private static boolean subscriptionNedded; // if true the subscription is needed to use the backuground service

    public static void loadAllConfigurations() {
        setSubscriptionNedded(ConfigurationRepository.getConfigurationValueByCode("SubscriptionNedded"));
    }

    // i don't want to update the subscription value from the code. for now the only method is doing manually
    public static void updateAllConfigurations() {
    }

    public static void setSubscriptionNedded(boolean isNedded) {
        subscriptionNedded = isNedded;
    }

    private static void setSubscriptionNedded(String subscriptionValue) {
        subscriptionValue = subscriptionValue.trim().toLowerCase();
        subscriptionNedded = subscriptionValue.equals("true") || subscriptionValue.equals("1");
    }

    public static boolean isSubscriptionNedded() {
        return subscriptionNedded;
    }
}
