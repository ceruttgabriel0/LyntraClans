package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.KillCategory;
import com.lyntra.lyntraclans.domain.Rank;
import com.lyntra.lyntraclans.util.TimeFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public final class ProfileFrame extends AbstractFrame {

    private final ClanServices services;
    private final OfflinePlayer target;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ProfileFrame(ClanServices services, OfflinePlayer target, Runnable onBack) {
        this.services = services;
        this.target = target;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 27, miniMessage.deserialize("<dark_gray>Perfil de " + target.getName()));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        Optional<ClanMember> memberOptional = services.clanManager().getMember(target.getUniqueId());
        Optional<Clan> clanOptional = memberOptional.flatMap(m -> services.clanManager().getClanById(m.getClanId()));

        ItemBuilder head = ItemBuilder.playerHead(target)
                .name(Component.text(target.getName() == null ? "?" : target.getName()));
        if (clanOptional.isPresent() && memberOptional.isPresent()) {
            Clan clan = clanOptional.get();
            ClanMember member = memberOptional.get();
            Rank rank = clan.getRankById(member.getRankId());
            head.lore(services.languageManager().get("perfil-clan", "clan", "[" + clan.getTag() + "] " + clan.getName()))
                    .lore(services.languageManager().get("perfil-cargo", "cargo", rank == null ? "-" : rank.getName()))
                    .lore(services.languageManager().get("perfil-kdr", "kdr",
                            String.format("%.2f", services.killManager().weightedKdr(member))))
                    .lore(services.languageManager().get("perfil-kills",
                            "rival", String.valueOf(member.getKills(KillCategory.RIVAL)),
                            "aliado", String.valueOf(member.getKills(KillCategory.ALIADO)),
                            "neutro", String.valueOf(member.getKills(KillCategory.NEUTRO)),
                            "civil", String.valueOf(member.getKills(KillCategory.CIVIL))))
                    .lore(services.languageManager().get("perfil-mortes", "mortes", String.valueOf(member.getDeaths())))
                    .lore(services.languageManager().get("perfil-entrou", "data", TimeFormat.format(member.getJoinedAt())));
        } else {
            head.lore(services.languageManager().get("sem-clan"));
        }
        inventory.setItem(13, head.build());

        inventory.setItem(22, ItemBuilder.of(Material.ARROW)
                .name(Component.text("Voltar"))
                .build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        if (slot == 22) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            onBack.run();
        }
    }
}
