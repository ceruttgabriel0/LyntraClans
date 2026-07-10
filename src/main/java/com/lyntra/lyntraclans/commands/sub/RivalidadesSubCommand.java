package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.RelationType;
import com.lyntra.lyntraclans.managers.RelationManager;
import org.bukkit.entity.Player;

import java.util.List;

public final class RivalidadesSubCommand extends AbstractClanSubCommand {

    public RivalidadesSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        List<RelationManager.RelationPair> pairs = services.relationManager()
                .getAllServerWide(RelationType.RIVAL, services.clanManager());
        if (pairs.isEmpty()) {
            msg(player, "rivalidades-vazio");
            return;
        }
        msg(player, "rivalidades-cabecalho");
        for (RelationManager.RelationPair pair : pairs) {
            player.sendMessage(services.languageManager().get("rivalidades-item",
                    "tag1", pair.clanA().getTag(), "tag2", pair.clanB().getTag()));
        }
    }
}
