package backupmanager.Repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.User;

public class UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    public static void insertUser(User user) {
        String sql = "INSERT INTO Users (Name, Surname, Email, Language, InsertDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.name());
            stmt.setString(2, user.surname());
            stmt.setString(3, user.email());
            stmt.setString(4, user.language());
            stmt.setString(5, LocalDateTime.now().toString());
            stmt.executeUpdate();

            logger.info("User inserted succesfully");

        } catch (SQLException e) {
            logger.error("User inserting error: " + e.getMessage());
        }
    }

    public static User getLastUser() {
        String sql = "SELECT UserId, Name, Surname, Email, Language FROM Users ORDER BY UserId DESC LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int id = rs.getInt("UserId");
                String name = rs.getString("Name");
                String surname = rs.getString("Surname");
                String email = rs.getString("Email");
                String language = rs.getString("Language");
                return new User(id, name, surname, email, language);
            }

        } catch (SQLException e) {
            logger.error("Error fetching last user: " + e.getMessage());
        }

        return null;
    }

    public static void updateUser(User user) {
        String sql = "UPDATE Users SET Name = ?, Surname = ?, Email = ? WHERE UserId = ?";
        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.name());
            stmt.setString(2, user.surname());
            stmt.setString(3, user.email());
            stmt.setInt(4, user.id());
            stmt.executeUpdate();

            logger.info("User updated succesfully");

        } catch (SQLException e) {
            logger.error("User updating error: " + e.getMessage());
        }
    }
}
