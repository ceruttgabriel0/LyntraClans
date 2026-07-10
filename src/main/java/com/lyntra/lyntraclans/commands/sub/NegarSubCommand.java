package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class NegarSubCommand extends AbstractClanSubCommand {

    public NegarSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "negar-uso");
            return;
        }
        Optional<Clan> clanOptional = services.clanManager().getClanByTag(args[0]);
        if (clanOptional.isEmpty()) {
            msg(player, "clan-nao-encontrado", "clan", args[0]);
            return;
        }
        Clan clan = clanOptional.get();
        if (!services.inviteManager().hasInvite(clan.getId(), player.getUniqueId())) {
            msg(player, "negar-nao-encontrado", "tag", clan.getTag());
            return;
        }
        services.inviteManager().removeInvite(clan.getId(), player.getUniqueId());
        msg(player, "negar-sucesso", "tag", clan.getTag());
    }
}
