package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AdminSubCommand extends AbstractClanSubCommand {

    private final Logger logger;

    public AdminSubCommand(ClanServices services, Logger logger) {
        super(services);
        this.logger = logger;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission("lyntraclans.admin")) {
            msg(player, "sem-permissao");
            return;
        }
        if (args.length < 1) {
            msg(player, "comando-desconhecido");
            return;
        }
        switch (args[0].toLowerCase()) {
            case "entrar" -> entrar(player, args);
            case "desfazer" -> desfazer(player, args);
            default -> msg(player, "comando-desconhecido");
        }
    }

    private void entrar(Player player, String[] args) {
        if (args.length < 3) {
            usage(player, "admin-entrar-uso");
            return;
        }
        Optional<Player> targetOptional = resolveOnlineTarget(player, args[1]);
        if (targetOptional.isEmpty()) {
            return;
        }
        Player target = targetOptional.get();
        Optional<Clan> clanOptional = services.clanManager().getClanByTag(args[2]);
        if (clanOptional.isEmpty()) {
            msg(player, "clan-nao-encontrado", "clan", args[2]);
            return;
        }
        Clan clan = clanOptional.get();
        if (services.clanManager().getClanOfPlayer(target.getUniqueId()).isPresent()) {
            msg(player, "jogador-ja-tem-clan", "jogador", target.getName());
            return;
        }
        try {
            services.clanManager().addMember(clan, target.getUniqueId(), clan.getHighestRank());
            msg(player, "admin-entrar-sucesso", "jogador", target.getName(), "tag", clan.getTag());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao forcar entrada de jogador no cla", e);
            msg(player, "erro-interno");
        }
    }

    private void desfazer(Player player, String[] args) {
        if (args.length < 2) {
            usage(player, "admin-desfazer-uso");
            return;
        }
        Optional<Clan> clanOptional = services.clanManager().getClanByTag(args[1]);
        if (clanOptional.isEmpty()) {
            msg(player, "clan-nao-encontrado", "clan", args[1]);
            return;
        }
        Clan clan = clanOptional.get();
        try {
            String tag = clan.getTag();
            services.clanManager().disbandClan(clan);
            services.inviteManager().removeAllInvitesForClan(clan.getId());
            services.relationManager().removeAllRelations(clan.getId());
            services.warManager().removeAllWars(clan.getId());
            services.noticeManager().removeAllForClan(clan.getId());
            msg(player, "admin-desfazer-sucesso", "tag", tag);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao forcar dissolucao de cla", e);
            msg(player, "erro-interno");
        }
    }
}
