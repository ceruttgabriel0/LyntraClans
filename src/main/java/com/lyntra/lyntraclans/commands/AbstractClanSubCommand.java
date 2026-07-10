package com.lyntra.lyntraclans.commands;

import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.domain.Rank;
import org.bukkit.entity.Player;

import java.util.Optional;

public abstract class AbstractClanSubCommand implements SubCommand {

    protected final ClanServices services;

    protected AbstractClanSubCommand(ClanServices services) {
        this.services = services;
    }

    protected void msg(Player player, String key, String... pairs) {
        player.sendMessage(services.languageManager().get(key, pairs));
    }

    protected void usage(Player player, String usageKey) {
        String usage = services.languageManager().raw(usageKey);
        msg(player, "argumentos-faltando", "uso", usage);
    }

    protected Optional<Clan> requireClan(Player player) {
        Optional<Clan> clan = services.clanManager().getClanOfPlayer(player.getUniqueId());
        if (clan.isEmpty()) {
            msg(player, "sem-clan");
        }
        return clan;
    }

    protected boolean requireNoClan(Player player) {
        if (services.clanManager().getClanOfPlayer(player.getUniqueId()).isPresent()) {
            msg(player, "ja-tem-clan");
            return false;
        }
        return true;
    }

    protected Optional<ClanMember> requireMember(Player player) {
        Optional<ClanMember> member = services.clanManager().getMember(player.getUniqueId());
        if (member.isEmpty()) {
            msg(player, "sem-clan");
        }
        return member;
    }

    protected Rank rankOf(Clan clan, ClanMember member) {
        return clan.getRankById(member.getRankId());
    }

    protected boolean hasPermission(Clan clan, ClanMember member, ClanPermission permission) {
        Rank rank = rankOf(clan, member);
        if (rank == null) {
            return false;
        }
        Rank highest = clan.getHighestRank();
        if (highest != null && highest.getId() == rank.getId()) {
            return true;
        }
        return rank.has(permission);
    }

    protected boolean requirePermission(Player player, Clan clan, ClanMember member, ClanPermission permission) {
        if (!hasPermission(clan, member, permission)) {
            msg(player, "sem-permissao-clan");
            return false;
        }
        return true;
    }

    protected Optional<Player> resolveOnlineTarget(Player player, String name) {
        Player target = player.getServer().getPlayerExact(name);
        if (target == null) {
            msg(player, "jogador-nao-encontrado", "jogador", name);
            return Optional.empty();
        }
        return Optional.of(target);
    }

    protected boolean isLeader(Clan clan, ClanMember member) {
        Rank highest = clan.getHighestRank();
        return highest != null && highest.getId() == member.getRankId();
    }
}
