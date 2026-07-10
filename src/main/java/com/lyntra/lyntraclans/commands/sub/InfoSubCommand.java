package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.KillCategory;
import com.lyntra.lyntraclans.util.TimeFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class InfoSubCommand extends AbstractClanSubCommand {

    public InfoSubCommand(ClanServices services) {
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
        List<ClanMember> members = services.clanManager().getMembers(clan.getId());

        String leaders = members.stream()
                .filter(m -> clan.getHighestRank() != null && m.getRankId() == clan.getHighestRank().getId())
                .map(m -> Bukkit.getOfflinePlayer(m.getUuid()).getName())
                .filter(name -> name != null)
                .collect(Collectors.joining(", "));
        if (leaders.isBlank()) {
            leaders = services.languageManager().raw("lista-vazia-generica");
        }

        long online = members.stream().map(ClanMember::getUuid).map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline()).count();

        String allies = formatClanList(services.relationManager().getAllies(clan, services.clanManager()));
        String rivals = formatClanList(services.relationManager().getRivals(clan, services.clanManager()));

        player.sendMessage(services.languageManager().get("info-cabecalho", "tag", clan.getTag(), "nome", clan.getName()));
        player.sendMessage(services.languageManager().get("info-descricao", "descricao",
                clan.getDescription().isBlank() ? services.languageManager().raw("lista-vazia-generica") : clan.getDescription()));
        player.sendMessage(services.languageManager().get("info-estado", "estado",
                services.languageManager().raw(clan.isVerified() ? "info-verificado" : "info-nao-verificado")));
        player.sendMessage(services.languageManager().get("info-lideres", "lideres", leaders));
        player.sendMessage(services.languageManager().get("info-membros", "online", String.valueOf(online),
                "total", String.valueOf(members.size())));
        player.sendMessage(services.languageManager().get("info-kdr", "kdr",
                String.format("%.2f", services.killManager().clanWeightedKdr(clan))));
        player.sendMessage(services.languageManager().get("info-kills",
                "rival", String.valueOf(services.killManager().clanKills(clan, KillCategory.RIVAL)),
                "aliado", String.valueOf(services.killManager().clanKills(clan, KillCategory.ALIADO)),
                "neutro", String.valueOf(services.killManager().clanKills(clan, KillCategory.NEUTRO)),
                "civil", String.valueOf(services.killManager().clanKills(clan, KillCategory.CIVIL))));
        player.sendMessage(services.languageManager().get("info-mortes", "mortes",
                String.valueOf(services.killManager().clanDeaths(clan))));
        player.sendMessage(services.languageManager().get("info-banco", "saldo",
                services.bankManager().format(clan.getBalance())));
        player.sendMessage(services.languageManager().get("info-taxa", "taxa",
                clan.isFeeEnabled() ? services.bankManager().format(clan.getFee()) : "desativada"));
        player.sendMessage(services.languageManager().get("info-aliados", "aliados", allies));
        player.sendMessage(services.languageManager().get("info-rivais", "rivais", rivals));
        player.sendMessage(services.languageManager().get("info-fundado", "data", TimeFormat.format(clan.getFoundedAt())));
        player.sendMessage(services.languageManager().get("info-inativo", "dias",
                String.valueOf(TimeFormat.daysSince(clan.getLastUsedAt()))));
    }

    private String formatClanList(List<Clan> clans) {
        if (clans.isEmpty()) {
            return services.languageManager().raw("lista-vazia-generica");
        }
        return clans.stream().map(c -> "[" + c.getTag() + "]").collect(Collectors.joining(", "));
    }
}
