package com.lyntra.lyntraclans.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

/**
 * Aplica migracoes incrementais no boot, versionadas por um inteiro guardado em schema_version.
 * Cada migracao deve ser idempotente/segura de rodar so uma vez (ADD COLUMN, novo indice, etc).
 * O schema.sql (CREATE TABLE IF NOT EXISTS) cobre o formato mais recente pra instalacoes novas;
 * as migracoes aqui cobrem instalacoes antigas que precisam alcancar esse mesmo formato.
 */
public final class SchemaMigrator {

    private static final int CURRENT_VERSION = 4;

    private static final List<Migration> MIGRATIONS = List.of(
            new Migration() {
                @Override
                public int targetVersion() {
                    return 2;
                }

                @Override
                public void apply(Connection connection) throws SQLException {
                    if (!columnExists(connection, "clan_members", "war_bonus_weight")) {
                        try (Statement statement = connection.createStatement()) {
                            statement.execute(
                                    "ALTER TABLE clan_members ADD COLUMN war_bonus_weight REAL NOT NULL DEFAULT 0");
                        }
                    }
                }
            },
            new Migration() {
                @Override
                public int targetVersion() {
                    return 3;
                }

                @Override
                public void apply(Connection connection) throws SQLException {
                    if (!columnExists(connection, "clans", "chest_contents")) {
                        try (Statement statement = connection.createStatement()) {
                            statement.execute(
                                    "ALTER TABLE clans ADD COLUMN chest_contents TEXT NOT NULL DEFAULT ''");
                        }
                    }
                }
            },
            new Migration() {
                @Override
                public int targetVersion() {
                    return 4;
                }

                @Override
                public void apply(Connection connection) throws SQLException {
                    if (!columnExists(connection, "clans", "xp")) {
                        try (Statement statement = connection.createStatement()) {
                            statement.execute("ALTER TABLE clans ADD COLUMN xp INTEGER NOT NULL DEFAULT 0");
                        }
                    }
                    if (!columnExists(connection, "clans", "level")) {
                        try (Statement statement = connection.createStatement()) {
                            statement.execute("ALTER TABLE clans ADD COLUMN level INTEGER NOT NULL DEFAULT 1");
                        }
                    }
                }
            }
    );

    private final Connection connection;
    private final Logger logger;

    public SchemaMigrator(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }

    public void migrate() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS schema_version (version INTEGER NOT NULL)");
        }
        int version = readVersion();
        if (version == 0) {
            writeVersion(CURRENT_VERSION);
            return;
        }
        for (Migration migration : MIGRATIONS) {
            if (migration.targetVersion() > version) {
                logger.info("Aplicando migracao de schema para versao " + migration.targetVersion() + "...");
                migration.apply(connection);
                version = migration.targetVersion();
                writeVersion(version);
            }
        }
    }

    private int readVersion() throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT version FROM schema_version LIMIT 1")) {
            if (resultSet.next()) {
                return resultSet.getInt("version");
            }
        }
        return 0;
    }

    private void writeVersion(int version) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM schema_version");
            statement.execute("INSERT INTO schema_version (version) VALUES (" + version + ")");
        }
    }

    public static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (resultSet.next()) {
                if (resultSet.getString("name").equalsIgnoreCase(column)) {
                    return true;
                }
            }
        }
        return false;
    }

    public interface Migration {
        int targetVersion();

        void apply(Connection connection) throws SQLException;
    }
}
