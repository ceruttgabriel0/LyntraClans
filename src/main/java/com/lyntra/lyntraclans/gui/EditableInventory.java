package com.lyntra.lyntraclans.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * Marca um InventoryHolder como um inventario de verdade (o jogador pode guardar/tirar item),
 * diferente das telas de menu do AbstractFrame (que sao so botoes, todo clique e cancelado).
 * FrameListener nao cancela cliques nesses inventarios; o conteudo e persistido no fechamento.
 */
public interface EditableInventory {
    void onClose(Player player, Inventory inventory);
}
