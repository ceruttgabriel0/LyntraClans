package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DesfazerSubCommand extends AbstractClanSubCommand {

    private static final long CONFIRM_WINDOW_MILLIS = 30_000L;

    private final Logger logger;
    private final Map<UUID, Long> pendingConfirmations = new HashMap<>();

    public DesfazerSubCommand(ClanServices services, Logger logger) {
        super(services);
        this.logger = logger;
    }

    @Override
    public void execute(Player player, String[] args) {
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        Optional<ClanMember> memberOptional = services.clanManager().getMember(player.getUniqueId());
        if (memberOptional.isEmpty() || !isLeader(clan, memberOptional.get())) {
            msg(player, "desfazer-somente-lider");
            return;
        }

        boolean confirming = args.length >= 1 && args[0].equalsIgnoreCase("confirmar");
        Long requestedAt = pendingConfirmations.get(player.getUniqueId());
        if (!confirming) {
            pendingConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
            msg(player, "desfazer-confirmar", "tag", clan.getTag());
            return;
        }
        if (requestedAt == null || System.currentTimeMillis() - requestedAt > CONFIRM_WINDOW_MILLIS) {
            msg(player, "desfazer-expirado");
            return;
        }
        pendingConfirmations.remove(player.getUniqueId());
        try {
            String tag = clan.getTag();
            services.clanManager().disbandClan(clan);
            services.inviteManager().removeAllInvitesForClan(clan.getId());
            services.relationManager().removeAllRelations(clan.getId());
            services.warManager().removeAllWars(clan.getId());
            services.noticeManager().removeAllForClan(clan.getId());
            msg(player, "desfazer-sucesso", "tag", tag);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao desfazer cla", e);
            msg(player, "erro-interno");
        }
    }
}
