package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.config.ConfigManager;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Da a cada jogador online um Scoreboard proprio (nao mexe no scoreboard principal do servidor,
 * entao convive com outros plugins que tambem usam scoreboard por jogador). Dois efeitos:
 * 1) Times por relacao, so pra colorir nome acima da cabeca/tablist (verde=aliado, vermelho=
 *    rival, vermelho escuro=guerra, branco=neutro, cinza=sem cla, ciano=proprio cla) - a cor e
 *    relativa a quem esta vendo, por isso precisa ser por jogador e nao um scoreboard global.
 * 2) Sidebar opcional (liga por preferencia pessoal) com os membros do cla online, no mesmo
 *    objeto de Scoreboard (um jogador so pode ter um Scoreboard ativo de cada vez).
 */
public final class ScoreboardManager {

    private static final String OBJECTIVE_SIDEBAR = "lc_sidebar";

    private final ConfigManager configManager;
    private final ClanManager clanManager;
    private final RelationManager relationManager;
    private final WarManager warManager;
    private final PlayerSettingsManager playerSettingsManager;

    public ScoreboardManager(ConfigManager configManager, ClanManager clanManager, RelationManager relationManager,
                              WarManager warManager, PlayerSettingsManager playerSettingsManager) {
        this.configManager = configManager;
        this.clanManager = clanManager;
        this.relationManager = relationManager;
        this.warManager = warManager;
        this.playerSettingsManager = playerSettingsManager;
    }

    public void refreshAll() {
        if (!configManager.nametagEnabled()) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            refresh(player);
        }
    }

    public void refresh(Player viewer) {
        if (!configManager.nametagEnabled()) {
            return;
        }
        Scoreboard board = viewer.getScoreboard();
        if (board == null || board == Bukkit.getScoreboardManager().getMainScoreboard()) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            viewer.setScoreboard(board);
        }

        Optional<Clan> viewerClanOptional = clanManager.getClanOfPlayer(viewer.getUniqueId());

        Team self = ensureTeam(board, "lc_self", configManager.nametagColorSelf());
        Team ally = ensureTeam(board, "lc_ally", configManager.nametagColorAlly());
        Team rival = ensureTeam(board, "lc_rival", configManager.nametagColorRival());
        Team war = ensureTeam(board, "lc_war", configManager.nametagColorWar());
        Team neutral = ensureTeam(board, "lc_neutral", configManager.nametagColorNeutral());
        Team noClan = ensureTeam(board, "lc_noclan", configManager.nametagColorNoClan());

        Set<String> stillOnline = new java.util.HashSet<>();
        for (Player other : Bukkit.getOnlinePlayers()) {
            stillOnline.add(other.getName());
            Team target = resolveTeam(viewerClanOptional, other, self, ally, rival, war, neutral, noClan);
            Team current = board.getEntryTeam(other.getName());
            // So mexe (remover+adicionar) se a relacao realmente mudou - refresh roda a cada poucos
            // segundos pra todo mundo online, reenviar entrada igual toda vez gera pacote de
            // team join/leave a toa pro cliente sem nenhuma mudanca visual.
            if (current == target) {
                continue;
            }
            if (current != null) {
                current.removeEntry(other.getName());
            }
            target.addEntry(other.getName());
        }

        // Jogador que ficou offline entre um refresh e outro - limpa a entrada velha dos times.
        for (Team team : List.of(self, ally, rival, war, neutral, noClan)) {
            for (String entry : Set.copyOf(team.getEntries())) {
                if (!stillOnline.contains(entry)) {
                    team.removeEntry(entry);
                }
            }
        }

        refreshSidebar(viewer, board, viewerClanOptional.orElse(null));
    }

    private Team resolveTeam(Optional<Clan> viewerClanOptional, Player other, Team self, Team ally, Team rival,
                              Team war, Team neutral, Team noClan) {
        Optional<Clan> otherClanOptional = clanManager.getClanOfPlayer(other.getUniqueId());
        if (otherClanOptional.isEmpty()) {
            return noClan;
        }
        if (viewerClanOptional.isEmpty()) {
            return neutral;
        }
        Clan viewerClan = viewerClanOptional.get();
        Clan otherClan = otherClanOptional.get();
        if (viewerClan.getId() == otherClan.getId()) {
            return self;
        }
        if (warManager.isAtWar(viewerClan.getId(), otherClan.getId())) {
            return war;
        }
        if (relationManager.isAlly(viewerClan.getId(), otherClan.getId())) {
            return ally;
        }
        if (relationManager.isRival(viewerClan.getId(), otherClan.getId())) {
            return rival;
        }
        return neutral;
    }

    private Team ensureTeam(Scoreboard board, String name, String colorName) {
        Team team = board.getTeam(name);
        if (team == null) {
            team = board.registerNewTeam(name);
        }
        NamedTextColor color = NamedTextColor.NAMES.value(colorName.toLowerCase(Locale.ROOT));
        team.color(color != null ? color : NamedTextColor.WHITE);
        return team;
    }

    private void refreshSidebar(Player viewer, Scoreboard board, Clan clan) {
        Objective existing = board.getObjective(OBJECTIVE_SIDEBAR);
        boolean wants = clan != null && playerSettingsManager.get(viewer.getUniqueId()).isSidebarEnabled();
        if (!wants) {
            if (existing != null) {
                existing.unregister();
            }
            return;
        }
        Objective objective = existing;
        if (objective == null) {
            Component title = LegacyComponentSerializer.legacyAmpersand().deserialize(configManager.sidebarTitle());
            objective = board.registerNewObjective(OBJECTIVE_SIDEBAR, Criteria.DUMMY, title);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        List<ClanMember> members = clanManager.getMembers(clan.getId());
        Set<String> shownNames = new java.util.HashSet<>();
        int line = members.size();
        int shown = 0;
        for (ClanMember member : members) {
            if (shown >= 14) {
                break;
            }
            UUID uuid = member.getUuid();
            Player onlineMember = Bukkit.getPlayer(uuid);
            if (onlineMember == null || !onlineMember.isOnline()) {
                continue;
            }
            objective.getScore(onlineMember.getName()).setScore(line);
            shownNames.add(onlineMember.getName());
            line--;
            shown++;
        }
        for (String previousEntry : Set.copyOf(objective.getScoreboard().getEntries())) {
            if (!shownNames.contains(previousEntry) && objective.getScore(previousEntry).isScoreSet()) {
                board.resetScores(previousEntry);
            }
        }
    }
}
