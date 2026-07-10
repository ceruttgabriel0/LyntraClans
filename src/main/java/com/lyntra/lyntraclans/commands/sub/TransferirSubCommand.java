package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class TransferirSubCommand extends AbstractClanSubCommand {

    public TransferirSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "transferir-uso");
            return;
        }
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
        if (!isLeader(clan, member)) {
            msg(player, "sem-permissao-clan");
            return;
        }

        Optional<Player> targetOptional = resolveOnlineTarget(player, args[0]);
        if (targetOptional.isEmpty()) {
            return;
        }
        Player target = targetOptional.get();
        Optional<ClanMember> targetMemberOptional = services.clanManager().getMember(target.getUniqueId());
        if (targetMemberOptional.isEmpty() || targetMemberOptional.get().getClanId() != clan.getId()) {
            msg(player, "transferir-nao-e-membro", "jogador", target.getName());
            return;
        }

        services.memberManager().transferLeadership(clan, member, targetMemberOptional.get());
        msg(player, "transferir-sucesso", "jogador", target.getName());
        broadcast(clan, target.getName());
    }

    private void broadcast(Clan clan, String newLeaderName) {
        services.clanManager().getMembers(clan.getId()).forEach(m -> {
            Player online = Bukkit.getPlayer(m.getUuid());
            if (online != null) {
                online.sendMessage(services.languageManager().get("transferir-anuncio", "jogador", newLeaderName));
            }
        });
    }
}
