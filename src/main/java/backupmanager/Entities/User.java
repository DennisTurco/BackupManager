package backupmanager.Entities;

public class User {
    public final String name;
    public final String surname;

    public User(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    @Override
    public String toString() {
        return name + " " + surname; 
    } 

    public static User getDefaultUser() {
        return new User("Unregistered", "User");
    }
}
