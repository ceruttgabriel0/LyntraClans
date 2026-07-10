package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class ConvidarSubCommand extends AbstractClanSubCommand {

    public ConvidarSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "convidar-uso");
            return;
        }
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
        if (!requirePermission(player, clan, member, ClanPermission.CONVIDAR)) {
            return;
        }

        Optional<Player> targetOptional = resolveOnlineTarget(player, args[0]);
        if (targetOptional.isEmpty()) {
            return;
        }
        Player target = targetOptional.get();

        if (services.clanManager().getClanOfPlayer(target.getUniqueId()).isPresent()) {
            msg(player, "jogador-ja-tem-clan", "jogador", target.getName());
            return;
        }
        if (!services.playerSettingsManager().get(target.getUniqueId()).isAllowInvites()) {
            msg(player, "convidar-bloqueado", "jogador", target.getName());
            return;
        }
        int currentSize = services.clanManager().getMembers(clan.getId()).size();
        if (currentSize >= clan.getMaxMembers()) {
            msg(player, "convidar-clan-cheio", "atual", String.valueOf(currentSize),
                    "max", String.valueOf(clan.getMaxMembers()));
            return;
        }
        if (services.inviteManager().hasInvite(clan.getId(), target.getUniqueId())) {
            msg(player, "convidar-ja-convidado", "jogador", target.getName());
            return;
        }

        services.inviteManager().invite(clan.getId(), target.getUniqueId());
        msg(player, "convidar-sucesso", "jogador", target.getName());
        target.sendMessage(services.languageManager().get("convidar-recebido", "jogador", player.getName(),
                "tag", clan.getTag(), "nome", clan.getName()));
    }
}
