package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.domain.Rank;
import com.lyntra.lyntraclans.managers.UpgradeManager;
import com.lyntra.lyntraclans.util.ClanColors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Configurações do clã: cor, descrição, tag, fogo amigo do clã, upgrades pagos, desfazer. */
public final class ClanSettingsFrame extends AbstractFrame {

    private final ClanServices services;
    private final Clan clan;
    private final Logger logger;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ClanSettingsFrame(ClanServices services, Clan clan, Logger logger, Runnable onBack) {
        this.services = services;
        this.clan = clan;
        this.logger = logger;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 36, miniMessage.deserialize("<dark_gray>Configurações - [" + clan.getTag() + "]"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        inventory.setItem(10, ItemBuilder.clanBanner(clan.getColor())
                .name(Component.text("Cor: " + clan.getColor()))
                .lore(Component.text("Clique pra trocar"))
                .build());
        inventory.setItem(11, ItemBuilder.of(Material.WRITABLE_BOOK)
                .name(Component.text("Descrição"))
                .lore(Component.text(clan.getDescription().isBlank() ? "(sem descrição)" : clan.getDescription()))
                .lore(Component.text("Clique pra editar"))
                .build());
        inventory.setItem(12, ItemBuilder.of(Material.NAME_TAG)
                .name(Component.text("Tag: " + clan.getTag()))
                .lore(Component.text("Clique pra editar"))
                .build());
        inventory.setItem(14, ItemBuilder.of(clan.isFriendlyFire() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(Component.text("Fogo amigo do clã: " + (clan.isFriendlyFire() ? "Permitido" : "Bloqueado")))
                .lore(Component.text("Clique pra alternar"))
                .build());
        inventory.setItem(15, ItemBuilder.of(Material.IRON_BARS)
                .name(Component.text("Upgrade: limite de membros"))
                .lore(Component.text("Atual: " + clan.getMaxMembers()))
                .lore(Component.text("Custo: " + services.bankManager().format(services.configManager().memberSlotPrice())))
                .build());
        inventory.setItem(16, ItemBuilder.of(Material.CHEST)
                .name(Component.text("Upgrade: tamanho do baú"))
                .lore(Component.text("Atual: " + clan.getChestSize() + " slots"))
                .lore(Component.text("Custo: " + services.bankManager().format(services.configManager().chestSlotPrice())))
                .build());
        inventory.setItem(22, ItemBuilder.of(Material.TNT)
                .name(Component.text("Desfazer o clã"))
                .lore(Component.text("Ação irreversível"))
                .build());
        inventory.setItem(31, ItemBuilder.of(Material.ARROW).name(Component.text("Voltar")).build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        Optional<ClanMember> viewerMember = services.clanManager().getMember(player.getUniqueId());
        if (viewerMember.isEmpty()) {
            return;
        }
        ClanMember viewer = viewerMember.get();

        switch (slot) {
            case 10 -> {
                if (!hasPermission(viewer, ClanPermission.ALTERAR_DESCRICAO)) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                int currentIndex = ClanColors.VALID_COLORS.indexOf(clan.getColor().toLowerCase());
                String nextColor = ClanColors.VALID_COLORS.get((currentIndex + 1) % ClanColors.VALID_COLORS.size());
                clan.setColor(nextColor);
                services.clanManager().persistClan(clan);
                player.sendMessage(services.languageManager().get("cor-sucesso", "cor", nextColor));
                open(player);
            }
            case 11 -> {
                if (!hasPermission(viewer, ClanPermission.ALTERAR_DESCRICAO)) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new AnvilTextInputFrame(services, "Nova descrição", "Digite a descrição", text -> {
                    String description = text.length() > 100 ? text.substring(0, 100) : text;
                    clan.setDescription(description);
                    services.clanManager().persistClan(clan);
                    player.closeInventory();
                    player.sendMessage(services.languageManager().get("descricao-sucesso"));
                }).open(player);
            }
            case 12 -> {
                if (!hasPermission(viewer, ClanPermission.ALTERAR_TAG)) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new AnvilTextInputFrame(services, "Nova tag", "Digite a tag", text -> {
                    int min = services.configManager().tagMinLength();
                    int max = services.configManager().tagMaxLength();
                    if (text.length() < min || text.length() > max) {
                        player.sendMessage(services.languageManager().get("criar-tag-tamanho",
                                "min", String.valueOf(min), "max", String.valueOf(max)));
                        return;
                    }
                    if (!text.matches("[a-zA-Z0-9]+")) {
                        player.sendMessage(services.languageManager().get("criar-tag-caracteres"));
                        return;
                    }
                    if (services.clanManager().tagInUse(text)) {
                        player.sendMessage(services.languageManager().get("criar-tag-em-uso", "tag", text));
                        return;
                    }
                    String oldTag = clan.getTag();
                    clan.setTag(text);
                    services.clanManager().persistClan(clan);
                    services.clanManager().renameTagIndex(oldTag, text, clan.getId());
                    player.closeInventory();
                    player.sendMessage(services.languageManager().get("mudartag-sucesso", "tag", text));
                }).open(player);
            }
            case 14 -> {
                if (!hasPermission(viewer, ClanPermission.GERENCIAR_FF_CLA)) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                clan.setFriendlyFire(!clan.isFriendlyFire());
                services.clanManager().persistClan(clan);
                player.sendMessage(services.languageManager().get(
                        clan.isFriendlyFire() ? "clanff-permitido" : "clanff-bloqueado"));
                open(player);
            }
            case 15 -> {
                if (!hasPermission(viewer, ClanPermission.UPGRADES)) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                UpgradeManager.Result result = services.upgradeManager().upgradeMemberSlot(clan);
                feedbackUpgrade(player, result, "upgrades-membros-sucesso", "upgrades-membros-teto",
                        services.configManager().memberSlotPrice(), String.valueOf(clan.getMaxMembers()),
                        String.valueOf(services.configManager().absoluteMaxMembers()));
                open(player);
            }
            case 16 -> {
                if (!hasPermission(viewer, ClanPermission.UPGRADES)) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                UpgradeManager.Result result = services.upgradeManager().upgradeChest(clan);
                feedbackUpgrade(player, result, "upgrades-bau-sucesso", "upgrades-bau-teto",
                        services.configManager().chestSlotPrice(), String.valueOf(clan.getChestSize()),
                        String.valueOf(services.configManager().chestSlotMax()));
                open(player);
            }
            case 22 -> {
                Rank highest = clan.getHighestRank();
                boolean isLeader = highest != null && highest.getId() == viewer.getRankId();
                if (!isLeader) {
                    player.sendMessage(services.languageManager().get("desfazer-somente-lider"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new ConfirmFrame("Desfazer o clã [" + clan.getTag() + "]? Ação irreversível!", () -> {
                    try {
                        String tag = clan.getTag();
                        services.clanManager().disbandClan(clan);
                        services.inviteManager().removeAllInvitesForClan(clan.getId());
                        services.relationManager().removeAllRelations(clan.getId());
                        services.warManager().removeAllWars(clan.getId());
                        services.noticeManager().removeAllForClan(clan.getId());
                        player.closeInventory();
                        player.sendMessage(services.languageManager().get("desfazer-sucesso", "tag", tag));
                    } catch (SQLException e) {
                        logger.log(Level.SEVERE, "Falha ao desfazer cla pela GUI", e);
                        player.sendMessage(services.languageManager().get("erro-interno"));
                    }
                }, () -> open(player)).open(player);
            }
            case 31 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                onBack.run();
            }
            default -> {
            }
        }
    }

    private void feedbackUpgrade(Player player, UpgradeManager.Result result, String successKey, String failKey,
                                  double price, String newValue, String maxValue) {
        switch (result) {
            case OK -> player.sendMessage(services.languageManager().get(successKey, "novo", newValue,
                    "custo", services.bankManager().format(price)));
            case MAX_REACHED -> player.sendMessage(services.languageManager().get(failKey, "max", maxValue));
            case INSUFFICIENT_CLAN_BALANCE -> player.sendMessage(services.languageManager().get(
                    "banco-sem-saldo-clan", "quantia", services.bankManager().format(price)));
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
