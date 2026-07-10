package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.KillCategory;
import com.lyntra.lyntraclans.domain.Rank;
import com.lyntra.lyntraclans.storage.dao.PlayerDataDao;
import com.lyntra.lyntraclans.util.TimeFormat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PerfilSubCommand extends AbstractClanSubCommand {

    private final PlayerDataDao playerDataDao;
    private final Logger logger;

    public PerfilSubCommand(ClanServices services, PlayerDataDao playerDataDao, Logger logger) {
        super(services);
        this.playerDataDao = playerDataDao;
        this.logger = logger;
    }

    @Override
    public void execute(Player player, String[] args) {
        OfflinePlayer target = args.length >= 1 ? Bukkit.getOfflinePlayer(args[0]) : player;
        if (args.length >= 1 && !target.hasPlayedBefore() && !target.isOnline()) {
            msg(player, "jogador-nunca-entrou", "jogador", args[0]);
            return;
        }
        String targetName = target.getName() == null ? args.length >= 1 ? args[0] : player.getName() : target.getName();

        Optional<ClanMember> memberOptional = services.clanManager().getMember(target.getUniqueId());
        Optional<Clan> clanOptional = memberOptional.flatMap(m -> services.clanManager().getClanById(m.getClanId()));

        String clanDisplay = clanOptional.map(c -> "[" + c.getTag() + "] " + c.getName())
                .orElse(services.languageManager().raw("lista-vazia-generica"));
        String cargoDisplay = "-";
        double kdr = 0;
        int rival = 0;
        int aliado = 0;
        int neutro = 0;
        int civil = 0;
        int mortes = 0;
        String entrouDisplay = "-";

        if (memberOptional.isPresent() && clanOptional.isPresent()) {
            ClanMember member = memberOptional.get();
            Rank rank = clanOptional.get().getRankById(member.getRankId());
            cargoDisplay = rank == null ? "-" : rank.getName();
            kdr = services.killManager().weightedKdr(member);
            rival = member.getKills(KillCategory.RIVAL);
            aliado = member.getKills(KillCategory.ALIADO);
            neutro = member.getKills(KillCategory.NEUTRO);
            civil = member.getKills(KillCategory.CIVIL);
            mortes = member.getDeaths();
            entrouDisplay = TimeFormat.format(member.getJoinedAt());
        }

        List<String> pastClans;
        try {
            pastClans = playerDataDao.getPastClans(target.getUniqueId());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao consultar clas anteriores", e);
            pastClans = List.of();
        }
        String pastClansDisplay = pastClans.isEmpty() ? services.languageManager().raw("lista-vazia-generica")
                : String.join(", ", pastClans);

        player.sendMessage(services.languageManager().get("perfil-cabecalho", "jogador", targetName));
        player.sendMessage(services.languageManager().get("perfil-clan", "clan", clanDisplay));
        player.sendMessage(services.languageManager().get("perfil-cargo", "cargo", cargoDisplay));
        player.sendMessage(services.languageManager().get("perfil-kdr", "kdr", String.format("%.2f", kdr)));
        player.sendMessage(services.languageManager().get("perfil-kills", "rival", String.valueOf(rival),
                "aliado", String.valueOf(aliado), "neutro", String.valueOf(neutro), "civil", String.valueOf(civil)));
        player.sendMessage(services.languageManager().get("perfil-mortes", "mortes", String.valueOf(mortes)));
        player.sendMessage(services.languageManager().get("perfil-entrou", "data", entrouDisplay));
        player.sendMessage(services.languageManager().get("perfil-clans-anteriores", "clans", pastClansDisplay));
    }
}
