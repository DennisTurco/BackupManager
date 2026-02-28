package backupmanager.Entities;

import java.util.Locale;

public record User (int id, String name, String surname, String email, String language) {

    public User(String name, String surname, String email) {
        this(0, name, surname, email, Locale.getDefault().getDisplayName());
    }

    public User(int id, String name, String surname, String email) {
        this(id, name, surname, email, Locale.getDefault().getDisplayName());
    }

    public String getUserCompleteName() {
        return name + " " + surname;
    }

    @Override
    public String toString() {
        return name + " " + surname + ", " + email + ", " + language;
    }
}
