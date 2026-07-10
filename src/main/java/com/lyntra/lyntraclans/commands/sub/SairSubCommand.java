package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SairSubCommand extends AbstractClanSubCommand {

    private static final long CONFIRM_WINDOW_MILLIS = 30_000L;

    private final Logger logger;
    private final Map<UUID, Long> pendingConfirmations = new HashMap<>();

    public SairSubCommand(ClanServices services, Logger logger) {
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

        boolean confirming = args.length >= 1 && args[0].equalsIgnoreCase("confirmar");
        Long requestedAt = pendingConfirmations.get(player.getUniqueId());
        if (!confirming) {
            pendingConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
            msg(player, "sair-confirmar", "tag", clan.getTag());
            // Se for o unico membro, sair tambem desfaz o cla (mesmo caminho de codigo la embaixo) -
            // mesmo aviso de saldo perdido que o /clan desfazer ja da, pra nao pegar ninguem de surpresa.
            if (services.clanManager().getMembers(clan.getId()).size() <= 1 && clan.getBalance() > 0) {
                msg(player, "desfazer-confirmar-saldo", "saldo", services.bankManager().format(clan.getBalance()));
            }
            return;
        }
        if (requestedAt == null || System.currentTimeMillis() - requestedAt > CONFIRM_WINDOW_MILLIS) {
            msg(player, "sair-expirado");
            return;
        }
        pendingConfirmations.remove(player.getUniqueId());

        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
        int totalMembers = services.clanManager().getMembers(clan.getId()).size();

        boolean onlyLeader = isLeader(clan, member)
                && services.clanManager().getMembers(clan.getId()).stream().filter(other -> isLeader(clan, other))
                .count() <= 1
                && totalMembers > 1;
        if (onlyLeader) {
            msg(player, "sair-somente-lider-transferir");
            return;
        }

        services.memberManager().kick(clan, member);
        msg(player, "sair-sucesso", "tag", clan.getTag());
        if (totalMembers > 1) {
            broadcastToClan(clan, "sair-membro-saiu", "jogador", player.getName());
        } else {
            try {
                services.clanManager().disbandClan(clan);
                services.inviteManager().removeAllInvitesForClan(clan.getId());
                services.relationManager().removeAllRelations(clan.getId());
                services.warManager().removeAllWars(clan.getId());
                services.noticeManager().removeAllForClan(clan.getId());
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Falha ao desfazer cla vazio", e);
            }
        }
    }

    private void broadcastToClan(Clan clan, String key, String... pairs) {
        services.clanManager().getMembers(clan.getId()).forEach(member -> {
            Player online = Bukkit.getPlayer(member.getUuid());
            if (online != null) {
                online.sendMessage(services.languageManager().get(key, pairs));
            }
        });
    }
}
