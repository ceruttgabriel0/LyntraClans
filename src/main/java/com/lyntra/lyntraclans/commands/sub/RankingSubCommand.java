package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

public final class RankingSubCommand extends AbstractClanSubCommand {

    public RankingSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        List<Clan> clans = services.clanManager().getAllClans().stream()
                .sorted(Comparator.comparingDouble((Clan c) -> services.killManager().clanWeightedKdr(c)).reversed())
                .limit(10)
                .toList();
        if (clans.isEmpty()) {
            msg(player, "ranking-vazio");
            return;
        }
        msg(player, "ranking-cabecalho");
        int position = 1;
        for (Clan clan : clans) {
            player.sendMessage(services.languageManager().get("ranking-item", "posicao", String.valueOf(position),
                    "tag", clan.getTag(), "nome", clan.getName(),
                    "kdr", String.format("%.2f", services.killManager().clanWeightedKdr(clan))));
            position++;
        }
    }
}
