package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class ClanffSubCommand extends AbstractClanSubCommand {

    public ClanffSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1 || (!args[0].equalsIgnoreCase("permitir") && !args[0].equalsIgnoreCase("bloquear"))) {
            usage(player, "clanff-uso");
            return;
        }
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
        if (!requirePermission(player, clan, member, ClanPermission.GERENCIAR_FF_CLA)) {
            return;
        }
        boolean permitir = args[0].equalsIgnoreCase("permitir");
        clan.setFriendlyFire(permitir);
        services.clanManager().persistClan(clan);
        msg(player, permitir ? "clanff-permitido" : "clanff-bloqueado");
    }
}
