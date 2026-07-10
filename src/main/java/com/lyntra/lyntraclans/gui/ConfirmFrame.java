package com.lyntra.lyntraclans.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/** Confirmação genérica sim/não pra ações destrutivas na GUI (desfazer, sair, etc). */
public final class ConfirmFrame extends AbstractFrame {

    private final String question;
    private final Runnable onConfirm;
    private final Runnable onCancel;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ConfirmFrame(String question, Runnable onConfirm, Runnable onCancel) {
        this.question = question;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 27, miniMessage.deserialize("<dark_gray>Tem certeza?"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        inventory.setItem(4, ItemBuilder.of(Material.PAPER)
                .name(Component.text(question))
                .build());
        inventory.setItem(11, ItemBuilder.of(Material.LIME_CONCRETE)
                .name(Component.text("Confirmar"))
                .build());
        inventory.setItem(15, ItemBuilder.of(Material.RED_CONCRETE)
                .name(Component.text("Cancelar"))
                .build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        if (slot == 11) {
            onConfirm.run();
        } else if (slot == 15) {
            onCancel.run();
        }
    }
}
