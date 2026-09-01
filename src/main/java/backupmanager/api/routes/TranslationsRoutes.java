package backupmanager.api.routes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import backupmanager.Enums.ConfigKey;
import backupmanager.Enums.LanguagesEnum;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

public class TranslationsRoutes {

    public static void register(Javalin app) {
        app.get("/api/translations/languages", TranslationsRoutes::getLanguages);
        app.get("/api/translations/{code}", TranslationsRoutes::getTranslations);
    }

    private static void getLanguages(Context ctx) {
        List<LanguageDto> languages = Arrays.stream(LanguagesEnum.values())
            .map(lang -> new LanguageDto(lang.getCode(), lang.getLanguageName()))
            .toList();
        ctx.json(languages);
    }

    private static void getTranslations(Context ctx) {
        String code = ctx.pathParam("code");
        LanguagesEnum language;
        try {
            language = LanguagesEnum.fromCode(code);
        } catch (IllegalArgumentException e) {
            throw new NotFoundResponse("Unknown language code: " + code);
        }

        Path path = Path.of(ConfigKey.LANGUAGES_DIRECTORY_STRING.getValue(), language.getFileName());
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            ctx.contentType("application/json").result(json);
        } catch (IOException e) {
            throw new NotFoundResponse("Translation file not found for language: " + code);
        }
    }

    public record LanguageDto(String code, String label) {}
}
