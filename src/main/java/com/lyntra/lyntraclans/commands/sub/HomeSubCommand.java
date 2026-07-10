package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class HomeSubCommand extends AbstractClanSubCommand {

    public HomeSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        if (!clan.hasHome()) {
            msg(player, "home-sem-home");
            return;
        }
        World world = Bukkit.getWorld(clan.getHomeWorld());
        if (world == null) {
            msg(player, "home-sem-home");
            return;
        }
        Location location = new Location(world, clan.getHomeX(), clan.getHomeY(), clan.getHomeZ(),
                clan.getHomeYaw(), clan.getHomePitch());
        player.teleport(location);
        msg(player, "home-teleportado");
    }
}
