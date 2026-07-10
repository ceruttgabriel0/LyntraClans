package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class MudarTagSubCommand extends AbstractClanSubCommand {

    public MudarTagSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "mudartag-uso");
            return;
        }
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
        if (!requirePermission(player, clan, member, ClanPermission.ALTERAR_TAG)) {
            return;
        }
        String newTag = args[0];
        int min = services.configManager().tagMinLength();
        int max = services.configManager().tagMaxLength();
        if (newTag.length() < min || newTag.length() > max) {
            msg(player, "criar-tag-tamanho", "min", String.valueOf(min), "max", String.valueOf(max));
            return;
        }
        if (!newTag.matches("[a-zA-Z0-9]+")) {
            msg(player, "criar-tag-caracteres");
            return;
        }
        if (services.clanManager().tagInUse(newTag)) {
            msg(player, "criar-tag-em-uso", "tag", newTag);
            return;
        }
        String oldTag = clan.getTag();
        clan.setTag(newTag);
        services.clanManager().persistClan(clan);
        services.clanManager().renameTagIndex(oldTag, newTag, clan.getId());
        msg(player, "mudartag-sucesso", "tag", newTag);
    }
}
