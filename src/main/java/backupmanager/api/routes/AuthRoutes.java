package backupmanager.api.routes;

import backupmanager.Email.EmailValidator;
import backupmanager.Entities.User;
import backupmanager.Services.LoginService;
import backupmanager.database.Repositories.UserRepository;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class AuthRoutes {

    private static final LoginService loginService = new LoginService();

    public static void register(Javalin app) {
        app.get("/api/auth/status", AuthRoutes::status);
        app.get("/api/auth/user", AuthRoutes::getUser);
        app.post("/api/auth/register", AuthRoutes::register);
    }

    private static void status(Context ctx) {
        boolean firstAccess = loginService.isFirstAccess();
        ctx.json(new StatusResponse(firstAccess));
    }

    private static void getUser(Context ctx) {
        User user = UserRepository.getLastUser();
        if (user == null) {
            ctx.status(404).json(new ErrorMsg("No user registered"));
            return;
        }
        ctx.json(user);
    }

    private static void register(Context ctx) {
        RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);
        if (req.name() == null || req.surname() == null || req.email() == null) {
            ctx.status(400).json(new ErrorMsg("name, surname and email are required"));
            return;
        }
        if (!EmailValidator.isValidEmail(req.email())) {
            ctx.status(422).json(new ErrorMsg("invalid email format"));
            return;
        }
        User user = new User(req.name(), req.surname(), req.email());
        loginService.createNewUser(user);
        ctx.status(201).json(user);
    }

    public record RegisterRequest(String name, String surname, String email) {}

    private record StatusResponse(boolean firstAccess) {}

    private record ErrorMsg(String error) {}
}
