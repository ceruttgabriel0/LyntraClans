package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RivalSubCommand extends AbstractClanSubCommand {

    private final Logger logger;

    public RivalSubCommand(ClanServices services, Logger logger) {
        super(services);
        this.logger = logger;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "rival-uso");
            return;
        }
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
        if (!requirePermission(player, clan, member, ClanPermission.GERENCIAR_RIVAL)) {
            return;
        }

        boolean removing = args[0].equalsIgnoreCase("remover");
        if (removing && args.length < 2) {
            usage(player, "rival-remover-uso");
            return;
        }
        String tagArg = removing ? args[1] : args[0];

        Optional<Clan> targetOptional = services.clanManager().getClanByTagOrName(tagArg);
        if (targetOptional.isEmpty()) {
            msg(player, "clan-nao-encontrado", "clan", tagArg);
            return;
        }
        Clan target = targetOptional.get();
        if (target.getId() == clan.getId()) {
            msg(player, "rival-mesmo-clan");
            return;
        }

        if (removing) {
            if (!services.relationManager().isRival(clan.getId(), target.getId())) {
                msg(player, "rival-nao-e-rival", "tag", target.getTag());
                return;
            }
            try {
                services.relationManager().removeRival(clan, target);
                msg(player, "rival-removido", "tag", target.getTag(), "nome", target.getName());
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Falha ao remover rival", e);
                msg(player, "erro-interno");
            }
            return;
        }

        if (services.relationManager().isRival(clan.getId(), target.getId())) {
            msg(player, "rival-ja-rival", "tag", target.getTag());
            return;
        }

        try {
            services.relationManager().declareRival(clan, target);
            msg(player, "rival-sucesso", "tag", target.getTag(), "nome", target.getName());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao declarar rival", e);
            msg(player, "erro-interno");
        }
    }
}
