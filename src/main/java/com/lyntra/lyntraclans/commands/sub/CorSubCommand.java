package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.util.ClanColors;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class CorSubCommand extends AbstractClanSubCommand {

    public CorSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "cor-uso");
            return;
        }
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
        if (!requirePermission(player, clan, member, ClanPermission.ALTERAR_DESCRICAO)) {
            return;
        }
        String color = ClanColors.normalize(args[0]);
        if (!ClanColors.isValid(color)) {
            msg(player, "cor-invalida", "cores", String.join(", ", ClanColors.VALID_COLORS));
            return;
        }
        clan.setColor(color);
        services.clanManager().persistClan(clan);
        msg(player, "cor-sucesso", "cor", color);
    }
}
