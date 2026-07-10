package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class GuerraSubCommand extends AbstractClanSubCommand {

    public GuerraSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            usage(player, "guerra-uso");
            return;
        }
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
        if (!requirePermission(player, clan, member, ClanPermission.GERENCIAR_GUERRA)) {
            return;
        }
        Optional<Clan> targetOptional = services.clanManager().getClanByTagOrName(args[1]);
        if (targetOptional.isEmpty()) {
            msg(player, "clan-nao-encontrado", "clan", args[1]);
            return;
        }
        Clan target = targetOptional.get();
        if (target.getId() == clan.getId()) {
            msg(player, "guerra-mesmo-clan");
            return;
        }

        if (args[0].equalsIgnoreCase("iniciar")) {
            if (services.relationManager().isAlly(clan.getId(), target.getId())) {
                msg(player, "guerra-aliado-bloqueado", "tag", target.getTag());
                return;
            }
            if (!services.warManager().startWar(clan, target)) {
                msg(player, "guerra-ja-em-guerra", "tag", target.getTag());
                return;
            }
            notifyClan(clan, "guerra-iniciada-anuncio", "tag", target.getTag());
            notifyClan(target, "guerra-iniciada-anuncio", "tag", clan.getTag());
        } else if (args[0].equalsIgnoreCase("finalizar")) {
            if (!services.warManager().endWar(clan, target)) {
                msg(player, "guerra-nao-em-guerra", "tag", target.getTag());
                return;
            }
            notifyClan(clan, "guerra-finalizada-anuncio", "tag", target.getTag());
            notifyClan(target, "guerra-finalizada-anuncio", "tag", clan.getTag());
        } else {
            usage(player, "guerra-uso");
        }
    }

    private void notifyClan(Clan clan, String key, String... pairs) {
        services.clanManager().getMembers(clan.getId()).forEach(m -> {
            Player online = Bukkit.getPlayer(m.getUuid());
            if (online != null) {
                online.sendMessage(services.languageManager().get(key, pairs));
            }
        });
    }
}
