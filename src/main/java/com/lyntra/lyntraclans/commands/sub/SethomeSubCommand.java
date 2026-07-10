package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class SethomeSubCommand extends AbstractClanSubCommand {

    public SethomeSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
        if (!requirePermission(player, clan, member, ClanPermission.ALTERAR_HOME)) {
            return;
        }
        Location location = player.getLocation();
        clan.setHome(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
        services.clanManager().persistClan(clan);
        msg(player, "sethome-sucesso");
    }
}
