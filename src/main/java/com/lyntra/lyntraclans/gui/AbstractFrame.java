package com.lyntra.lyntraclans.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractFrame implements InventoryHolder {

    private Inventory inventory;

    public void open(Player player) {
        inventory = createInventory(player);
        populate(player, inventory);
        player.openInventory(inventory);
    }

    protected abstract Inventory createInventory(Player player);

    protected abstract void populate(Player player, Inventory inventory);

    public abstract void handleClick(Player player, int slot);

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
