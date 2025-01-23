package backupmanager.Entities;

public class User {
    public final String name;
    public final String surname;
    public final String email; // nullable

    public User(String name, String surname, String email) {
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public String getUserCompleteName() {
        return name + " " + surname; 
    } 
    
    @Override
    public String toString() {
        return name + " " + surname + ", " + email; 
    } 

    public static User getDefaultUser() {
        return new User("Unregistered", "User", "");
    }
}
