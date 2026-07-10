package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.domain.Rank;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public final class ExpulsarSubCommand extends AbstractClanSubCommand {

    public ExpulsarSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "expulsar-uso");
            return;
        }
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
        if (!requirePermission(player, clan, member, ClanPermission.EXPULSAR)) {
            return;
        }

        if (args[0].equalsIgnoreCase(player.getName())) {
            msg(player, "expulsar-a-si-mesmo");
            return;
        }

        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(args[0]);
        UUID targetUuid = targetOffline.getUniqueId();
        Optional<ClanMember> targetMemberOptional = services.clanManager().getMember(targetUuid);
        if (targetMemberOptional.isEmpty() || targetMemberOptional.get().getClanId() != clan.getId()) {
            msg(player, "expulsar-nao-e-membro", "jogador", args[0]);
            return;
        }
        ClanMember targetMember = targetMemberOptional.get();

        Rank actorRank = rankOf(clan, member);
        Rank targetRank = rankOf(clan, targetMember);
        if (targetRank != null && actorRank != null && targetRank.getPriority() >= actorRank.getPriority()) {
            msg(player, "expulsar-cargo-maior-igual");
            return;
        }

        services.memberManager().kick(clan, targetMember);
        msg(player, "expulsar-sucesso", "jogador", args[0]);
        broadcast(clan, args[0]);

        Player targetOnline = Bukkit.getPlayer(targetUuid);
        if (targetOnline != null) {
            targetOnline.sendMessage(services.languageManager().get("expulso-notificacao", "tag", clan.getTag()));
        }
    }

    private void broadcast(Clan clan, String kickedName) {
        services.clanManager().getMembers(clan.getId()).forEach(member -> {
            Player online = Bukkit.getPlayer(member.getUuid());
            if (online != null) {
                online.sendMessage(services.languageManager().get("expulsar-anuncio", "jogador", kickedName));
            }
        });
    }
}
