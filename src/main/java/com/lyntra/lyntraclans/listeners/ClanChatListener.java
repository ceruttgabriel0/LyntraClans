package com.lyntra.lyntraclans.listeners;

import com.lyntra.lyntraclans.config.LanguageManager;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.Rank;
import com.lyntra.lyntraclans.managers.ChatModeManager;
import com.lyntra.lyntraclans.managers.ClanManager;
import com.lyntra.lyntraclans.managers.RelationManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Optional;

public final class ClanChatListener implements Listener {

    private final ClanManager clanManager;
    private final ChatModeManager chatModeManager;
    private final RelationManager relationManager;
    private final LanguageManager languageManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ClanChatListener(ClanManager clanManager, ChatModeManager chatModeManager,
                             RelationManager relationManager, LanguageManager languageManager) {
        this.clanManager = clanManager;
        this.chatModeManager = chatModeManager;
        this.relationManager = relationManager;
        this.languageManager = languageManager;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        ChatModeManager.Mode mode = chatModeManager.getMode(player.getUniqueId());
        if (mode == ChatModeManager.Mode.NORMAL) {
            return;
        }
        Optional<Clan> clanOptional = clanManager.getClanOfPlayer(player.getUniqueId());
        if (clanOptional.isEmpty()) {
            chatModeManager.clear(player.getUniqueId());
            return;
        }
        event.setCancelled(true);
        Clan clan = clanOptional.get();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        List<Player> recipients = mode == ChatModeManager.Mode.CLAN
                ? clanRecipients(clan)
                : allianceRecipients(clan);

        String cargo = resolveRankDisplayName(clan, player);
        String cor = clan.getColor() == null || clan.getColor().isBlank() ? "white" : clan.getColor().toLowerCase();

        String tagKey = mode == ChatModeManager.Mode.CLAN ? "clan-chat-formato" : "alianca-chat-formato";
        Component formatted = miniMessage.deserialize(languageManager.raw(tagKey, "tag", clan.getTag(), "cor", cor,
                "cargo", escapeMiniMessage(cargo), "jogador", player.getName(),
                "mensagem", escapeMiniMessage(message)));
        for (Player recipient : recipients) {
            recipient.sendMessage(formatted);
        }
    }

    private String resolveRankDisplayName(Clan clan, Player player) {
        Optional<ClanMember> memberOptional = clanManager.getMember(player.getUniqueId());
        if (memberOptional.isEmpty()) {
            return "";
        }
        Rank rank = clan.getRankById(memberOptional.get().getRankId());
        return rank == null ? "" : rank.getDisplayName();
    }

    private List<Player> clanRecipients(Clan clan) {
        return clanManager.getMembers(clan.getId()).stream()
                .map(ClanMember::getUuid)
                .map(org.bukkit.Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .toList();
    }

    private List<Player> allianceRecipients(Clan clan) {
        List<Player> recipients = new java.util.ArrayList<>(clanRecipients(clan));
        for (Clan ally : relationManager.getAllies(clan, clanManager)) {
            recipients.addAll(clanRecipients(ally));
        }
        return recipients;
    }

    private String escapeMiniMessage(String raw) {
        return raw.replace("<", "\\<");
    }
}
