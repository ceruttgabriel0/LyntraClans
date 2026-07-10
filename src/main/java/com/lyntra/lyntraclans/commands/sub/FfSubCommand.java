package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.FfMode;
import com.lyntra.lyntraclans.domain.PlayerSettings;
import org.bukkit.entity.Player;

public final class FfSubCommand extends AbstractClanSubCommand {

    public FfSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "ff-uso");
            return;
        }
        FfMode mode;
        try {
            mode = FfMode.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            usage(player, "ff-uso");
            return;
        }
        PlayerSettings settings = services.playerSettingsManager().get(player.getUniqueId());
        settings.setFfMode(mode);
        services.playerSettingsManager().save(player.getUniqueId(), settings);
        msg(player, "ff-sucesso", "modo", mode.name());
    }
}
