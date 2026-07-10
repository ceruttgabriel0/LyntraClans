package com.lyntra.lyntraclans.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.view.AnvilView;

public final class AnvilInputListener implements Listener {

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof AnvilTextCapture capture) {
            AnvilView view = event.getView();
            view.setRepairCost(0);
            capture.captureRenameText(view.getRenameText());
        }
    }
}
