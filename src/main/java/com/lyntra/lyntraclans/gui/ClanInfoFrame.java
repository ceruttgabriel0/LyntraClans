package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.KillCategory;
import com.lyntra.lyntraclans.util.TimeFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.stream.Collectors;

public final class ClanInfoFrame extends AbstractFrame {

    private final ClanServices services;
    private final Clan clan;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ClanInfoFrame(ClanServices services, Clan clan, Runnable onBack) {
        this.services = services;
        this.clan = clan;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 27, miniMessage.deserialize("<dark_gray>[" + clan.getTag() + "] " + clan.getName()));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        List<ClanMember> members = services.clanManager().getMembers(clan.getId());
        String leaders = members.stream()
                .filter(m -> clan.getHighestRank() != null && m.getRankId() == clan.getHighestRank().getId())
                .map(m -> Bukkit.getOfflinePlayer(m.getUuid()).getName())
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.joining(", "));
        long online = members.stream().map(ClanMember::getUuid).map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline()).count();

        ItemBuilder info = ItemBuilder.clanBanner(clan.getColor())
                .name(Component.text("[" + clan.getTag() + "] " + clan.getName()))
                .lore(services.languageManager().get("info-descricao", "descricao",
                        clan.getDescription().isBlank() ? "-" : clan.getDescription()))
                .lore(services.languageManager().get("info-lideres", "lideres",
                        leaders.isBlank() ? "-" : leaders))
                .lore(services.languageManager().get("info-membros", "online", String.valueOf(online),
                        "total", String.valueOf(members.size())))
                .lore(services.languageManager().get("info-kdr", "kdr",
                        String.format("%.2f", services.killManager().clanWeightedKdr(clan))))
                .lore(services.languageManager().get("info-kills",
                        "rival", String.valueOf(services.killManager().clanKills(clan, KillCategory.RIVAL)),
                        "aliado", String.valueOf(services.killManager().clanKills(clan, KillCategory.ALIADO)),
                        "neutro", String.valueOf(services.killManager().clanKills(clan, KillCategory.NEUTRO)),
                        "civil", String.valueOf(services.killManager().clanKills(clan, KillCategory.CIVIL))))
                .lore(services.languageManager().get("info-banco", "saldo", services.bankManager().format(clan.getBalance())))
                .lore(services.languageManager().get("info-fundado", "data", TimeFormat.format(clan.getFoundedAt())))
                .lore(services.languageManager().get("info-inativo", "dias",
                        String.valueOf(TimeFormat.daysSince(clan.getLastUsedAt()))));
        inventory.setItem(13, info.build());

        inventory.setItem(11, ItemBuilder.of(Material.ARMOR_STAND)
                .name(Component.text("Ver membros"))
                .build());
        inventory.setItem(22, ItemBuilder.of(Material.ARROW).name(Component.text("Voltar")).build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        if (slot == 22) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            onBack.run();
        } else if (slot == 11) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new MembersFrame(services, clan, () -> open(player)).open(player);
        }
    }
}
