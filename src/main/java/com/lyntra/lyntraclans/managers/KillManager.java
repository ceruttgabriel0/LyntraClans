package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.config.ConfigManager;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.FfMode;
import com.lyntra.lyntraclans.domain.KillCategory;

import java.util.Optional;
import java.util.UUID;

public final class KillManager {

    private final ConfigManager configManager;
    private final ClanManager clanManager;
    private final RelationManager relationManager;
    private final PlayerSettingsManager playerSettingsManager;
    private final WarManager warManager;

    public KillManager(ConfigManager configManager, ClanManager clanManager, RelationManager relationManager,
                        PlayerSettingsManager playerSettingsManager, WarManager warManager) {
        this.configManager = configManager;
        this.clanManager = clanManager;
        this.relationManager = relationManager;
        this.playerSettingsManager = playerSettingsManager;
        this.warManager = warManager;
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
        if (killer != null) {
            killer.addKill(category);
            if (killerClanId != -1 && victimClanId != -1 && warManager.isAtWar(killerClanId, victimClanId)) {
                double bonus = configManager.killWeight(category) * (configManager.warWeightMultiplier() - 1);
                killer.addWarBonus(bonus);
            }
            clanManager.persistMember(killer);
        }
        if (victim != null) {
            victim.addDeath();
            clanManager.persistMember(victim);
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
