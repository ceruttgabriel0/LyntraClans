package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.config.ConfigManager;
import com.lyntra.lyntraclans.domain.Clan;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Desfaz automaticamente clas inativos ha mais de {@code inactivity.clan-purge-days}. O campo
 * {@code lastUsedAt} (ver ClanManager.touchActivity, chamado em qualquer acao relevante: chat,
 * kill, boletim, deposito etc) ja existia desde o desenho inicial do schema especificamente pra
 * isso, mas nunca tinha sido ligado a uma rotina de verdade - a config ficava sem efeito nenhum.
 * purge-days <= 0 desliga a limpeza automatica (dono do servidor pode nao querer isso).
 */
public final class InactivityManager {

    private final Logger logger;
    private final ConfigManager configManager;
    private final ClanManager clanManager;
    private final InviteManager inviteManager;
    private final RelationManager relationManager;
    private final WarManager warManager;
    private final NoticeManager noticeManager;

    public InactivityManager(Logger logger, ConfigManager configManager, ClanManager clanManager,
                              InviteManager inviteManager, RelationManager relationManager,
                              WarManager warManager, NoticeManager noticeManager) {
        this.logger = logger;
        this.configManager = configManager;
        this.clanManager = clanManager;
        this.inviteManager = inviteManager;
        this.relationManager = relationManager;
        this.warManager = warManager;
        this.noticeManager = noticeManager;
    }

    /** Roda numa task periodica. Retorna as tags dos clas que foram desfeitos, pra log/auditoria. */
    public List<String> purgeInactiveClans() {
        List<String> purged = new ArrayList<>();
        int purgeDays = configManager.clanPurgeDays();
        if (purgeDays <= 0) {
            return purged;
        }
        long thresholdMillis = purgeDays * 24L * 60L * 60L * 1000L;
        long now = System.currentTimeMillis();
        for (Clan clan : List.copyOf(clanManager.getAllClans())) {
            if (now - clan.getLastUsedAt() < thresholdMillis) {
                continue;
            }
            try {
                String tag = clan.getTag();
                clanManager.disbandClan(clan);
                inviteManager.removeAllInvitesForClan(clan.getId());
                relationManager.removeAllRelations(clan.getId());
                warManager.removeAllWars(clan.getId());
                noticeManager.removeAllForClan(clan.getId());
                purged.add(tag);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Falha ao desfazer cla inativo " + clan.getTag(), e);
            }
        }
        return purged;
    }
}
