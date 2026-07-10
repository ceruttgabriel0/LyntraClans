package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class EstatisticasSubCommand extends AbstractClanSubCommand {

    public EstatisticasSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        List<ClanMember> members = services.clanManager().getMembers(clan.getId()).stream()
                .sorted(Comparator.comparingDouble((ClanMember m) -> services.killManager().weightedKdr(m)).reversed())
                .toList();
        msg(player, "estatisticas-cabecalho", "tag", clan.getTag());
        for (ClanMember member : members) {
            String name = Bukkit.getOfflinePlayer(member.getUuid()).getName();
            player.sendMessage(services.languageManager().get("estatisticas-item",
                    "jogador", name == null ? member.getUuid().toString() : name,
                    "kdr", String.format("%.2f", services.killManager().weightedKdr(member)),
                    "abates", String.valueOf(member.getTotalKills()),
                    "mortes", String.valueOf(member.getDeaths())));
        }
    }
}
