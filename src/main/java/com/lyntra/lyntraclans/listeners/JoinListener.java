package com.lyntra.lyntraclans.listeners;

import com.lyntra.lyntraclans.config.LanguageManager;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.managers.ClanManager;
import com.lyntra.lyntraclans.managers.NoticeManager;
import com.lyntra.lyntraclans.managers.PlayerSettingsManager;
import com.lyntra.lyntraclans.storage.dao.NoticeDao;
import com.lyntra.lyntraclans.util.TimeFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Optional;

public final class JoinListener implements Listener {

    private final ClanManager clanManager;
    private final NoticeManager noticeManager;
    private final PlayerSettingsManager playerSettingsManager;
    private final LanguageManager languageManager;

    public JoinListener(ClanManager clanManager, NoticeManager noticeManager,
                         PlayerSettingsManager playerSettingsManager, LanguageManager languageManager) {
        this.clanManager = clanManager;
        this.noticeManager = noticeManager;
        this.playerSettingsManager = playerSettingsManager;
        this.languageManager = languageManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!playerSettingsManager.get(player.getUniqueId()).isShowWarnings()) {
            return;
        }
        Optional<Clan> clanOptional = clanManager.getClanOfPlayer(player.getUniqueId());
        if (clanOptional.isEmpty()) {
            return;
        }
        List<NoticeDao.Notice> notices = noticeManager.getNotices(clanOptional.get().getId());
        if (notices.isEmpty()) {
            return;
        }
        player.sendMessage(languageManager.get("avisos-cabecalho"));
        for (NoticeDao.Notice notice : notices) {
            String author = Bukkit.getOfflinePlayer(notice.authorUuid()).getName();
            player.sendMessage(languageManager.get("avisos-item", "autor", author == null ? "?" : author,
                    "mensagem", notice.message(), "data", TimeFormat.format(notice.createdAt())));
        }
    }
}
