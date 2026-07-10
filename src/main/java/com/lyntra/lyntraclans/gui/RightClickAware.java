package com.lyntra.lyntraclans.gui;

import org.bukkit.entity.Player;

/** Implementado por frames que distinguem clique esquerdo de direito num mesmo slot. */
public interface RightClickAware {
    void handleRightClick(Player player, int slot);
}
