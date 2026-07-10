package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

public final class ListarSaldoSubCommand extends AbstractClanSubCommand {

    public ListarSaldoSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        List<Clan> clans = services.clanManager().getAllClans().stream()
                .sorted(Comparator.comparingDouble(Clan::getBalance).reversed())
                .limit(10)
                .toList();
        if (clans.isEmpty()) {
            msg(player, "listarsaldo-vazio");
            return;
        }
        msg(player, "listarsaldo-cabecalho");
        int position = 1;
        for (Clan clan : clans) {
            player.sendMessage(services.languageManager().get("listarsaldo-item", "posicao", String.valueOf(position),
                    "tag", clan.getTag(), "nome", clan.getName(), "saldo", services.bankManager().format(clan.getBalance())));
            position++;
        }
    }
}
