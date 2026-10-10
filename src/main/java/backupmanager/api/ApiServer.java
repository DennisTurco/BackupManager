package backupmanager.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import backupmanager.api.routes.AnalyticsRoutes;
import backupmanager.api.routes.BackupRoutes;
import backupmanager.api.routes.ConfigRoutes;
import backupmanager.api.routes.LogRoutes;
import backupmanager.api.routes.SettingsRoutes;
import backupmanager.api.routes.SubscriptionRoutes;
import backupmanager.api.routes.TranslationsRoutes;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiServer {

    private static final Logger logger = LoggerFactory.getLogger(ApiServer.class);
    private static final int PORT = Integer.parseInt(System.getProperty("api.port", "7089"));

    private Javalin app;

    public void start() {
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper, false));
            // Local API for the Electron UI only: not reachable from other machines on the network
            config.jetty.defaultHost = "127.0.0.1";
            config.bundledPlugins.enableCors(cors ->
                cors.addRule(rule -> rule.anyHost())
            );
        });

        app.get("/api/status", ctx -> ctx.json("{\"status\":\"ok\"}"));

        // AnalyticsRoutes registers /api/backups/running, a static path that must be
        // matched before BackupRoutes' /api/backups/{id} — Javalin resolves ambiguous
        // routes in registration order, not by specificity.
        AnalyticsRoutes.register(app);
        BackupRoutes.register(app);
        SettingsRoutes.register(app);
        LogRoutes.register(app);
        ConfigRoutes.register(app);
        SubscriptionRoutes.register(app);
        TranslationsRoutes.register(app);

        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Unhandled API error", e);
            ctx.status(500).json(new ErrorResponse(e.getMessage()));
        });

        app.start(PORT);
        logger.info("API server started on port {}", PORT);
    }

    public void stop() {
        if (app != null) {
            app.stop();
            logger.info("API server stopped");
        }
    }

    public record ErrorResponse(String error) {}
}
