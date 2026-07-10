package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class ConfiarSubCommand extends AbstractClanSubCommand {

    private final boolean trust;

    public ConfiarSubCommand(ClanServices services, boolean trust) {
        super(services);
        this.trust = trust;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, trust ? "confiar-uso" : "naoconfiar-uso");
            return;
        }
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
        if (!requirePermission(player, clan, member, ClanPermission.GERENCIAR_CONFIANCA)) {
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        Optional<ClanMember> targetMemberOptional = services.clanManager().getMember(target.getUniqueId());
        if (targetMemberOptional.isEmpty() || targetMemberOptional.get().getClanId() != clan.getId()) {
            msg(player, "expulsar-nao-e-membro", "jogador", args[0]);
            return;
        }
        targetMemberOptional.get().setTrusted(trust);
        services.clanManager().persistMember(targetMemberOptional.get());
        msg(player, trust ? "confiar-sucesso" : "naoconfiar-sucesso", "jogador", args[0]);
    }
}
