package com.lyntra.lyntraclans.storage.dao;

import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.domain.Rank;
import com.lyntra.lyntraclans.storage.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class RankDao {

    private final DatabaseManager databaseManager;

    public RankDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Rank insert(int clanId, String name, int priority, Set<ClanPermission> permissions) throws SQLException {
        return insert(clanId, name, priority, permissions, false);
    }

    public Rank insert(int clanId, String name, int priority, Set<ClanPermission> permissions, boolean isDefault)
            throws SQLException {
        Connection connection = databaseManager.getConnection();
        String sql = "INSERT INTO clan_ranks (clan_id, name, priority, permissions, is_default) VALUES (?, ?, ?, ?, ?)";
        Rank rank = new Rank(0, clanId, name, priority, permissions, isDefault);
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, clanId);
            statement.setString(2, name);
            statement.setInt(3, priority);
            statement.setString(4, rank.serializePermissions());
            statement.setInt(5, isDefault ? 1 : 0);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new Rank(keys.getInt(1), clanId, name, priority, permissions, isDefault);
            }
        }
    }

    public List<Rank> findByClan(int clanId) throws SQLException {
        List<Rank> ranks = new ArrayList<>();
        String sql = "SELECT * FROM clan_ranks WHERE clan_id = ? ORDER BY priority DESC";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ranks.add(fromResultSet(resultSet));
                }
            }
        }
        return ranks;
    }

    public void update(Rank rank) throws SQLException {
        String sql = "UPDATE clan_ranks SET name = ?, priority = ?, permissions = ?, is_default = ?, "
                + "display_name = ? WHERE id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, rank.getName());
            statement.setInt(2, rank.getPriority());
            statement.setString(3, rank.serializePermissions());
            statement.setInt(4, rank.isDefault() ? 1 : 0);
            statement.setString(5, rank.getDisplayName().equals(rank.getName()) ? null : rank.getDisplayName());
            statement.setInt(6, rank.getId());
            statement.executeUpdate();
        }
    }

    public void delete(int rankId) throws SQLException {
        String sql = "DELETE FROM clan_ranks WHERE id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, rankId);
            statement.executeUpdate();
        }
    }

    public int countMembersUsingRank(int rankId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clan_members WHERE rank_id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, rankId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private Rank fromResultSet(ResultSet resultSet) throws SQLException {
        Set<ClanPermission> permissions = Rank.deserializePermissions(resultSet.getString("permissions"));
        Rank rank = new Rank(resultSet.getInt("id"), resultSet.getInt("clan_id"), resultSet.getString("name"),
                resultSet.getInt("priority"), permissions, resultSet.getInt("is_default") == 1);
        rank.setDisplayName(resultSet.getString("display_name"));
        return rank;
    }
}
