package com.lyntra.lyntraclans.hooks;

import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.KillCategory;
import com.lyntra.lyntraclans.domain.Rank;
import com.lyntra.lyntraclans.managers.BankManager;
import com.lyntra.lyntraclans.managers.ClanManager;
import com.lyntra.lyntraclans.managers.KillManager;
import com.lyntra.lyntraclans.managers.PlayerSettingsManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class PlaceholderApiHook {

    private final JavaPlugin plugin;
    private final ClanManager clanManager;
    private final KillManager killManager;
    private final BankManager bankManager;
    private final PlayerSettingsManager playerSettingsManager;
    private Expansion expansion;

    public PlaceholderApiHook(JavaPlugin plugin, ClanManager clanManager, KillManager killManager,
                               BankManager bankManager, PlayerSettingsManager playerSettingsManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.killManager = killManager;
        this.bankManager = bankManager;
        this.playerSettingsManager = playerSettingsManager;
    }

    public void setup() {
        if (expansion != null || plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }
        expansion = new Expansion();
        expansion.register();
    }

    private final class Expansion extends PlaceholderExpansion {

        @Override
        public @NotNull String getIdentifier() {
            return "lyntraclans";
        }

        @Override
        public @NotNull String getAuthor() {
            return "zNeoaK_ (Gabriel Cerutt)";
        }

        @Override
        public @NotNull String getVersion() {
            return plugin.getPluginMeta().getVersion();
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
            if (offlinePlayer == null) {
                return "";
            }
            Optional<Clan> clanOptional = clanManager.getClanOfPlayer(offlinePlayer.getUniqueId());
            Optional<ClanMember> memberOptional = clanManager.getMember(offlinePlayer.getUniqueId());

            return switch (params) {
                case "clan_tag" -> clanOptional.map(Clan::getTag).orElse("");
                case "clan_tag_formatted" -> formattedTag(offlinePlayer, clanOptional);
                case "clan_name" -> clanOptional.map(Clan::getName).orElse("");
                case "clan_color" -> clanOptional.map(Clan::getColor).orElse("");
                case "clan_description" -> clanOptional.map(Clan::getDescription).orElse("");
                case "clan_balance" -> clanOptional.map(clan -> String.valueOf(clan.getBalance())).orElse("0");
                case "clan_balance_formatted" -> clanOptional.map(clan -> bankManager.format(clan.getBalance()))
                        .orElse(bankManager.format(0));
                case "clan_members_online" -> clanOptional.map(this::membersOnline).orElse("0");
                case "clan_members_total" -> clanOptional
                        .map(clan -> String.valueOf(clanManager.getMembers(clan.getId()).size())).orElse("0");
                case "clan_kdr" -> clanOptional.map(clan -> format(killManager.clanWeightedKdr(clan))).orElse("0");
                case "clan_rank" -> resolveRankName(clanOptional, memberOptional);
                case "kdr" -> memberOptional.map(member -> format(killManager.weightedKdr(member))).orElse("0");
                case "kills_rival" -> memberOptional.map(member -> String.valueOf(member.getKills(KillCategory.RIVAL)))
                        .orElse("0");
                case "kills_aliado" -> memberOptional
                        .map(member -> String.valueOf(member.getKills(KillCategory.ALIADO))).orElse("0");
                case "kills_neutro" -> memberOptional
                        .map(member -> String.valueOf(member.getKills(KillCategory.NEUTRO))).orElse("0");
                case "kills_civil" -> memberOptional.map(member -> String.valueOf(member.getKills(KillCategory.CIVIL)))
                        .orElse("0");
                case "deaths" -> memberOptional.map(member -> String.valueOf(member.getDeaths())).orElse("0");
                default -> null;
            };
        }

        /**
         * Tag pronta pra colar antes do nome no chat de outro plugin (ex: {@code %lyntraclans_clan_tag_formatted%}
         * no formato do LyntraChat) - ja vem com colchetes, cor do cla e o espaco final, ou string vazia
         * (sem colchete vazio) se o jogador nao tiver cla ou tiver desligado a tag pessoal
         * ({@code /clan alternar tag}, ate agora essa preferencia nunca era lida em lugar nenhum).
         */
        private String formattedTag(OfflinePlayer offlinePlayer, Optional<Clan> clanOptional) {
            if (clanOptional.isEmpty()) {
                return "";
            }
            if (!playerSettingsManager.get(offlinePlayer.getUniqueId()).isShowTag()) {
                return "";
            }
            Clan clan = clanOptional.get();
            String color = clan.getColor() == null || clan.getColor().isBlank() ? "white" : clan.getColor().toLowerCase();
            return "<" + color + ">[" + clan.getTag() + "]</" + color + "> ";
        }

        private String resolveRankName(Optional<Clan> clanOptional, Optional<ClanMember> memberOptional) {
            if (clanOptional.isEmpty() || memberOptional.isEmpty()) {
                return "";
            }
            Rank rank = clanOptional.get().getRankById(memberOptional.get().getRankId());
            return rank == null ? "" : rank.getName();
        }

        private String membersOnline(Clan clan) {
            long online = clanManager.getMembers(clan.getId()).stream()
                    .map(ClanMember::getUuid)
                    .map(Bukkit::getPlayer)
                    .filter(p -> p != null && p.isOnline())
                    .count();
            return String.valueOf(online);
        }

        private String format(double value) {
            return String.format("%.2f", value);
        }
    }
}
