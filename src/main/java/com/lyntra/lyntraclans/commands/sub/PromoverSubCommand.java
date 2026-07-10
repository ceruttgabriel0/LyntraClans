package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class PromoverSubCommand extends AbstractClanSubCommand {

    public PromoverSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "promover-uso");
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
        // "promover" so eleva o alvo pro cargo mais alto, sem rebaixar quem promoveu - o cla
        // suporta multiplos lideres simultaneos (ver CLANS.md). "transferir" e que entrega a
        // lideranca de fato, rebaixando quem tinha.
        services.memberManager().setRank(targetMemberOptional.get(), clan.getHighestRank());
        msg(player, "promover-sucesso", "jogador", target.getName());
    }
}
