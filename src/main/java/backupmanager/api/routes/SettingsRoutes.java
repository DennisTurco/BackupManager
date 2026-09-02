package backupmanager.api.routes;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Managers.LanguageManager;
import backupmanager.database.Repositories.ConfigurationRepository;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class SettingsRoutes {

    private static final Logger logger = LoggerFactory.getLogger(SettingsRoutes.class);

    // Every key here must actually be read somewhere (Electron main process or the React UI) —
    // THEME/CHECK_INTERVAL_MINUTES/EMAIL_* used to live here but were never read by anything
    // (theme is handled entirely via localStorage, the real backup interval comes from
    // JsonConfig, and SMTP config comes from config.enc), so they were removed rather than
    // left as a setting that silently did nothing.
    private static final String[] EDITABLE_KEYS = {
        "LANGUAGE", "NOTIFY_ON_COMPLETE", "NOTIFY_ON_FAILURE", "START_MINIMIZED",
        "DEFAULT_DESTINATION_PATH", "DEFAULT_MAX_TO_KEEP"
    };

    public static void register(Javalin app) {
        app.get("/api/settings", SettingsRoutes::getAll);
        app.put("/api/settings", SettingsRoutes::updateBulk);
        app.put("/api/settings/{code}", SettingsRoutes::update);
    }

    private static void getAll(Context ctx) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String key : EDITABLE_KEYS) {
            String value = ConfigurationRepository.getConfigurationValueByCode(key);
            // LANGUAGE has no seeded row until the user saves it once — fall back to the
            // language actually active on the backend, so the picker isn't empty on first load.
            if (value == null && "LANGUAGE".equals(key)) {
                value = LanguageManager.getLanguage().getCode();
            }
            result.put(key, value);
        }
        ctx.json(result);
    }

    @SuppressWarnings("unchecked")
    private static void updateBulk(Context ctx) {
        Map<String, String> body = ctx.bodyAsClass(Map.class);
        Map<String, String> updated = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : body.entrySet()) {
            String code = entry.getKey();
            if (Arrays.asList(EDITABLE_KEYS).contains(code)) {
                ConfigurationRepository.updateConfigurationValueByCode(code, entry.getValue());
                updated.put(code, entry.getValue());
                if ("LANGUAGE".equals(code)) reloadLanguage(entry.getValue());
            }
        }
        ctx.json(updated);
    }

    private static void update(Context ctx) {
        String code = ctx.pathParam("code");
        boolean allowed = Arrays.asList(EDITABLE_KEYS).contains(code);
        if (!allowed) {
            ctx.status(403).json(Map.of("error", "Setting not editable: " + code));
            return;
        }
        UpdateRequest req = ctx.bodyAsClass(UpdateRequest.class);
        ConfigurationRepository.updateConfigurationValueByCode(code, req.value());
        if ("LANGUAGE".equals(code)) reloadLanguage(req.value());
        ctx.json(Map.of("code", code, "value", req.value()));
    }

    private static void reloadLanguage(String languageCode) {
        try {
            LanguageManager.setLanguageByCode(languageCode);
            logger.info("Translations reloaded for language: {}", languageCode);
        } catch (Exception ex) {
            logger.warn("Could not reload translations for language '{}': {}", languageCode, ex.getMessage());
        }
    }

    public record UpdateRequest(String value) {}
}
