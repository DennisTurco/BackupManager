package backupmanager.api.routes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import backupmanager.Enums.ConfigKey;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class LogRoutes {

    public static void register(Javalin app) {
        app.get("/api/logs", LogRoutes::getLogs);
    }

    private static void getLogs(Context ctx) {
        try {
            Path logFile = Paths.get(
                System.getProperty("user.home"),
                ".backupmanager", "logs",
                ConfigKey.LOG_FILE_STRING.getValue()
            );

            if (!Files.exists(logFile)) {
                ctx.status(404).json(Map.of("error", "Log file not found: " + logFile));
                return;
            }

            String content = Files.readString(logFile);
            ctx.contentType("text/plain").result(content);

        } catch (IOException e) {
            ctx.status(500).json(Map.of("error", "Failed to read log file: " + e.getMessage()));
        }
    }
}
