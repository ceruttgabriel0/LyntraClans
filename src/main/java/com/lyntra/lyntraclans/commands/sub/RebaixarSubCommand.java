package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.Rank;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public final class RebaixarSubCommand extends AbstractClanSubCommand {

    public RebaixarSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "rebaixar-uso");
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
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        Optional<ClanMember> targetMemberOptional = services.clanManager().getMember(target.getUniqueId());
        if (targetMemberOptional.isEmpty() || targetMemberOptional.get().getClanId() != clan.getId()
                || !isLeader(clan, targetMemberOptional.get())) {
            msg(player, "rebaixar-nao-e-lider", "jogador", args[0]);
            return;
        }
        List<ClanMember> leaders = services.clanManager().getMembers(clan.getId()).stream()
                .filter(m -> isLeader(clan, m)).toList();
        if (leaders.size() <= 1) {
            msg(player, "rebaixar-unico-lider");
            return;
        }
        Rank defaultRank = clan.getDefaultRank();
        if (defaultRank == null) {
            msg(player, "erro-interno");
            return;
        }
        services.memberManager().setRank(targetMemberOptional.get(), defaultRank);
        msg(player, "rebaixar-sucesso", "jogador", args[0]);
    }
}
