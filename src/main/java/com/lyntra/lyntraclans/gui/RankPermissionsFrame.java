package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.domain.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/** Editor de permissões de um cargo: cada permissão do catálogo é um item, clicar liga/desliga. */
public final class RankPermissionsFrame extends AbstractFrame {

    private final ClanServices services;
    private final Rank rank;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final ClanPermission[] PERMISSIONS = ClanPermission.values();

    public RankPermissionsFrame(ClanServices services, Rank rank, Runnable onBack) {
        this.services = services;
        this.rank = rank;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 45,
                miniMessage.deserialize("<dark_gray>Permissões - " + rank.getDisplayName()));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        for (int i = 0; i < PERMISSIONS.length; i++) {
            ClanPermission permission = PERMISSIONS[i];
            boolean granted = rank.has(permission);
            inventory.setItem(i, ItemBuilder.of(granted ? Material.LIME_DYE : Material.GRAY_DYE)
                    .name(Component.text(permission.name()))
                    .lore(Component.text(granted ? "Concedida - clique pra remover" : "Não concedida - clique pra conceder"))
                    .build());
        }
        inventory.setItem(44, ItemBuilder.of(Material.ARROW).name(Component.text("Voltar")).build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        if (slot == 44) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            onBack.run();
            return;
        }
        if (slot < 0 || slot >= PERMISSIONS.length) {
            return;
        }
        ClanPermission permission = PERMISSIONS[slot];
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        if (rank.has(permission)) {
            services.rankManager().revokePermission(rank, permission);
            player.sendMessage(services.languageManager().get("cargo-permissao-removida",
                    "permissao", permission.name(), "cargo", rank.getName()));
        } else {
            services.rankManager().grantPermission(rank, permission);
            player.sendMessage(services.languageManager().get("cargo-permissao-concedida",
                    "permissao", permission.name(), "cargo", rank.getName()));
        }
        open(player);
    }
}
