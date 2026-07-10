package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.managers.ChatModeManager;
import org.bukkit.entity.Player;

public final class ChatSubCommand extends AbstractClanSubCommand {

    public ChatSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (requireClan(player).isEmpty()) {
            return;
        }
        ChatModeManager.Mode current = services.chatModeManager().getMode(player.getUniqueId());
        if (current == ChatModeManager.Mode.CLAN) {
            services.chatModeManager().setMode(player.getUniqueId(), ChatModeManager.Mode.NORMAL);
            msg(player, "chat-desativado");
        } else {
            services.chatModeManager().setMode(player.getUniqueId(), ChatModeManager.Mode.CLAN);
            msg(player, "chat-ativado");
        }
    }
}
