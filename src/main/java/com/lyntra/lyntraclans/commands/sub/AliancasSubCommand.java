package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.RelationType;
import com.lyntra.lyntraclans.managers.RelationManager;
import org.bukkit.entity.Player;

import java.util.List;

public final class AliancasSubCommand extends AbstractClanSubCommand {

    public AliancasSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        List<RelationManager.RelationPair> pairs = services.relationManager()
                .getAllServerWide(RelationType.ALLY, services.clanManager());
        if (pairs.isEmpty()) {
            msg(player, "aliancas-vazio");
            return;
        }
        msg(player, "aliancas-cabecalho");
        for (RelationManager.RelationPair pair : pairs) {
            player.sendMessage(services.languageManager().get("aliancas-item",
                    "tag1", pair.clanA().getTag(), "tag2", pair.clanB().getTag()));
        }
    }
}
