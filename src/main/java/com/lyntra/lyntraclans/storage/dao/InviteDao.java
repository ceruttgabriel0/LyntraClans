package com.lyntra.lyntraclans.storage.dao;

import com.lyntra.lyntraclans.storage.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class InviteDao {

    public record Invite(int id, int clanId, UUID playerUuid, long invitedAt) {
    }

    private final DatabaseManager databaseManager;

    public InviteDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void insert(int clanId, UUID playerUuid, long invitedAt) throws SQLException {
        String sql = "INSERT OR REPLACE INTO clan_invites (clan_id, player_uuid, invited_at) VALUES (?, ?, ?)";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.setString(2, playerUuid.toString());
            statement.setLong(3, invitedAt);
            statement.executeUpdate();
        }
    }

    public boolean exists(int clanId, UUID playerUuid) throws SQLException {
        String sql = "SELECT 1 FROM clan_invites WHERE clan_id = ? AND player_uuid = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.setString(2, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public List<Invite> findByPlayer(UUID playerUuid) throws SQLException {
        List<Invite> invites = new ArrayList<>();
        String sql = "SELECT * FROM clan_invites WHERE player_uuid = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    invites.add(fromResultSet(resultSet));
                }
            }
        }
        return invites;
    }

    public void delete(int clanId, UUID playerUuid) throws SQLException {
        String sql = "DELETE FROM clan_invites WHERE clan_id = ? AND player_uuid = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.setString(2, playerUuid.toString());
            statement.executeUpdate();
        }
    }

    public void deleteAllForPlayer(UUID playerUuid) throws SQLException {
        String sql = "DELETE FROM clan_invites WHERE player_uuid = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            statement.executeUpdate();
        }
    }

    public void deleteByClan(int clanId) throws SQLException {
        String sql = "DELETE FROM clan_invites WHERE clan_id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.executeUpdate();
        }
    }

    private Invite fromResultSet(ResultSet resultSet) throws SQLException {
        return new Invite(resultSet.getInt("id"), resultSet.getInt("clan_id"),
                UUID.fromString(resultSet.getString("player_uuid")), resultSet.getLong("invited_at"));
    }
}
