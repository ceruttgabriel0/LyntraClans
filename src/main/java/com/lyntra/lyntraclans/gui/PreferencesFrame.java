package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.FfMode;
import com.lyntra.lyntraclans.domain.PlayerSettings;
import com.lyntra.lyntraclans.managers.ChatModeManager;
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

/** Preferências pessoais: toggles, fogo amigo pessoal, modo de chat, sair do clã. */
public final class PreferencesFrame extends AbstractFrame {

    private final ClanServices services;
    private final Clan clan;
    private final Logger logger;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PreferencesFrame(ClanServices services, Clan clan, Logger logger, Runnable onBack) {
        this.services = services;
        this.clan = clan;
        this.logger = logger;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 36, miniMessage.deserialize("<dark_gray>Minhas Preferências"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        PlayerSettings settings = services.playerSettingsManager().get(player.getUniqueId());

        inventory.setItem(10, ItemBuilder.of(settings.isAllowInvites() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(Component.text("Aceitar convites: " + (settings.isAllowInvites() ? "Sim" : "Não")))
                .lore(Component.text("Clique pra alternar"))
                .build());
        inventory.setItem(11, ItemBuilder.of(settings.isShowWarnings() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(Component.text("Mostrar avisos ao entrar: " + (settings.isShowWarnings() ? "Sim" : "Não")))
                .lore(Component.text("Clique pra alternar"))
                .build());
        inventory.setItem(12, ItemBuilder.of(settings.isShowTag() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(Component.text("Mostrar tag no nome: " + (settings.isShowTag() ? "Sim" : "Não")))
                .lore(Component.text("Clique pra alternar"))
                .build());
        inventory.setItem(14, ItemBuilder.of(Material.SHIELD)
                .name(Component.text("Fogo amigo pessoal: " + settings.getFfMode().name()))
                .lore(Component.text("Clique pra alternar (auto → permitir → bloquear)"))
                .build());
        inventory.setItem(15, ItemBuilder.of(settings.isSidebarEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(Component.text("Sidebar do clã: " + (settings.isSidebarEnabled() ? "Sim" : "Não")))
                .lore(Component.text("Clique pra alternar"))
                .build());

        ChatModeManager.Mode chatMode = services.chatModeManager().getMode(player.getUniqueId());
        inventory.setItem(16, ItemBuilder.of(Material.PAPER)
                .name(Component.text("Modo de chat: " + chatMode.name()))
                .lore(Component.text("Clique pra alternar normal/clã/aliança"))
                .build());

        inventory.setItem(22, ItemBuilder.of(Material.BARRIER)
                .name(Component.text("Sair do clã"))
                .lore(Component.text("Clique pra sair"))
                .build());
        inventory.setItem(31, ItemBuilder.of(Material.ARROW).name(Component.text("Voltar")).build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        PlayerSettings settings = services.playerSettingsManager().get(player.getUniqueId());
        switch (slot) {
            case 10 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                settings.setAllowInvites(!settings.isAllowInvites());
                services.playerSettingsManager().save(player.getUniqueId(), settings);
                open(player);
            }
            case 11 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                settings.setShowWarnings(!settings.isShowWarnings());
                services.playerSettingsManager().save(player.getUniqueId(), settings);
                open(player);
            }
            case 12 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                settings.setShowTag(!settings.isShowTag());
                services.playerSettingsManager().save(player.getUniqueId(), settings);
                open(player);
            }
            case 14 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                FfMode next = switch (settings.getFfMode()) {
                    case AUTO -> FfMode.PERMITIR;
                    case PERMITIR -> FfMode.BLOQUEAR;
                    case BLOQUEAR -> FfMode.AUTO;
                };
                settings.setFfMode(next);
                services.playerSettingsManager().save(player.getUniqueId(), settings);
                open(player);
            }
            case 15 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                settings.setSidebarEnabled(!settings.isSidebarEnabled());
                services.playerSettingsManager().save(player.getUniqueId(), settings);
                services.scoreboardManager().refresh(player);
                open(player);
            }
            case 16 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                ChatModeManager.Mode current = services.chatModeManager().getMode(player.getUniqueId());
                ChatModeManager.Mode next = switch (current) {
                    case NORMAL -> ChatModeManager.Mode.CLAN;
                    case CLAN -> ChatModeManager.Mode.ALIANCA;
                    case ALIANCA -> ChatModeManager.Mode.NORMAL;
                };
                services.chatModeManager().setMode(player.getUniqueId(), next);
                open(player);
            }
            case 22 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new ConfirmFrame("Sair do clã [" + clan.getTag() + "]?", () -> {
                    Optional<ClanMember> memberOptional = services.clanManager().getMember(player.getUniqueId());
                    if (memberOptional.isEmpty()) {
                        player.closeInventory();
                        return;
                    }
                    ClanMember member = memberOptional.get();
                    int totalMembers = services.clanManager().getMembers(clan.getId()).size();
                    var highest = clan.getHighestRank();
                    boolean isLeader = highest != null && highest.getId() == member.getRankId();
                    boolean onlyLeader = isLeader && services.clanManager().getMembers(clan.getId()).stream()
                            .filter(other -> {
                                var h = clan.getHighestRank();
                                return h != null && h.getId() == other.getRankId();
                            }).count() <= 1 && totalMembers > 1;
                    if (onlyLeader) {
                        player.closeInventory();
                        player.sendMessage(services.languageManager().get("sair-somente-lider-transferir"));
                        return;
                    }
                    services.memberManager().kick(clan, member);
                    player.closeInventory();
                    player.sendMessage(services.languageManager().get("sair-sucesso", "tag", clan.getTag()));
                    if (totalMembers <= 1) {
                        try {
                            services.clanManager().disbandClan(clan);
                            services.inviteManager().removeAllInvitesForClan(clan.getId());
                            services.relationManager().removeAllRelations(clan.getId());
                            services.warManager().removeAllWars(clan.getId());
                            services.noticeManager().removeAllForClan(clan.getId());
                        } catch (SQLException e) {
                            logger.log(Level.SEVERE, "Falha ao desfazer cla vazio pela GUI (sair)", e);
                        }
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
}
