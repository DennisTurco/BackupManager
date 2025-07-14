package backupmanager.Entities;

import java.util.Locale;

import lombok.Getter;
import lombok.Setter;

public class User {
    @Getter @Setter private int id;
    @Getter private String name;
    @Getter private String surname;
    @Getter private String email;
    @Getter private String language;

    public User(int id, String name, String surname, String email, String language) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.language = language;
    }

    public User(String name, String surname, String email) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.language = Locale.getDefault().getDisplayName();
    }

    public String getUserCompleteName() {
        return name + " " + surname;
    }

    @Override
    public String toString() {
        return name + " " + surname + ", " + email + ", " + language;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        User other = (User) obj;

        return name.equals(other.name) &&
            surname.equals(other.surname) &&
            email.equals(other.email);
    }

    public static User getDefaultUser() {
        return new User("Unregistered", "User", "");
    }
}
