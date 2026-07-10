package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.storage.dao.WarDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WarManager {

    private final Logger logger;
    private final WarDao warDao;

    public WarManager(Logger logger, WarDao warDao) {
        this.logger = logger;
        this.warDao = warDao;
    }

    public boolean startWar(Clan clan, Clan target) {
        try {
            if (warDao.isAtWar(clan.getId(), target.getId())) {
                return false;
            }
            warDao.insert(clan.getId(), target.getId(), System.currentTimeMillis());
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao iniciar guerra", e);
            return false;
        }
    }

    public boolean endWar(Clan clan, Clan target) {
        try {
            if (!warDao.isAtWar(clan.getId(), target.getId())) {
                return false;
            }
            warDao.end(clan.getId(), target.getId());
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao finalizar guerra", e);
            return false;
        }
    }

    public boolean isAtWar(int clanId, int targetClanId) {
        try {
            return warDao.isAtWar(clanId, targetClanId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao consultar guerra", e);
            return false;
        }
    }

    public List<Clan> getWarTargets(Clan clan, ClanManager clanManager) {
        List<Clan> result = new ArrayList<>();
        try {
            for (WarDao.War war : warDao.findByClan(clan.getId())) {
                int otherId = war.clanId() == clan.getId() ? war.targetClanId() : war.clanId();
                clanManager.getClanById(otherId).ifPresent(result::add);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao consultar guerras do cla", e);
        }
        return result;
    }

    public void removeAllWars(int clanId) {
        try {
            warDao.deleteByClan(clanId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao remover guerras do cla", e);
        }
    }
}
