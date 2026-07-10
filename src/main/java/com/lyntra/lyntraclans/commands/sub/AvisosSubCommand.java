package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.storage.dao.NoticeDao;
import com.lyntra.lyntraclans.util.TimeFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public final class AvisosSubCommand extends AbstractClanSubCommand {

    public AvisosSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();

        if (args.length >= 1 && args[0].equalsIgnoreCase("adicionar")) {
            ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
            if (!requirePermission(player, clan, member, ClanPermission.BOLETIM)) {
                return;
            }
            if (args.length < 2) {
                usage(player, "avisos-adicionar-uso");
                return;
            }
            String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            services.noticeManager().post(clan.getId(), player.getUniqueId(), message);
            msg(player, "avisos-adicionado");
            return;
        }

        List<NoticeDao.Notice> notices = services.noticeManager().getNotices(clan.getId());
        if (notices.isEmpty()) {
            msg(player, "avisos-vazio");
            return;
        }
        msg(player, "avisos-cabecalho");
        for (NoticeDao.Notice notice : notices) {
            String authorName = Bukkit.getOfflinePlayer(notice.authorUuid()).getName();
            player.sendMessage(services.languageManager().get("avisos-item",
                    "autor", authorName == null ? "?" : authorName, "mensagem", notice.message(),
                    "data", TimeFormat.format(notice.createdAt())));
        }
    }
}
