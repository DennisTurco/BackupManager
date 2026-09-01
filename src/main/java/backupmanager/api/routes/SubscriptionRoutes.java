package backupmanager.api.routes;

import backupmanager.Email.EmailSender;
import backupmanager.Entities.Subscription;
import backupmanager.Entities.User;
import backupmanager.Enums.SubscriptionStatus;
import backupmanager.Helpers.SubscriptionHelper;
import backupmanager.database.Repositories.UserRepository;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class SubscriptionRoutes {

    public static void register(Javalin app) {
        app.get("/api/subscription/status", SubscriptionRoutes::getStatus);
        app.post("/api/subscription/request-renewal", SubscriptionRoutes::requestRenewal);
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

    private static void requestRenewal(Context ctx) {
        User user = UserRepository.getLastUser();
        if (user == null) {
            ctx.status(422).json(new ErrorMsg("No user registered"));
            return;
        }

        SubscriptionStatus status = SubscriptionHelper.getSubscriptionStatus();
        Subscription subscription = SubscriptionHelper.getLastValidSubscription();
        String validUntil = subscription != null ? subscription.endDate().toString() : null;

        EmailSender.sendSubscriptionRenewalRequestEmail(user, status.name(), validUntil);
        ctx.status(202).json(new ErrorMsg("Renewal request sent"));
    }

    public record SubscriptionStatusDto(String status, String validFrom, String validUntil) {}

    private record ErrorMsg(String message) {}
}
