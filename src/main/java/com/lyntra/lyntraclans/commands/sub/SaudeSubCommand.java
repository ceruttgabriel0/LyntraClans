package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SaudeSubCommand extends AbstractClanSubCommand {

    public SaudeSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        msg(player, "saude-cabecalho");
        List<Clan> clans = new ArrayList<>();
        clans.add(clan);
        clans.addAll(services.relationManager().getAllies(clan, services.clanManager()));

        boolean any = false;
        for (Clan target : clans) {
            for (ClanMember member : services.clanManager().getMembers(target.getId())) {
                Player online = Bukkit.getPlayer(member.getUuid());
                if (online == null) {
                    continue;
                }
                any = true;
                double health = online.getHealth();
                double max = online.getAttribute(Attribute.MAX_HEALTH) == null ? 20
                        : online.getAttribute(Attribute.MAX_HEALTH).getValue();
                player.sendMessage(services.languageManager().get("saude-item",
                        "jogador", online.getName(), "tag", target.getTag(),
                        "vida", String.format("%.1f", health), "vidamax", String.format("%.1f", max)));
            }
        }
        if (!any) {
            msg(player, "saude-vazio");
        }
    }
}
