package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

public final class ListaSubCommand extends AbstractClanSubCommand {

    public ListaSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        Comparator<Clan> comparator = resolveComparator(args);
        List<Clan> clans = services.clanManager().getAllClans().stream()
                .sorted(comparator)
                .toList();
        if (clans.isEmpty()) {
            msg(player, "lista-vazia");
            return;
        }
        msg(player, "lista-cabecalho", "total", String.valueOf(clans.size()));
        for (Clan clan : clans) {
            int members = services.clanManager().getMembers(clan.getId()).size();
            player.sendMessage(services.languageManager().get("lista-item", "tag", clan.getTag(),
                    "nome", clan.getName(), "membros", String.valueOf(members)));
        }
    }

    /** Suporta "/clan listar ordem <nome|membros|kdr>", padrão nome (alfabético). */
    private Comparator<Clan> resolveComparator(String[] args) {
        if (args.length >= 2 && args[0].equalsIgnoreCase("ordem")) {
            return switch (args[1].toLowerCase()) {
                case "membros" -> Comparator
                        .comparingInt((Clan c) -> services.clanManager().getMembers(c.getId()).size()).reversed();
                case "kdr" -> Comparator
                        .comparingDouble((Clan c) -> services.killManager().clanWeightedKdr(c)).reversed();
                default -> Comparator.comparing(Clan::getName, String.CASE_INSENSITIVE_ORDER);
            };
        }
        return Comparator.comparing(Clan::getName, String.CASE_INSENSITIVE_ORDER);
    }
}
