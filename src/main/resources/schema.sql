CREATE TABLE IF NOT EXISTS clans (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tag TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL UNIQUE,
    color TEXT NOT NULL DEFAULT 'WHITE',
    description TEXT NOT NULL DEFAULT '',
    balance REAL NOT NULL DEFAULT 0,
    fee REAL NOT NULL DEFAULT 0,
    fee_enabled INTEGER NOT NULL DEFAULT 0,
    friendly_fire INTEGER NOT NULL DEFAULT 0,
    max_members INTEGER NOT NULL DEFAULT 10,
    chest_size INTEGER NOT NULL DEFAULT 9,
    home_world TEXT,
    home_x REAL,
    home_y REAL,
    home_z REAL,
    home_yaw REAL,
    home_pitch REAL,
    founded_at INTEGER NOT NULL,
    last_used_at INTEGER NOT NULL,
    verified INTEGER NOT NULL DEFAULT 0,
    flags TEXT NOT NULL DEFAULT '',
    chest_contents TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS clan_ranks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    clan_id INTEGER NOT NULL REFERENCES clans(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    priority INTEGER NOT NULL,
    permissions TEXT NOT NULL DEFAULT '',
    is_default INTEGER NOT NULL DEFAULT 0,
    display_name TEXT,
    UNIQUE(clan_id, name)
);

CREATE TABLE IF NOT EXISTS clan_members (
    uuid TEXT PRIMARY KEY,
    clan_id INTEGER NOT NULL REFERENCES clans(id) ON DELETE CASCADE,
    rank_id INTEGER NOT NULL REFERENCES clan_ranks(id),
    joined_at INTEGER NOT NULL,
    kills_rival INTEGER NOT NULL DEFAULT 0,
    kills_ally INTEGER NOT NULL DEFAULT 0,
    kills_neutral INTEGER NOT NULL DEFAULT 0,
    kills_civil INTEGER NOT NULL DEFAULT 0,
    deaths INTEGER NOT NULL DEFAULT 0,
    trusted INTEGER NOT NULL DEFAULT 0,
    war_bonus_weight REAL NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS clan_relations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    clan_id INTEGER NOT NULL REFERENCES clans(id) ON DELETE CASCADE,
    target_clan_id INTEGER NOT NULL REFERENCES clans(id) ON DELETE CASCADE,
    type TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    UNIQUE(clan_id, target_clan_id)
);

CREATE TABLE IF NOT EXISTS clan_wars (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    clan_id INTEGER NOT NULL REFERENCES clans(id) ON DELETE CASCADE,
    target_clan_id INTEGER NOT NULL REFERENCES clans(id) ON DELETE CASCADE,
    started_at INTEGER NOT NULL,
    UNIQUE(clan_id, target_clan_id)
);

CREATE TABLE IF NOT EXISTS clan_invites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    clan_id INTEGER NOT NULL REFERENCES clans(id) ON DELETE CASCADE,
    player_uuid TEXT NOT NULL,
    invited_at INTEGER NOT NULL,
    UNIQUE(clan_id, player_uuid)
);

CREATE TABLE IF NOT EXISTS clan_notices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    clan_id INTEGER NOT NULL REFERENCES clans(id) ON DELETE CASCADE,
    author_uuid TEXT NOT NULL,
    message TEXT NOT NULL,
    created_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS player_data (
    uuid TEXT PRIMARY KEY,
    last_name TEXT,
    past_clans TEXT NOT NULL DEFAULT '',
    allow_invites INTEGER NOT NULL DEFAULT 1,
    show_warnings INTEGER NOT NULL DEFAULT 1,
    show_tag INTEGER NOT NULL DEFAULT 1,
    ff_mode TEXT NOT NULL DEFAULT 'AUTO'
);
