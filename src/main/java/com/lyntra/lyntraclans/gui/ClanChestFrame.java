package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Bau do cla de verdade: inventario editavel (guardar/tirar item), persistido em
 * {@code clans.chest_contents} (ver ClanChestSerializer). Tamanho segue clan.getChestSize(),
 * que cresce por upgrade pago - crescer preserva os itens que ja estavam guardados.
 */
public final class ClanChestFrame implements InventoryHolder, EditableInventory {

    private final ClanServices services;
    private final Clan clan;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private Inventory inventory;

    public ClanChestFrame(ClanServices services, Clan clan) {
        this.services = services;
        this.clan = clan;
    }

    public void open(Player player) {
        int size = roundedSize(clan.getChestSize());
        inventory = Bukkit.createInventory(this, size,
                miniMessage.deserialize("<dark_gray>Baú - [" + clan.getTag() + "]"));
        ItemStack[] contents = ClanChestSerializer.deserialize(clan.getChestContents(), size);
        inventory.setContents(contents);
        player.openInventory(inventory);
    }

    private int roundedSize(int chestSize) {
        int rounded = ((chestSize + 8) / 9) * 9;
        return Math.max(9, Math.min(54, rounded));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClose(Player player, Inventory closedInventory) {
        String serialized = ClanChestSerializer.serialize(closedInventory.getContents());
        clan.setChestContents(serialized);
        services.clanManager().persistClan(clan);
    }
}
