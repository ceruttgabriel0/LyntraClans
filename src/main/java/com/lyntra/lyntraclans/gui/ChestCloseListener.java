package com.lyntra.lyntraclans.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public final class ChestCloseListener implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof EditableInventory editable && event.getPlayer() instanceof Player player) {
            editable.onClose(player, event.getInventory());
        }
    }
}
