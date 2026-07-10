package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.Rank;
import com.lyntra.lyntraclans.storage.dao.PlayerDataDao;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MemberManager {

    private final Logger logger;
    private final ClanManager clanManager;
    private final PlayerDataDao playerDataDao;

    public MemberManager(Logger logger, ClanManager clanManager, PlayerDataDao playerDataDao) {
        this.logger = logger;
        this.clanManager = clanManager;
        this.playerDataDao = playerDataDao;
    }

    public void setRank(ClanMember member, Rank rank) {
        member.setRankId(rank.getId());
        clanManager.persistMember(member);
    }

    public void transferLeadership(Clan clan, ClanMember currentLeader, ClanMember newLeader) {
        Rank highest = clan.getHighestRank();
        Rank defaultRank = clan.getDefaultRank();
        newLeader.setRankId(highest.getId());
        clanManager.persistMember(newLeader);
        if (defaultRank != null && currentLeader.getUuid() != newLeader.getUuid()) {
            currentLeader.setRankId(defaultRank.getId());
            clanManager.persistMember(currentLeader);
        }
    }

    public void kick(Clan clan, ClanMember member) {
        try {
            clanManager.removeMember(member);
            playerDataDao.addPastClan(member.getUuid(), clan.getTag());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao remover membro do cla", e);
        }
    }
}
