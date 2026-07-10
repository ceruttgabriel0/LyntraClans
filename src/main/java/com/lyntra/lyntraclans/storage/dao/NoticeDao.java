package com.lyntra.lyntraclans.storage.dao;

import com.lyntra.lyntraclans.storage.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class NoticeDao {

    public record Notice(int id, int clanId, UUID authorUuid, String message, long createdAt) {
    }

    private final DatabaseManager databaseManager;

    public NoticeDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void insert(int clanId, UUID authorUuid, String message, long createdAt) throws SQLException {
        String sql = "INSERT INTO clan_notices (clan_id, author_uuid, message, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.setString(2, authorUuid.toString());
            statement.setString(3, message);
            statement.setLong(4, createdAt);
            statement.executeUpdate();
        }
    }

    public List<Notice> findByClan(int clanId, int limit) throws SQLException {
        List<Notice> notices = new ArrayList<>();
        String sql = "SELECT * FROM clan_notices WHERE clan_id = ? ORDER BY created_at DESC LIMIT ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.setInt(2, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notices.add(new Notice(resultSet.getInt("id"), resultSet.getInt("clan_id"),
                            UUID.fromString(resultSet.getString("author_uuid")), resultSet.getString("message"),
                            resultSet.getLong("created_at")));
                }
            }
        }
        return notices;
    }

    public void deleteByClan(int clanId) throws SQLException {
        String sql = "DELETE FROM clan_notices WHERE clan_id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.executeUpdate();
        }
    }
}
