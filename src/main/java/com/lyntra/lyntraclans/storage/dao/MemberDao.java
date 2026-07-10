package com.lyntra.lyntraclans.storage.dao;

import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.storage.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class MemberDao {

    private final DatabaseManager databaseManager;

    public MemberDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public ClanMember insert(UUID uuid, int clanId, int rankId, long joinedAt) throws SQLException {
        String sql = "INSERT INTO clan_members (uuid, clan_id, rank_id, joined_at, kills_rival, kills_ally, "
                + "kills_neutral, kills_civil, deaths) VALUES (?, ?, ?, ?, 0, 0, 0, 0, 0)";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setInt(2, clanId);
            statement.setInt(3, rankId);
            statement.setLong(4, joinedAt);
            statement.executeUpdate();
        }
        return new ClanMember(uuid, clanId, rankId, joinedAt, 0, 0, 0, 0, 0, false, 0);
    }

    public Optional<ClanMember> findByUuid(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM clan_members WHERE uuid = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(fromResultSet(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<ClanMember> findByClan(int clanId) throws SQLException {
        List<ClanMember> members = new ArrayList<>();
        String sql = "SELECT * FROM clan_members WHERE clan_id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    members.add(fromResultSet(resultSet));
                }
            }
        }
        return members;
    }

    public int countByRank(int rankId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clan_members WHERE rank_id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, rankId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    public void update(ClanMember member) throws SQLException {
        String sql = "UPDATE clan_members SET rank_id = ?, kills_rival = ?, kills_ally = ?, kills_neutral = ?, "
                + "kills_civil = ?, deaths = ?, trusted = ?, war_bonus_weight = ? WHERE uuid = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, member.getRankId());
            statement.setInt(2, member.getKills(com.lyntra.lyntraclans.domain.KillCategory.RIVAL));
            statement.setInt(3, member.getKills(com.lyntra.lyntraclans.domain.KillCategory.ALIADO));
            statement.setInt(4, member.getKills(com.lyntra.lyntraclans.domain.KillCategory.NEUTRO));
            statement.setInt(5, member.getKills(com.lyntra.lyntraclans.domain.KillCategory.CIVIL));
            statement.setInt(6, member.getDeaths());
            statement.setInt(7, member.isTrusted() ? 1 : 0);
            statement.setDouble(8, member.getWarBonusWeight());
            statement.setString(9, member.getUuid().toString());
            statement.executeUpdate();
        }
    }

    public void delete(UUID uuid) throws SQLException {
        String sql = "DELETE FROM clan_members WHERE uuid = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        }
    }

    public void deleteByClan(int clanId) throws SQLException {
        String sql = "DELETE FROM clan_members WHERE clan_id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.executeUpdate();
        }
    }

    public int countByClan(int clanId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clan_members WHERE clan_id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private ClanMember fromResultSet(ResultSet resultSet) throws SQLException {
        return new ClanMember(
                UUID.fromString(resultSet.getString("uuid")),
                resultSet.getInt("clan_id"),
                resultSet.getInt("rank_id"),
                resultSet.getLong("joined_at"),
                resultSet.getInt("kills_rival"),
                resultSet.getInt("kills_ally"),
                resultSet.getInt("kills_neutral"),
                resultSet.getInt("kills_civil"),
                resultSet.getInt("deaths"),
                resultSet.getInt("trusted") == 1,
                resultSet.getDouble("war_bonus_weight")
        );
    }
}
