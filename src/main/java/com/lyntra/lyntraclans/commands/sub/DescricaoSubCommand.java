package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.util.ProfanityFilter;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class DescricaoSubCommand extends AbstractClanSubCommand {

    public DescricaoSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "descricao-uso");
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
        String description = String.join(" ", args);
        if (description.length() > 100) {
            description = description.substring(0, 100);
        }
        if (ProfanityFilter.containsBannedWord(description, services.configManager().bannedWords())) {
            msg(player, "moderacao-palavra-banida");
            return;
        }
        clan.setDescription(description);
        services.clanManager().persistClan(clan);
        msg(player, "descricao-sucesso");
    }
}
