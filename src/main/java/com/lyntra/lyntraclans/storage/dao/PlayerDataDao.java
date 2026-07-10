package com.lyntra.lyntraclans.storage.dao;

import com.lyntra.lyntraclans.domain.FfMode;
import com.lyntra.lyntraclans.domain.PlayerSettings;
import com.lyntra.lyntraclans.storage.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PlayerDataDao {

    private final DatabaseManager databaseManager;

    public PlayerDataDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void touch(UUID uuid, String lastName) throws SQLException {
        String sql = "INSERT INTO player_data (uuid, last_name, past_clans) VALUES (?, ?, '') "
                + "ON CONFLICT(uuid) DO UPDATE SET last_name = excluded.last_name";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, lastName);
            statement.executeUpdate();
        }
    }

    public List<String> getPastClans(UUID uuid) throws SQLException {
        String sql = "SELECT past_clans FROM player_data WHERE uuid = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return new ArrayList<>();
                }
                String raw = resultSet.getString("past_clans");
                List<String> tags = new ArrayList<>();
                if (raw != null && !raw.isBlank()) {
                    for (String tag : raw.split(",")) {
                        tags.add(tag.trim());
                    }
                }
                return tags;
            }
        }
    }

    public void addPastClan(UUID uuid, String tag) throws SQLException {
        List<String> pastClans = getPastClans(uuid);
        pastClans.remove(tag);
        pastClans.add(tag);
        String joined = String.join(",", pastClans);
        String sql = "INSERT INTO player_data (uuid, last_name, past_clans) VALUES (?, '', ?) "
                + "ON CONFLICT(uuid) DO UPDATE SET past_clans = excluded.past_clans";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, joined);
            statement.executeUpdate();
        }
    }

    public PlayerSettings getSettings(UUID uuid) throws SQLException {
        String sql = "SELECT allow_invites, show_warnings, show_tag, ff_mode, sidebar_enabled "
                + "FROM player_data WHERE uuid = ?";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return new PlayerSettings(true, true, true, FfMode.AUTO, false);
                }
                FfMode ffMode;
                try {
                    ffMode = FfMode.valueOf(resultSet.getString("ff_mode"));
                } catch (IllegalArgumentException e) {
                    ffMode = FfMode.AUTO;
                }
                return new PlayerSettings(resultSet.getInt("allow_invites") == 1,
                        resultSet.getInt("show_warnings") == 1, resultSet.getInt("show_tag") == 1, ffMode,
                        resultSet.getInt("sidebar_enabled") == 1);
            }
        }
    }

    public void saveSettings(UUID uuid, PlayerSettings settings) throws SQLException {
        String sql = "INSERT INTO player_data (uuid, last_name, past_clans, allow_invites, show_warnings, "
                + "show_tag, ff_mode, sidebar_enabled) VALUES (?, '', '', ?, ?, ?, ?, ?) "
                + "ON CONFLICT(uuid) DO UPDATE SET allow_invites = excluded.allow_invites, "
                + "show_warnings = excluded.show_warnings, show_tag = excluded.show_tag, ff_mode = excluded.ff_mode, "
                + "sidebar_enabled = excluded.sidebar_enabled";
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setInt(2, settings.isAllowInvites() ? 1 : 0);
            statement.setInt(3, settings.isShowWarnings() ? 1 : 0);
            statement.setInt(4, settings.isShowTag() ? 1 : 0);
            statement.setString(5, settings.getFfMode().name());
            statement.setInt(6, settings.isSidebarEnabled() ? 1 : 0);
            statement.executeUpdate();
        }
    }
}
