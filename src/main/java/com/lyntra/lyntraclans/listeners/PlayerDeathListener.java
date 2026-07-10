package com.lyntra.lyntraclans.listeners;

import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.KillCategory;
import com.lyntra.lyntraclans.managers.ClanManager;
import com.lyntra.lyntraclans.managers.KillManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Optional;

public final class PlayerDeathListener implements Listener {

    private final ClanManager clanManager;
    private final KillManager killManager;

    public PlayerDeathListener(ClanManager clanManager, KillManager killManager) {
        this.clanManager = clanManager;
        this.killManager = killManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        Optional<ClanMember> victimMember = clanManager.getMember(victim.getUniqueId());
        if (killer == null) {
            victimMember.ifPresent(member -> killManager.registerKill(null, member, KillCategory.CIVIL));
            return;
        }
        if (killer.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }

        Optional<Clan> killerClan = clanManager.getClanOfPlayer(killer.getUniqueId());
        Optional<Clan> victimClan = clanManager.getClanOfPlayer(victim.getUniqueId());
        KillCategory category = killManager.categorize(killerClan.orElse(null), victimClan.orElse(null));

        ClanMember killerMember = clanManager.getMember(killer.getUniqueId()).orElse(null);
        ClanMember member = victimMember.orElse(null);
        killManager.registerKill(killerMember, member, category);
    }
}
