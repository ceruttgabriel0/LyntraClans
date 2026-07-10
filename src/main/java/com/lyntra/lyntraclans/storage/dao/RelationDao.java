package com.lyntra.lyntraclans.storage.dao;

import com.lyntra.lyntraclans.domain.ClanRelation;
import com.lyntra.lyntraclans.domain.RelationType;
import com.lyntra.lyntraclans.storage.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RelationDao {

    private final DatabaseManager databaseManager;

    public RelationDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public ClanRelation insert(int clanId, int targetClanId, RelationType type, long createdAt) throws SQLException {
        String sql = "INSERT INTO clan_relations (clan_id, target_clan_id, type, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = databaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, clanId);
            statement.setInt(2, targetClanId);
            statement.setString(3, type.name());
            statement.setLong(4, createdAt);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new ClanRelation(keys.getInt(1), clanId, targetClanId, type, createdAt);
            }
        }
    }

    public Optional<ClanRelation> find(int clanId, int targetClanId) throws SQLException {
        String sql = "SELECT * FROM clan_relations WHERE clan_id = ? AND target_clan_id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.setInt(2, targetClanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(fromResultSet(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<ClanRelation> findByClan(int clanId) throws SQLException {
        List<ClanRelation> relations = new ArrayList<>();
        String sql = "SELECT * FROM clan_relations WHERE clan_id = ? OR target_clan_id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.setInt(2, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    relations.add(fromResultSet(resultSet));
                }
            }
        }
        return relations;
    }

    public void updateType(int id, RelationType type) throws SQLException {
        String sql = "UPDATE clan_relations SET type = ? WHERE id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, type.name());
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM clan_relations WHERE id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void deleteBetween(int clanId, int targetClanId) throws SQLException {
        String sql = "DELETE FROM clan_relations WHERE (clan_id = ? AND target_clan_id = ?) "
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
        String sql = "DELETE FROM clan_relations WHERE clan_id = ? OR target_clan_id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.setInt(2, clanId);
            statement.executeUpdate();
        }
    }

    private ClanRelation fromResultSet(ResultSet resultSet) throws SQLException {
        return new ClanRelation(resultSet.getInt("id"), resultSet.getInt("clan_id"),
                resultSet.getInt("target_clan_id"), RelationType.valueOf(resultSet.getString("type")),
                resultSet.getLong("created_at"));
    }
}
