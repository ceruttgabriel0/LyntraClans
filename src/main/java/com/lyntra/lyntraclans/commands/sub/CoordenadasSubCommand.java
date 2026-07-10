package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class CoordenadasSubCommand extends AbstractClanSubCommand {

    public CoordenadasSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        msg(player, "coordenadas-cabecalho");
        boolean any = false;
        for (ClanMember member : services.clanManager().getMembers(clan.getId())) {
            Player online = Bukkit.getPlayer(member.getUuid());
            if (online == null) {
                continue;
            }
            any = true;
            Location location = online.getLocation();
            player.sendMessage(services.languageManager().get("coordenadas-item",
                    "jogador", online.getName(), "mundo", location.getWorld().getName(),
                    "x", String.valueOf(location.getBlockX()), "y", String.valueOf(location.getBlockY()),
                    "z", String.valueOf(location.getBlockZ())));
        }
        if (!any) {
            msg(player, "coordenadas-vazio");
        }
    }
}
