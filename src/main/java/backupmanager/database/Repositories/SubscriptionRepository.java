package backupmanager.database.Repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backupmanager.Entities.Subscription;
import backupmanager.Helpers.SqlHelper;
import backupmanager.database.Database;

// if the table is empty it means that the subscription is not nedded at all
// there is no insert method because for now if you want to use the subscription you have to insert it manually inside the database.
public class SubscriptionRepository {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionRepository.class);

    public static Subscription getAnySubscriptionValid() {
        String sql = """
            SELECT
                SubscriptionId,
                InsertDate,
                StartDate,
                EndDate
            FROM
                Subscriptions
            WHERE
                StartDate <= ? AND EndDate >= ?
            ORDER BY EndDate DESC
            LIMIT 1
            """;

        long nowMillis = System.currentTimeMillis();

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, nowMillis);
            stmt.setLong(2, nowMillis);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("SubscriptionId");
                    long insertDateLong = rs.getLong("InsertDate");
                    long startDateLong = rs.getLong("StartDate");
                    long endDateLong = rs.getLong("EndDate");

                    LocalDateTime insertDate = SqlHelper.toLocalDateTime(insertDateLong);
                    LocalDate startDate = SqlHelper.toLocalDate(startDateLong);
                    LocalDate endDate = SqlHelper.toLocalDate(endDateLong);

                    return new Subscription(id, insertDate, startDate, endDate);
                }
            }

        } catch (SQLException e) {
            logger.error("Error fetching a valid subscription", e);
        }

        return null;
    }
}
