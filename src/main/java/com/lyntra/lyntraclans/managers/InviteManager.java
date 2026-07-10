package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.storage.dao.InviteDao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class InviteManager {

    private final Logger logger;
    private final InviteDao inviteDao;

    public InviteManager(Logger logger, InviteDao inviteDao) {
        this.logger = logger;
        this.inviteDao = inviteDao;
    }

    public boolean hasInvite(int clanId, UUID playerUuid) {
        try {
            return inviteDao.exists(clanId, playerUuid);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao consultar convite", e);
            return false;
        }
    }

    public void invite(int clanId, UUID playerUuid) {
        try {
            inviteDao.insert(clanId, playerUuid, System.currentTimeMillis());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao salvar convite", e);
        }
    }

    public List<InviteDao.Invite> getInvites(UUID playerUuid) {
        try {
            return inviteDao.findByPlayer(playerUuid);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao consultar convites", e);
            return List.of();
        }
    }

    public void removeInvite(int clanId, UUID playerUuid) {
        try {
            inviteDao.delete(clanId, playerUuid);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao remover convite", e);
        }
    }

    public void removeAllInvitesForPlayer(UUID playerUuid) {
        try {
            inviteDao.deleteAllForPlayer(playerUuid);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao remover convites do jogador", e);
        }
    }

    public void removeAllInvitesForClan(int clanId) {
        try {
            inviteDao.deleteByClan(clanId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao remover convites do cla", e);
        }
    }
}
