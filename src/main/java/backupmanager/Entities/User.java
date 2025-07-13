package backupmanager.Entities;

import java.util.Locale;

public class User {
    public int id;
    public String name;
    public String surname;
    public String email;
    public String language;

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
