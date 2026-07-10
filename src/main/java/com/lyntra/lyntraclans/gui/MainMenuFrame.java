package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public final class MainMenuFrame extends AbstractFrame {

    private final ClanServices services;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MainMenuFrame(ClanServices services) {
        this.services = services;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 36, miniMessage.deserialize("<dark_gray>Clãs"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        Optional<Clan> clanOptional = services.clanManager().getClanOfPlayer(player.getUniqueId());

        inventory.setItem(10, ItemBuilder.playerHead(player)
                .name(Component.text("Meu perfil"))
                .lore(Component.text("Clique para ver seu perfil"))
                .build());

        if (clanOptional.isPresent()) {
            Clan clan = clanOptional.get();
            inventory.setItem(11, ItemBuilder.clanBanner(clan.getColor())
                    .name(Component.text("[" + clan.getTag() + "] " + clan.getName()))
                    .lore(Component.text("Clique para ver as informações do seu clã"))
                    .build());
            inventory.setItem(12, ItemBuilder.of(Material.ARMOR_STAND)
                    .name(Component.text("Membros"))
                    .lore(Component.text("Clique para ver/gerenciar os membros"))
                    .build());
            inventory.setItem(13, ItemBuilder.of(Material.RED_BED)
                    .name(Component.text("Base do clã"))
                    .lore(Component.text("Clique para teleportar"))
                    .build());
            inventory.setItem(14, ItemBuilder.of(Material.PAPER)
                    .name(Component.text("Convidar jogador"))
                    .lore(Component.text("Clique para escolher quem convidar"))
                    .build());
            inventory.setItem(15, ItemBuilder.of(Material.GOLD_INGOT)
                    .name(Component.text("Banco do clã"))
                    .lore(Component.text("Saldo: " + services.bankManager().format(clan.getBalance())))
                    .build());
            inventory.setItem(16, ItemBuilder.of(Material.BELL)
                    .name(Component.text("Quadro de avisos"))
                    .lore(Component.text("Clique para ver os avisos"))
                    .build());
            inventory.setItem(17, ItemBuilder.of(Material.IRON_HELMET)
                    .name(Component.text("Cargos"))
                    .lore(Component.text("Clique para editar cargos e permissões"))
                    .build());
        } else {
            inventory.setItem(11, ItemBuilder.of(Material.BOOK)
                    .name(Component.text("Criar clã"))
                    .lore(Component.text("Clique para criar um clã"))
                    .build());
            inventory.setItem(12, ItemBuilder.of(Material.PAPER)
                    .name(Component.text("Convites recebidos"))
                    .lore(Component.text("Clique para ver/responder convites"))
                    .build());
        }

        inventory.setItem(20, ItemBuilder.of(Material.ITEM_FRAME)
                .name(Component.text("Ranking"))
                .lore(Component.text("Clique para ver o ranking de clãs"))
                .build());

        inventory.setItem(21, ItemBuilder.of(Material.WHITE_BANNER)
                .name(Component.text("Lista de Clãs"))
                .lore(Component.text("Clique para ver todos os clãs"))
                .build());

        inventory.setItem(23, ItemBuilder.of(Material.WRITABLE_BOOK)
                .name(Component.text("Ajuda"))
                .lore(Component.text("Clique para ver todos os comandos"))
                .build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        Optional<Clan> clanOptional = services.clanManager().getClanOfPlayer(player.getUniqueId());
        switch (slot) {
            case 10 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new ProfileFrame(services, player, () -> open(player)).open(player);
            }
            case 11 -> {
                if (clanOptional.isPresent()) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    new ClanInfoFrame(services, clanOptional.get(), () -> open(player)).open(player);
                } else {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    new CreateClanAnvilFrame(services, services.logger()).open(player);
                }
            }
            case 12 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                if (clanOptional.isPresent()) {
                    new MembersFrame(services, clanOptional.get(), () -> open(player)).open(player);
                } else {
                    new InvitesFrame(services, services.logger(), () -> open(player)).open(player);
                }
            }
            case 13 -> clanOptional.ifPresent(clan -> {
                if (!clan.hasHome()) {
                    player.sendMessage(services.languageManager().get("home-sem-home"));
                    return;
                }
                org.bukkit.World world = Bukkit.getWorld(clan.getHomeWorld());
                if (world == null) {
                    player.sendMessage(services.languageManager().get("home-sem-home"));
                    return;
                }
                player.closeInventory();
                player.teleport(new org.bukkit.Location(world, clan.getHomeX(), clan.getHomeY(), clan.getHomeZ(),
                        clan.getHomeYaw(), clan.getHomePitch()));
                player.sendMessage(services.languageManager().get("home-teleportado"));
            });
            case 14 -> clanOptional.ifPresent(clan -> {
                Optional<ClanMember> member = services.clanManager().getMember(player.getUniqueId());
                boolean canInvite = member.isPresent() && hasPermission(clan, member.get(), ClanPermission.CONVIDAR);
                if (!canInvite) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new InvitePickerFrame(services, clan, () -> open(player)).open(player);
            });
            case 15 -> clanOptional.ifPresent(clan -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new BankFrame(services, clan, () -> open(player)).open(player);
            });
            case 16 -> clanOptional.ifPresent(clan -> {
                player.closeInventory();
                var notices = services.noticeManager().getNotices(clan.getId());
                if (notices.isEmpty()) {
                    player.sendMessage(services.languageManager().get("avisos-vazio"));
                    return;
                }
                player.sendMessage(services.languageManager().get("avisos-cabecalho"));
                notices.forEach(notice -> {
                    String author = Bukkit.getOfflinePlayer(notice.authorUuid()).getName();
                    player.sendMessage(services.languageManager().get("avisos-item",
                            "autor", author == null ? "?" : author, "mensagem", notice.message(),
                            "data", com.lyntra.lyntraclans.util.TimeFormat.format(notice.createdAt())));
                });
            });
            case 17 -> clanOptional.ifPresent(clan -> {
                Optional<ClanMember> member = services.clanManager().getMember(player.getUniqueId());
                boolean canManage = member.isPresent()
                        && hasPermission(clan, member.get(), ClanPermission.GERENCIAR_CARGOS);
                if (!canManage) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new RanksFrame(services, clan, () -> open(player)).open(player);
            });
            case 20 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new RankingFrame(services, () -> open(player)).open(player);
            }
            case 21 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new ClanListFrame(services, () -> open(player)).open(player);
            }
            case 23 -> {
                player.closeInventory();
                boolean hasClan = clanOptional.isPresent();
                services.languageManager().getList(hasClan ? "ajuda-com-clan" : "ajuda-sem-clan")
                        .forEach(player::sendMessage);
            }
            default -> {
            }
        }
    }

    private boolean hasPermission(Clan clan, ClanMember member, ClanPermission permission) {
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
