package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.managers.ChatModeManager;
import org.bukkit.entity.Player;

public final class AchatSubCommand extends AbstractClanSubCommand {

    public AchatSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (requireClan(player).isEmpty()) {
            return;
        }
        ChatModeManager.Mode current = services.chatModeManager().getMode(player.getUniqueId());
        if (current == ChatModeManager.Mode.ALIANCA) {
            services.chatModeManager().setMode(player.getUniqueId(), ChatModeManager.Mode.NORMAL);
            msg(player, "achat-desativado");
        } else {
            services.chatModeManager().setMode(player.getUniqueId(), ChatModeManager.Mode.ALIANCA);
            msg(player, "achat-ativado");
        }
    }
}
