package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class MortesSubCommand extends AbstractClanSubCommand {

    public MortesSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        OfflinePlayer target = args.length >= 1 ? Bukkit.getOfflinePlayer(args[0]) : player;
        String targetName = args.length >= 1 ? args[0] : player.getName();
        Optional<ClanMember> memberOptional = services.clanManager().getMember(target.getUniqueId());
        int deaths = memberOptional.map(ClanMember::getDeaths).orElse(0);
        msg(player, "mortes-info", "jogador", targetName, "mortes", String.valueOf(deaths));
    }
}
