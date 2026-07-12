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

    // Layout: linha 0 e linha 3 sao so borda. Linha 1 = identidade/pessoal. Linha 2 = gestao/info.
    // Slot 31 (centro da linha 3) = ajuda. Ver handleClick() para o mapeamento de acao por slot.
    private static final int SLOT_PERFIL = 10;
    private static final int SLOT_CLA_OU_CRIAR = 11;
    private static final int SLOT_MEMBROS_OU_CONVITES = 12;
    private static final int SLOT_BASE = 13;
    private static final int SLOT_CONVIDAR = 14;
    private static final int SLOT_BANCO = 15;
    private static final int SLOT_AVISOS = 16;
    private static final int SLOT_CARGOS = 19;
    private static final int SLOT_DIPLOMACIA = 20;
    private static final int SLOT_CONFIGURACOES = 21;
    private static final int SLOT_PREFERENCIAS = 22;
    private static final int SLOT_BAU = 23;
    private static final int SLOT_RANKING = 24;
    private static final int SLOT_LISTA = 25;
    private static final int SLOT_AJUDA = 31;

    @Override
    protected void populate(Player player, Inventory inventory) {
        fillBorder(inventory);
        Optional<Clan> clanOptional = services.clanManager().getClanOfPlayer(player.getUniqueId());

        inventory.setItem(SLOT_PERFIL, ItemBuilder.playerHead(player)
                .name(Component.text("Meu perfil"))
                .lore(Component.text("Clique para ver seu perfil"))
                .build());

        if (clanOptional.isPresent()) {
            Clan clan = clanOptional.get();
            inventory.setItem(SLOT_CLA_OU_CRIAR, ItemBuilder.clanBanner(clan.getColor())
                    .name(Component.text("[" + clan.getTag() + "] " + clan.getName()))
                    .lore(Component.text("Clique para ver as informações do seu clã"))
                    .build());
            inventory.setItem(SLOT_MEMBROS_OU_CONVITES, ItemBuilder.of(Material.ARMOR_STAND)
                    .name(Component.text("Membros"))
                    .lore(Component.text("Clique para ver/gerenciar os membros"))
                    .build());
            inventory.setItem(SLOT_BASE, ItemBuilder.of(Material.RED_BED)
                    .name(Component.text("Base do clã"))
                    .lore(Component.text("Clique para teleportar"))
                    .build());
            inventory.setItem(SLOT_CONVIDAR, ItemBuilder.of(Material.PAPER)
                    .name(Component.text("Convidar jogador"))
                    .lore(Component.text("Clique para escolher quem convidar"))
                    .build());
            inventory.setItem(SLOT_BANCO, ItemBuilder.of(Material.GOLD_INGOT)
                    .name(Component.text("Banco do clã"))
                    .lore(Component.text("Saldo: " + services.bankManager().format(clan.getBalance())))
                    .build());
            inventory.setItem(SLOT_AVISOS, ItemBuilder.of(Material.BELL)
                    .name(Component.text("Quadro de avisos"))
                    .lore(Component.text("Clique para ver os avisos"))
                    .build());
            inventory.setItem(SLOT_CARGOS, ItemBuilder.of(Material.IRON_HELMET)
                    .name(Component.text("Cargos"))
                    .lore(Component.text("Clique para editar cargos e permissões"))
                    .build());
            inventory.setItem(SLOT_DIPLOMACIA, ItemBuilder.of(Material.SHIELD)
                    .name(Component.text("Diplomacia"))
                    .lore(Component.text("Aliados, rivais e guerra"))
                    .build());
            inventory.setItem(SLOT_CONFIGURACOES, ItemBuilder.of(Material.COMPARATOR)
                    .name(Component.text("Configurações do clã"))
                    .lore(Component.text("Cor, tag, descrição, upgrades, desfazer"))
                    .build());
            inventory.setItem(SLOT_PREFERENCIAS, ItemBuilder.of(Material.CLOCK)
                    .name(Component.text("Minhas preferências"))
                    .lore(Component.text("Toggles, fogo amigo, chat, sair"))
                    .build());
            inventory.setItem(SLOT_BAU, ItemBuilder.of(Material.CHEST)
                    .name(Component.text("Baú do clã"))
                    .lore(Component.text(clan.getChestSize() + " slots"))
                    .lore(Component.text("Clique para abrir"))
                    .build());
        } else {
            inventory.setItem(SLOT_CLA_OU_CRIAR, ItemBuilder.of(Material.BOOK)
                    .name(Component.text("Criar clã"))
                    .lore(Component.text("Clique para criar um clã"))
                    .build());
            inventory.setItem(SLOT_MEMBROS_OU_CONVITES, ItemBuilder.of(Material.PAPER)
                    .name(Component.text("Convites recebidos"))
                    .lore(Component.text("Clique para ver/responder convites"))
                    .build());
        }

        inventory.setItem(SLOT_RANKING, ItemBuilder.of(Material.ITEM_FRAME)
                .name(Component.text("Ranking"))
                .lore(Component.text("Clique para ver o ranking de clãs"))
                .build());

        inventory.setItem(SLOT_LISTA, ItemBuilder.of(Material.WHITE_BANNER)
                .name(Component.text("Lista de Clãs"))
                .lore(Component.text("Clique para ver todos os clãs"))
                .build());

        inventory.setItem(SLOT_AJUDA, ItemBuilder.of(Material.WRITABLE_BOOK)
                .name(Component.text("Ajuda"))
                .lore(Component.text("Clique para ver todos os comandos"))
                .build());
    }

    private void fillBorder(Inventory inventory) {
        java.util.Set<Integer> border = java.util.Set.of(
                0, 1, 2, 3, 4, 5, 6, 7, 8,
                9, 17, 18, 26,
                27, 28, 29, 30, 32, 33, 34, 35);
        for (int slot : border) {
            inventory.setItem(slot, ItemBuilder.filler());
        }
    }

    @Override
    public void handleClick(Player player, int slot) {
        Optional<Clan> clanOptional = services.clanManager().getClanOfPlayer(player.getUniqueId());
        switch (slot) {
            case SLOT_PERFIL -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new ProfileFrame(services, player, () -> open(player)).open(player);
            }
            case SLOT_CLA_OU_CRIAR -> {
                if (clanOptional.isPresent()) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    new ClanInfoFrame(services, clanOptional.get(), () -> open(player)).open(player);
                } else {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    new CreateClanAnvilFrame(services, services.logger()).open(player);
                }
            }
            case SLOT_MEMBROS_OU_CONVITES -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                if (clanOptional.isPresent()) {
                    new MembersFrame(services, clanOptional.get(), () -> open(player)).open(player);
                } else {
                    new InvitesFrame(services, services.logger(), () -> open(player)).open(player);
                }
            }
            case SLOT_BASE -> clanOptional.ifPresent(clan -> {
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
            case SLOT_CONVIDAR -> clanOptional.ifPresent(clan -> {
                Optional<ClanMember> member = services.clanManager().getMember(player.getUniqueId());
                boolean canInvite = member.isPresent() && hasPermission(clan, member.get(), ClanPermission.CONVIDAR);
                if (!canInvite) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new InvitePickerFrame(services, clan, () -> open(player)).open(player);
            });
            case SLOT_BANCO -> clanOptional.ifPresent(clan -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new BankFrame(services, clan, () -> open(player)).open(player);
            });
            case SLOT_AVISOS -> clanOptional.ifPresent(clan -> {
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
            case SLOT_CARGOS -> clanOptional.ifPresent(clan -> {
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
            case SLOT_DIPLOMACIA -> clanOptional.ifPresent(clan -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new DiplomacyFrame(services, clan, services.logger(), () -> open(player)).open(player);
            });
            case SLOT_CONFIGURACOES -> clanOptional.ifPresent(clan -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new ClanSettingsFrame(services, clan, services.logger(), () -> open(player)).open(player);
            });
            case SLOT_RANKING -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new RankingFrame(services, () -> open(player)).open(player);
            }
            case SLOT_LISTA -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new ClanListFrame(services, () -> open(player)).open(player);
            }
            case SLOT_PREFERENCIAS -> clanOptional.ifPresent(clan -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new PreferencesFrame(services, clan, services.logger(), () -> open(player)).open(player);
            });
            case SLOT_BAU -> clanOptional.ifPresent(clan -> {
                Optional<ClanMember> member = services.clanManager().getMember(player.getUniqueId());
                boolean canAccess = member.isPresent()
                        && hasPermission(clan, member.get(), ClanPermission.ACESSAR_BAU);
                if (!canAccess) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new ClanChestFrame(services, clan).open(player);
            });
            case SLOT_AJUDA -> {
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
