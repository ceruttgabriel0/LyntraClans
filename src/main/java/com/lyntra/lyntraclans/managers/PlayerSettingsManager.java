package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.domain.PlayerSettings;
import com.lyntra.lyntraclans.storage.dao.PlayerDataDao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Cache em memoria das preferencias pessoais (consultadas a cada golpe de PvP, nao pode bater no banco toda hora). */
public final class PlayerSettingsManager {

    private final Logger logger;
    private final PlayerDataDao playerDataDao;
    private final Map<UUID, PlayerSettings> cache = new HashMap<>();

    public PlayerSettingsManager(Logger logger, PlayerDataDao playerDataDao) {
        this.logger = logger;
        this.playerDataDao = playerDataDao;
    }

    public PlayerSettings get(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> {
            try {
                return playerDataDao.getSettings(id);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Falha ao carregar preferencias do jogador " + id, e);
                return new PlayerSettings(true, true, true, com.lyntra.lyntraclans.domain.FfMode.AUTO);
            }
        });
    }

    public void save(UUID uuid, PlayerSettings settings) {
        cache.put(uuid, settings);
        try {
            playerDataDao.saveSettings(uuid, settings);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao salvar preferencias do jogador " + uuid, e);
        }
    }
}
