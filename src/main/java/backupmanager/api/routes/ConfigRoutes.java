package backupmanager.api.routes;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import backupmanager.Enums.ConfigKey;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;
import io.javalin.http.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigRoutes {

    private static final Logger logger = LoggerFactory.getLogger(ConfigRoutes.class);

    public static void register(Javalin app) {
        app.get("/api/config", ConfigRoutes::getConfig);
    }

    private static void getConfig(Context ctx) {
        // Re-read MenuItems from the raw JSON since ConfigKey enum doesn't model nested objects
        JsonObject menuItems = new JsonObject();
        try {
            String configPath = ConfigKey.CONFIG_DIRECTORY_STRING.getValue()
                + ConfigKey.CONFIG_FILE_STRING.getValue();
            try (FileReader reader = new FileReader(configPath, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (root.has("MenuItems")) menuItems = root.getAsJsonObject("MenuItems");
            }
        } catch (IOException e) {
            logger.warn("Could not read config.json for MenuItems: {}", e.getMessage());
        }

        ConfigDto dto = new ConfigDto(
            ConfigKey.VERSION.getValue(),
            ConfigKey.EMAIL.getValue(),
            new LinksDto(
                ConfigKey.DONATE_PAYPAL_LINK.getValue(),
                ConfigKey.DONATE_BUYMEACOFFE_LINK.getValue(),
                ConfigKey.INFO_PAGE_LINK.getValue(),
                ConfigKey.ISSUE_PAGE_LINK.getValue(),
                ConfigKey.SHARE_LINK.getValue(),
                ConfigKey.SHARD_WEBSITE.getValue()
            ),
            new GuiDto(
                intVal(ConfigKey.GUI_WIDTH, 1366),
                intVal(ConfigKey.GUI_HEIGHT, 768),
                intVal(ConfigKey.GUI_MIN_WIDTH, 800),
                intVal(ConfigKey.GUI_MIN_HEIGHT, 600)
            ),
            menuItems.toString()
        );
        ctx.json(dto);
    }

    private static int intVal(ConfigKey key, int fallback) {
        try { return Integer.parseInt(key.getValue()); } catch (Exception e) { return fallback; }
    }

    public record LinksDto(
        String donatePaypal,
        String donateBuymeacoffee,
        String infoPage,
        String issuePage,
        String share,
        String website
    ) {}

    public record GuiDto(int width, int height, int minWidth, int minHeight) {}

    public record ConfigDto(
        String version,
        String email,
        LinksDto links,
        GuiDto gui,
        String menuItemsJson
    ) {}
}
