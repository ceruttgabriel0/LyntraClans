package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AceitarSubCommand extends AbstractClanSubCommand {

    private final Logger logger;

    public AceitarSubCommand(ClanServices services, Logger logger) {
        super(services);
        this.logger = logger;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "aceitar-uso");
            return;
        }
        if (!requireNoClan(player)) {
            return;
        }
        Optional<Clan> clanOptional = services.clanManager().getClanByTag(args[0]);
        if (clanOptional.isEmpty()) {
            msg(player, "clan-nao-encontrado", "clan", args[0]);
            return;
        }
        Clan clan = clanOptional.get();
        if (!services.inviteManager().hasInvite(clan.getId(), player.getUniqueId())) {
            msg(player, "aceitar-nao-encontrado", "tag", clan.getTag());
            return;
        }
        if (services.clanManager().getMembers(clan.getId()).size() >= clan.getMaxMembers()) {
            msg(player, "convidar-clan-cheio", "atual", String.valueOf(clan.getMaxMembers()),
                    "max", String.valueOf(clan.getMaxMembers()));
            return;
        }

        try {
            services.clanManager().addMember(clan, player.getUniqueId(), clan.getDefaultRank());
            services.inviteManager().removeAllInvitesForPlayer(player.getUniqueId());
            msg(player, "aceitar-sucesso", "tag", clan.getTag(), "nome", clan.getName());
            broadcast(clan, player);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao aceitar convite", e);
            msg(player, "erro-interno");
        }
    }

    private void broadcast(Clan clan, Player joined) {
        ClanMember joinedMember = services.clanManager().getMember(joined.getUniqueId()).orElse(null);
        services.clanManager().getMembers(clan.getId()).forEach(member -> {
            if (joinedMember != null && member.getUuid().equals(joinedMember.getUuid())) {
                return;
            }
            Player online = Bukkit.getPlayer(member.getUuid());
            if (online != null) {
                online.sendMessage(services.languageManager().get("aceitar-anuncio", "jogador", joined.getName()));
            }
        });
    }
}
