package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.managers.BankManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public final class BankFrame extends AbstractFrame {

    private static final double[] PRESETS = {100, 1000, 10000};

    private final ClanServices services;
    private final Clan clan;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BankFrame(ClanServices services, Clan clan, Runnable onBack) {
        this.services = services;
        this.clan = clan;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 27, miniMessage.deserialize("<dark_gray>Banco do clã"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        inventory.setItem(4, ItemBuilder.of(Material.GOLD_INGOT)
                .name(Component.text("Saldo: " + services.bankManager().format(clan.getBalance())))
                .build());

        for (int i = 0; i < PRESETS.length; i++) {
            double amount = PRESETS[i];
            inventory.setItem(10 + i, ItemBuilder.of(Material.LIME_DYE)
                    .name(Component.text("Depositar " + services.bankManager().format(amount)))
                    .lore(Component.text("Clique para depositar"))
                    .build());
            inventory.setItem(19 + i, ItemBuilder.of(Material.RED_DYE)
                    .name(Component.text("Sacar " + services.bankManager().format(amount)))
                    .lore(Component.text("Clique para sacar"))
                    .build());
        }

        inventory.setItem(26, ItemBuilder.of(Material.ARROW)
                .name(Component.text("Voltar"))
                .build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        Optional<ClanMember> memberOptional = services.clanManager().getMember(player.getUniqueId());
        if (memberOptional.isEmpty()) {
            player.closeInventory();
            return;
        }
        ClanMember member = memberOptional.get();

        if (slot == 26) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            onBack.run();
            return;
        }

        if (slot >= 10 && slot < 10 + PRESETS.length) {
            deposit(player, member, PRESETS[slot - 10]);
        } else if (slot >= 19 && slot < 19 + PRESETS.length) {
            withdraw(player, member, PRESETS[slot - 19]);
        }
    }

    private void deposit(Player player, ClanMember member, double amount) {
        if (!hasPermission(member, ClanPermission.BANCO_DEPOSITAR)) {
            player.closeInventory();
            player.sendMessage(services.languageManager().get("sem-permissao-clan"));
            return;
        }
        BankManager.Result result = services.bankManager().deposit(player, clan, amount);
        feedback(player, result, "banco-depositar-sucesso", amount);
    }

    private void withdraw(Player player, ClanMember member, double amount) {
        if (!hasPermission(member, ClanPermission.BANCO_SACAR)) {
            player.closeInventory();
            player.sendMessage(services.languageManager().get("sem-permissao-clan"));
            return;
        }
        BankManager.Result result = services.bankManager().withdraw(player, clan, amount);
        feedback(player, result, "banco-sacar-sucesso", amount);
    }

    private void feedback(Player player, BankManager.Result result, String successKey, double amount) {
        switch (result) {
            case OK -> {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                player.sendMessage(services.languageManager().get(successKey, "quantia",
                        services.bankManager().format(amount)));
                open(player);
            }
            case ECONOMY_UNAVAILABLE -> {
                player.closeInventory();
                player.sendMessage(services.languageManager().get("banco-sem-vault"));
            }
            case INVALID_AMOUNT -> {
                player.closeInventory();
                player.sendMessage(services.languageManager().get("banco-quantia-invalida"));
            }
            case INSUFFICIENT_PLAYER_BALANCE -> {
                player.closeInventory();
                player.sendMessage(services.languageManager()
                        .get("banco-sem-saldo-pessoal", "quantia", services.bankManager().format(amount)));
            }
            case INSUFFICIENT_CLAN_BALANCE -> {
                player.closeInventory();
                player.sendMessage(services.languageManager()
                        .get("banco-sem-saldo-clan", "quantia", services.bankManager().format(amount)));
            }
        }
    }

    private boolean hasPermission(ClanMember member, ClanPermission permission) {
        var rank = clan.getRankById(member.getRankId());
        if (rank == null) {
            return false;
        }
        var highest = clan.getHighestRank();
        if (highest != null && highest.getId() == rank.getId()) {
            return true;
        }
        return rank.has(permission);
    }
}
