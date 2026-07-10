package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.PlayerSettings;
import com.lyntra.lyntraclans.domain.Rank;
import com.lyntra.lyntraclans.util.TimeFormat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class DetalhesSubCommand extends AbstractClanSubCommand {

    public DetalhesSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        OfflinePlayer target = args.length >= 1 ? Bukkit.getOfflinePlayer(args[0]) : player;
        String targetName = target.getName() == null ? (args.length >= 1 ? args[0] : player.getName())
                : target.getName();

        Optional<ClanMember> memberOptional = services.clanManager().getMember(target.getUniqueId());
        Optional<Clan> clanOptional = memberOptional.flatMap(m -> services.clanManager().getClanById(m.getClanId()));
        PlayerSettings settings = services.playerSettingsManager().get(target.getUniqueId());

        String clanDisplay = clanOptional.map(c -> "[" + c.getTag() + "] " + c.getName())
                .orElse(services.languageManager().raw("lista-vazia-generica"));
        String cargoDisplay = "-";
        String confiavelDisplay = services.languageManager().raw("nao");
        if (memberOptional.isPresent() && clanOptional.isPresent()) {
            Rank rank = clanOptional.get().getRankById(memberOptional.get().getRankId());
            cargoDisplay = rank == null ? "-" : rank.getDisplayName();
            confiavelDisplay = services.languageManager().raw(memberOptional.get().isTrusted() ? "sim" : "nao");
        }

        boolean online = target.isOnline();
        String lastSeen = online ? services.languageManager().raw("status-online")
                : (target.getLastLogin() > 0 ? TimeFormat.format(target.getLastLogin()) : "-");

        player.sendMessage(services.languageManager().get("detalhes-cabecalho", "jogador", targetName));
        player.sendMessage(services.languageManager().get("detalhes-clan", "clan", clanDisplay));
        player.sendMessage(services.languageManager().get("detalhes-cargo", "cargo", cargoDisplay));
        player.sendMessage(services.languageManager().get("detalhes-confiavel", "confiavel", confiavelDisplay));
        player.sendMessage(services.languageManager().get("detalhes-ff", "ff", settings.getFfMode().name()));
        player.sendMessage(services.languageManager().get("detalhes-convites", "estado",
                services.languageManager().raw(settings.isAllowInvites() ? "sim" : "nao")));
        player.sendMessage(services.languageManager().get("detalhes-visto", "data", lastSeen));
    }
}
