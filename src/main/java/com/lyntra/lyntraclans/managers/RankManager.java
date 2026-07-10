package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.domain.Rank;
import com.lyntra.lyntraclans.storage.dao.RankDao;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RankManager {

    private final Logger logger;
    private final RankDao rankDao;

    public RankManager(Logger logger, RankDao rankDao) {
        this.logger = logger;
        this.rankDao = rankDao;
    }

    public Rank createRank(Clan clan, String name) throws SQLException {
        int priority = clan.getDefaultRank() == null ? 5 : clan.getDefaultRank().getPriority() - 5;
        if (priority < 1) {
            priority = 1;
        }
        Rank rank = rankDao.insert(clan.getId(), name, priority, EnumSet.noneOf(ClanPermission.class));
        clan.getRanks().add(rank);
        return rank;
    }

    public void deleteRank(Clan clan, Rank rank) throws SQLException {
        rankDao.delete(rank.getId());
        clan.getRanks().remove(rank);
    }

    public void grantPermission(Rank rank, ClanPermission permission) {
        rank.grant(permission);
        persist(rank);
    }

    public void revokePermission(Rank rank, ClanPermission permission) {
        rank.revoke(permission);
        persist(rank);
    }

    public int countMembersUsingRank(Rank rank) throws SQLException {
        return rankDao.countMembersUsingRank(rank.getId());
    }

    public void persist(Rank rank) {
        try {
            rankDao.update(rank);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao salvar cargo " + rank.getName(), e);
        }
    }
}
