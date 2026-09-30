package backupmanager.api.routes;

import backupmanager.Entities.Subscription;
import backupmanager.Enums.SubscriptionStatus;
import backupmanager.Helpers.SubscriptionHelper;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class SubscriptionRoutes {

    public static void register(Javalin app) {
        app.get("/api/subscription/status", SubscriptionRoutes::getStatus);
    }

    private static void getStatus(Context ctx) {
        SubscriptionStatus status = SubscriptionHelper.getSubscriptionStatus();
        Subscription subscription = SubscriptionHelper.getLastValidSubscription();
        ctx.json(new SubscriptionStatusDto(
            status.name(),
            subscription != null ? subscription.startDate().toString() : null,
            subscription != null ? subscription.endDate().toString() : null
        ));
    }

    public record SubscriptionStatusDto(String status, String validFrom, String validUntil) {}

}
