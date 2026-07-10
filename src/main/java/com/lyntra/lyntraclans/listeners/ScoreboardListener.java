package com.lyntra.lyntraclans.listeners;

import com.lyntra.lyntraclans.managers.ScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/** Atualiza a cor de nome/tablist na hora quando um jogador entra ou sai (o resto e coberto pelo refresh periodico). */
public final class ScoreboardListener implements Listener {

    private final JavaPlugin plugin;
    private final ScoreboardManager scoreboardManager;

    public ScoreboardListener(JavaPlugin plugin, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        scoreboardManager.refreshAll();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, scoreboardManager::refreshAll);
    }
}
