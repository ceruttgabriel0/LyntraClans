package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.config.ConfigManager;
import com.lyntra.lyntraclans.config.LanguageManager;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.FfMode;
import com.lyntra.lyntraclans.domain.KillCategory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public final class KillManager {

    private final ConfigManager configManager;
    private final ClanManager clanManager;
    private final RelationManager relationManager;
    private final PlayerSettingsManager playerSettingsManager;
    private final WarManager warManager;
    private final LanguageManager languageManager;

    public KillManager(ConfigManager configManager, ClanManager clanManager, RelationManager relationManager,
                        PlayerSettingsManager playerSettingsManager, WarManager warManager,
                        LanguageManager languageManager) {
        this.configManager = configManager;
        this.clanManager = clanManager;
        this.relationManager = relationManager;
        this.playerSettingsManager = playerSettingsManager;
        this.warManager = warManager;
        this.languageManager = languageManager;
    }

    public KillCategory categorize(Clan killerClan, Clan victimClan) {
        if (victimClan == null) {
            return KillCategory.CIVIL;
        }
        if (killerClan == null) {
            return KillCategory.CIVIL;
        }
        if (relationManager.isRival(killerClan.getId(), victimClan.getId())) {
            return KillCategory.RIVAL;
        }
        if (relationManager.isAlly(killerClan.getId(), victimClan.getId())) {
            return KillCategory.ALIADO;
        }
        return KillCategory.NEUTRO;
    }

    public void registerKill(ClanMember killer, ClanMember victim, KillCategory category) {
        registerKill(killer, victim, category, -1, -1);
    }

    /**
     * killerClanId/victimClanId servem so pra checar se os dois clas estao em guerra ativa e aplicar
     * o multiplicador extra de peso (ver {@link ConfigManager#warWeightMultiplier()}). Passe -1 quando
     * um dos dois lados nao tem cla (kill Civil), a guerra nunca se aplica nesse caso.
     */
    public void registerKill(ClanMember killer, ClanMember victim, KillCategory category, int killerClanId,
                              int victimClanId) {
        double weightedValue = 0;
        if (killer != null) {
            killer.addKill(category);
            weightedValue = configManager.killWeight(category);
            if (killerClanId != -1 && victimClanId != -1 && warManager.isAtWar(killerClanId, victimClanId)) {
                double bonus = configManager.killWeight(category) * (configManager.warWeightMultiplier() - 1);
                killer.addWarBonus(bonus);
                weightedValue += bonus;
            }
            clanManager.persistMember(killer);
        }
        if (victim != null) {
            victim.addDeath();
            clanManager.persistMember(victim);
        }
        if (killerClanId != -1 && weightedValue > 0) {
            awardXp(killerClanId, weightedValue);
        }
    }

    /**
     * XP e nivel do cla: formula linear simples (nivel N precisa de (N-1)*xp-per-level de XP
     * acumulado). Cada nivel subido da um bonus fixo de slots de membro GRATIS (nao mexe no
     * teto pago via upgrade, so soma em cima). Ve {@link ConfigManager#xpPerLevel()}.
     */
    private void awardXp(int clanId, double weightedValue) {
        Optional<Clan> clanOptional = clanManager.getClanById(clanId);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        long xpGained = Math.round(weightedValue * configManager.xpPerWeightedKill());
        if (xpGained <= 0) {
            return;
        }
        clan.setXp(clan.getXp() + xpGained);
        long xpPerLevel = Math.max(1, configManager.xpPerLevel());
        int newLevel = 1 + (int) (clan.getXp() / xpPerLevel);
        int oldLevel = clan.getLevel();
        if (newLevel > oldLevel) {
            int levelsGained = newLevel - oldLevel;
            clan.setLevel(newLevel);
            clan.setMaxMembers(clan.getMaxMembers() + levelsGained * configManager.memberBonusPerLevel());
            broadcastLevelUp(clan, newLevel);
        }
        clanManager.persistClan(clan);
    }

    private void broadcastLevelUp(Clan clan, int newLevel) {
        for (ClanMember member : clanManager.getMembers(clan.getId())) {
            Player online = Bukkit.getPlayer(member.getUuid());
            if (online != null) {
                online.sendMessage(languageManager.get("clan-subiu-nivel", "nivel", String.valueOf(newLevel)));
            }
        }
    }

    public double weightedKdr(ClanMember member) {
        return member.getWeightedKdr(
                configManager.killWeight(KillCategory.RIVAL),
                configManager.killWeight(KillCategory.ALIADO),
                configManager.killWeight(KillCategory.NEUTRO),
                configManager.killWeight(KillCategory.CIVIL)
        );
    }

    public double clanWeightedKdr(Clan clan) {
        double totalWeighted = 0;
        double totalDeaths = 0;
        for (ClanMember member : clanManager.getMembers(clan.getId())) {
            totalWeighted += member.getKills(KillCategory.RIVAL) * configManager.killWeight(KillCategory.RIVAL)
                    + member.getKills(KillCategory.ALIADO) * configManager.killWeight(KillCategory.ALIADO)
                    + member.getKills(KillCategory.NEUTRO) * configManager.killWeight(KillCategory.NEUTRO)
                    + member.getKills(KillCategory.CIVIL) * configManager.killWeight(KillCategory.CIVIL);
            totalDeaths += member.getDeaths();
        }
        return totalDeaths == 0 ? totalWeighted : totalWeighted / totalDeaths;
    }

    public int clanKills(Clan clan, KillCategory category) {
        int total = 0;
        for (ClanMember member : clanManager.getMembers(clan.getId())) {
            total += member.getKills(category);
        }
        return total;
    }

    public int clanDeaths(Clan clan) {
        int total = 0;
        for (ClanMember member : clanManager.getMembers(clan.getId())) {
            total += member.getDeaths();
        }
        return total;
    }

    /**
     * O `friendlyFire` do cla e o padrao pra todo mundo (`clanff bloquear/permitir`). A vitima pode
     * sobrepor esse padrao pessoalmente (`ff auto/permitir/bloquear`) - `auto` usa o padrao do cla,
     * `permitir` sempre deixa o dano passar, `bloquear` sempre bloqueia, mesmo se o cla permitir.
     */
    public boolean shouldBlockDamage(Optional<Clan> attackerClan, Optional<Clan> victimClan, UUID victimUuid) {
        if (attackerClan.isEmpty() || victimClan.isEmpty()) {
            return false;
        }
        Clan attacker = attackerClan.get();
        Clan victim = victimClan.get();
        boolean sameClan = attacker.getId() == victim.getId();
        boolean allied = !sameClan && relationManager.isAlly(attacker.getId(), victim.getId());
        if (!sameClan && !allied) {
            return false;
        }
        if ((sameClan && !configManager.blockDamageSameClan()) || (allied && !configManager.blockDamageAllies())) {
            return false;
        }

        FfMode personal = playerSettingsManager.get(victimUuid).getFfMode();
        if (personal == FfMode.PERMITIR) {
            return false;
        }
        if (personal == FfMode.BLOQUEAR) {
            return true;
        }
        return !attacker.isFriendlyFire();
    }
}
