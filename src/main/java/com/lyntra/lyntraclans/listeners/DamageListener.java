package com.lyntra.lyntraclans.listeners;

import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.managers.ClanManager;
import com.lyntra.lyntraclans.managers.KillManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Optional;

public final class DamageListener implements Listener {

    private final ClanManager clanManager;
    private final KillManager killManager;

    public DamageListener(ClanManager clanManager, KillManager killManager) {
        this.clanManager = clanManager;
        this.killManager = killManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player attacker = resolveAttacker(event);
        if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }
        Optional<Clan> attackerClan = clanManager.getClanOfPlayer(attacker.getUniqueId());
        Optional<Clan> victimClan = clanManager.getClanOfPlayer(victim.getUniqueId());
        if (killManager.shouldBlockDamage(attackerClan, victimClan, victim.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private Player resolveAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }
        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) {
                return player;
            }
        }
        return null;
    }
}
