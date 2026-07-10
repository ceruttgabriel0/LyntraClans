package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class MembrosSubCommand extends AbstractClanSubCommand {

    public MembrosSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        Optional<Clan> clanOptional;
        if (args.length >= 1) {
            clanOptional = services.clanManager().getClanByTagOrName(args[0]);
            if (clanOptional.isEmpty()) {
                msg(player, "clan-nao-encontrado", "clan", args[0]);
                return;
            }
        } else {
            clanOptional = requireClan(player);
            if (clanOptional.isEmpty()) {
                return;
            }
        }
        Clan clan = clanOptional.get();
        msg(player, "membros-cabecalho", "tag", clan.getTag(), "nome", clan.getName());
        for (ClanMember member : services.clanManager().getMembers(clan.getId())) {
            String name = Bukkit.getOfflinePlayer(member.getUuid()).getName();
            Rank rank = clan.getRankById(member.getRankId());
            boolean online = Bukkit.getPlayer(member.getUuid()) != null;
            player.sendMessage(services.languageManager().get("membros-item",
                    "jogador", name == null ? member.getUuid().toString() : name,
                    "cargo", rank == null ? "-" : rank.getDisplayName(),
                    "status", services.languageManager().raw(online ? "status-online" : "status-offline"),
                    "confiavel", member.isTrusted() ? services.languageManager().raw("sim") : ""));
        }
    }
}
