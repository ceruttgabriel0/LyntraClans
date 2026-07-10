package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import org.bukkit.entity.Player;

public final class AjudaSubCommand extends AbstractClanSubCommand {

    public AjudaSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        boolean hasClan = services.clanManager().getClanOfPlayer(player.getUniqueId()).isPresent();
        String key = hasClan ? "ajuda-com-clan" : "ajuda-sem-clan";
        services.languageManager().getList(key).forEach(player::sendMessage);
    }
}
