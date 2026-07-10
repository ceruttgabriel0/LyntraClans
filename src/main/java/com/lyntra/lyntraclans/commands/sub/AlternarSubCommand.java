package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.PlayerSettings;
import org.bukkit.entity.Player;

public final class AlternarSubCommand extends AbstractClanSubCommand {

    public AlternarSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "alternar-uso");
            return;
        }
        PlayerSettings settings = services.playerSettingsManager().get(player.getUniqueId());
        switch (args[0].toLowerCase()) {
            case "convidar" -> {
                settings.setAllowInvites(!settings.isAllowInvites());
                services.playerSettingsManager().save(player.getUniqueId(), settings);
                msg(player, settings.isAllowInvites() ? "alternar-convidar-ligado" : "alternar-convidar-desligado");
            }
            case "avisos" -> {
                settings.setShowWarnings(!settings.isShowWarnings());
                services.playerSettingsManager().save(player.getUniqueId(), settings);
                msg(player, settings.isShowWarnings() ? "alternar-avisos-ligado" : "alternar-avisos-desligado");
            }
            case "tag" -> {
                settings.setShowTag(!settings.isShowTag());
                services.playerSettingsManager().save(player.getUniqueId(), settings);
                msg(player, settings.isShowTag() ? "alternar-tag-ligado" : "alternar-tag-desligado");
            }
            default -> usage(player, "alternar-uso");
        }
    }
}
