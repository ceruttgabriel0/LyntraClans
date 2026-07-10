package com.lyntra.lyntraclans.storage.dao;

import com.lyntra.lyntraclans.storage.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class WarDao {

    public record War(int id, int clanId, int targetClanId, long startedAt) {
    }

    private final DatabaseManager databaseManager;

    public WarDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void insert(int clanId, int targetClanId, long startedAt) throws SQLException {
        String sql = "INSERT OR IGNORE INTO clan_wars (clan_id, target_clan_id, started_at) VALUES (?, ?, ?)";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.setInt(2, targetClanId);
            statement.setLong(3, startedAt);
            statement.executeUpdate();
        }
    }

    public boolean isAtWar(int clanId, int targetClanId) throws SQLException {
        String sql = "SELECT 1 FROM clan_wars WHERE (clan_id = ? AND target_clan_id = ?) "
                + "OR (clan_id = ? AND target_clan_id = ?)";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.setInt(2, targetClanId);
            statement.setInt(3, targetClanId);
            statement.setInt(4, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public List<War> findByClan(int clanId) throws SQLException {
        List<War> wars = new ArrayList<>();
        String sql = "SELECT * FROM clan_wars WHERE clan_id = ? OR target_clan_id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.setInt(2, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    wars.add(new War(resultSet.getInt("id"), resultSet.getInt("clan_id"),
                            resultSet.getInt("target_clan_id"), resultSet.getLong("started_at")));
                }
            }
        }
        return wars;
    }

    public void end(int clanId, int targetClanId) throws SQLException {
        String sql = "DELETE FROM clan_wars WHERE (clan_id = ? AND target_clan_id = ?) "
                + "OR (clan_id = ? AND target_clan_id = ?)";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.setInt(2, targetClanId);
            statement.setInt(3, targetClanId);
            statement.setInt(4, clanId);
            statement.executeUpdate();
        }
    }

    public void deleteByClan(int clanId) throws SQLException {
        String sql = "DELETE FROM clan_wars WHERE clan_id = ? OR target_clan_id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.setInt(2, clanId);
            statement.executeUpdate();
        }
    }
}
