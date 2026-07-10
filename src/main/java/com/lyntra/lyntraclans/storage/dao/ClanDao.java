package com.lyntra.lyntraclans.storage.dao;

import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.storage.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ClanDao {

    private final DatabaseManager databaseManager;

    public ClanDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Clan insert(String tag, String name, String color, long now) throws SQLException {
        Connection connection = databaseManager.getConnection();
        String sql = "INSERT INTO clans (tag, name, color, description, balance, fee, fee_enabled, friendly_fire, "
                + "max_members, chest_size, founded_at, last_used_at, verified, flags) "
                + "VALUES (?, ?, ?, '', 0, 0, 0, 0, ?, 9, ?, ?, 0, '')";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, tag);
            statement.setString(2, name);
            statement.setString(3, color);
            statement.setInt(4, 10);
            statement.setLong(5, now);
            statement.setLong(6, now);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);
                return mapClan(id, tag, name, color, "", 0, 0, false, false, 10, 9, now, now, false, "", 0, 1);
            }
        }
    }

    public Optional<Clan> findById(int id) throws SQLException {
        String sql = "SELECT * FROM clans WHERE id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(fromResultSet(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Clan> findByTag(String tag) throws SQLException {
        String sql = "SELECT * FROM clans WHERE tag = ? COLLATE NOCASE";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, tag);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(fromResultSet(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Clan> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM clans WHERE name = ? COLLATE NOCASE";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(fromResultSet(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<Clan> findAll() throws SQLException {
        List<Clan> clans = new ArrayList<>();
        String sql = "SELECT * FROM clans ORDER BY name COLLATE NOCASE";
        try (Statement statement = databaseManager.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                clans.add(fromResultSet(resultSet));
            }
        }
        return clans;
    }

    public void update(Clan clan) throws SQLException {
        String sql = "UPDATE clans SET tag = ?, name = ?, color = ?, description = ?, balance = ?, fee = ?, "
                + "fee_enabled = ?, friendly_fire = ?, max_members = ?, chest_size = ?, home_world = ?, home_x = ?, "
                + "home_y = ?, home_z = ?, home_yaw = ?, home_pitch = ?, last_used_at = ?, verified = ?, "
                + "chest_contents = ?, xp = ?, level = ? WHERE id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, clan.getTag());
            statement.setString(2, clan.getName());
            statement.setString(3, clan.getColor());
            statement.setString(4, clan.getDescription());
            statement.setDouble(5, clan.getBalance());
            statement.setDouble(6, clan.getFee());
            statement.setInt(7, clan.isFeeEnabled() ? 1 : 0);
            statement.setInt(8, clan.isFriendlyFire() ? 1 : 0);
            statement.setInt(9, clan.getMaxMembers());
            statement.setInt(10, clan.getChestSize());
            if (clan.hasHome()) {
                statement.setString(11, clan.getHomeWorld());
                statement.setDouble(12, clan.getHomeX());
                statement.setDouble(13, clan.getHomeY());
                statement.setDouble(14, clan.getHomeZ());
                statement.setFloat(15, clan.getHomeYaw());
                statement.setFloat(16, clan.getHomePitch());
            } else {
                statement.setNull(11, java.sql.Types.VARCHAR);
                statement.setNull(12, java.sql.Types.REAL);
                statement.setNull(13, java.sql.Types.REAL);
                statement.setNull(14, java.sql.Types.REAL);
                statement.setNull(15, java.sql.Types.REAL);
                statement.setNull(16, java.sql.Types.REAL);
            }
            statement.setLong(17, clan.getLastUsedAt());
            statement.setInt(18, clan.isVerified() ? 1 : 0);
            statement.setString(19, clan.getChestContents());
            statement.setLong(20, clan.getXp());
            statement.setInt(21, clan.getLevel());
            statement.setInt(22, clan.getId());
            statement.executeUpdate();
        }
    }

    public void delete(int clanId) throws SQLException {
        String sql = "DELETE FROM clans WHERE id = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clanId);
            statement.executeUpdate();
        }
    }

    private Clan fromResultSet(ResultSet resultSet) throws SQLException {
        Clan clan = mapClan(
                resultSet.getInt("id"),
                resultSet.getString("tag"),
                resultSet.getString("name"),
                resultSet.getString("color"),
                resultSet.getString("description"),
                resultSet.getDouble("balance"),
                resultSet.getDouble("fee"),
                resultSet.getInt("fee_enabled") == 1,
                resultSet.getInt("friendly_fire") == 1,
                resultSet.getInt("max_members"),
                resultSet.getInt("chest_size"),
                resultSet.getLong("founded_at"),
                resultSet.getLong("last_used_at"),
                resultSet.getInt("verified") == 1,
                resultSet.getString("chest_contents"),
                resultSet.getLong("xp"),
                resultSet.getInt("level")
        );
        String homeWorld = resultSet.getString("home_world");
        if (homeWorld != null) {
            clan.setHome(homeWorld, resultSet.getDouble("home_x"), resultSet.getDouble("home_y"),
                    resultSet.getDouble("home_z"), resultSet.getFloat("home_yaw"), resultSet.getFloat("home_pitch"));
        }
        return clan;
    }

    private Clan mapClan(int id, String tag, String name, String color, String description, double balance,
                          double fee, boolean feeEnabled, boolean friendlyFire, int maxMembers, int chestSize,
                          long foundedAt, long lastUsedAt, boolean verified, String chestContents, long xp,
                          int level) {
        return new Clan(id, tag, name, color, description, balance, fee, feeEnabled, friendlyFire, maxMembers,
                chestSize, foundedAt, lastUsedAt, verified, chestContents, xp, level);
    }
}
