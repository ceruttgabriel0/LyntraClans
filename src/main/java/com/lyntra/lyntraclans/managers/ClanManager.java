package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.config.ConfigManager;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.domain.Rank;
import com.lyntra.lyntraclans.storage.dao.ClanDao;
import com.lyntra.lyntraclans.storage.dao.MemberDao;
import com.lyntra.lyntraclans.storage.dao.RankDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fonte da verdade em memoria pra clas, cargos e membros. Todas as escritas passam
 * por aqui e sao persistidas de forma sincrona no SQLite (aceitavel pro volume de
 * dados de um plugin de cla; ver MEMORIA.md pra nota sobre mover pra async no futuro).
 */
public final class ClanManager {

    private final Logger logger;
    private final ConfigManager configManager;
    private final ClanDao clanDao;
    private final RankDao rankDao;
    private final MemberDao memberDao;

    private final Map<Integer, Clan> clansById = new HashMap<>();
    private final Map<String, Integer> clanIdByTag = new HashMap<>();
    private final Map<UUID, ClanMember> membersByUuid = new HashMap<>();

    public ClanManager(Logger logger, ConfigManager configManager, ClanDao clanDao, RankDao rankDao,
                        MemberDao memberDao) {
        this.logger = logger;
        this.configManager = configManager;
        this.clanDao = clanDao;
        this.rankDao = rankDao;
        this.memberDao = memberDao;
    }

    public void loadAll() {
        clansById.clear();
        clanIdByTag.clear();
        membersByUuid.clear();
        try {
            for (Clan clan : clanDao.findAll()) {
                clansById.put(clan.getId(), clan);
                clanIdByTag.put(clan.getTag().toLowerCase(Locale.ROOT), clan.getId());
                clan.getRanks().addAll(rankDao.findByClan(clan.getId()));
                for (ClanMember member : memberDao.findByClan(clan.getId())) {
                    membersByUuid.put(member.getUuid(), member);
                }
            }
            logger.info("Clas carregados: " + clansById.size());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao carregar clas do banco de dados", e);
        }
    }

    public Collection<Clan> getAllClans() {
        return Collections.unmodifiableCollection(clansById.values());
    }

    public Optional<Clan> getClanById(int id) {
        return Optional.ofNullable(clansById.get(id));
    }

    public Optional<Clan> getClanByTag(String tag) {
        Integer id = clanIdByTag.get(tag.toLowerCase(Locale.ROOT));
        return id == null ? Optional.empty() : Optional.ofNullable(clansById.get(id));
    }

    public Optional<Clan> getClanByTagOrName(String query) {
        Optional<Clan> byTag = getClanByTag(query);
        if (byTag.isPresent()) {
            return byTag;
        }
        return clansById.values().stream()
                .filter(clan -> clan.getName().equalsIgnoreCase(query))
                .findFirst();
    }

    public Optional<ClanMember> getMember(UUID uuid) {
        return Optional.ofNullable(membersByUuid.get(uuid));
    }

    public Optional<Clan> getClanOfPlayer(UUID uuid) {
        ClanMember member = membersByUuid.get(uuid);
        return member == null ? Optional.empty() : getClanById(member.getClanId());
    }

    public List<ClanMember> getMembers(int clanId) {
        List<ClanMember> result = new ArrayList<>();
        for (ClanMember member : membersByUuid.values()) {
            if (member.getClanId() == clanId) {
                result.add(member);
            }
        }
        return result;
    }

    public boolean tagInUse(String tag) {
        return clanIdByTag.containsKey(tag.toLowerCase(Locale.ROOT));
    }

    public boolean nameInUse(String name) {
        return clansById.values().stream().anyMatch(clan -> clan.getName().equalsIgnoreCase(name));
    }

    public Clan createClan(String tag, String name, UUID leaderUuid) throws SQLException {
        long now = System.currentTimeMillis();
        Clan clan = clanDao.insert(tag, name, "GOLD", now);
        clan.setMaxMembers(configManager.initialMaxMembers());
        clan.setChestSize(configManager.initialChestSize());
        clanDao.update(clan);

        for (DefaultRank defaultRank : DefaultRank.values()) {
            Rank rank = rankDao.insert(clan.getId(), defaultRank.rankName, defaultRank.priority,
                    defaultRank.permissions, defaultRank.isDefault);
            clan.getRanks().add(rank);
        }
        Rank leaderRank = clan.getHighestRank();

        ClanMember member = memberDao.insert(leaderUuid, clan.getId(), leaderRank.getId(), now);

        clansById.put(clan.getId(), clan);
        clanIdByTag.put(clan.getTag().toLowerCase(Locale.ROOT), clan.getId());
        membersByUuid.put(leaderUuid, member);
        return clan;
    }

    public void disbandClan(Clan clan) throws SQLException {
        for (ClanMember member : getMembers(clan.getId())) {
            membersByUuid.remove(member.getUuid());
        }
        clanDao.delete(clan.getId());
        clansById.remove(clan.getId());
        clanIdByTag.remove(clan.getTag().toLowerCase(Locale.ROOT));
    }

    public ClanMember addMember(Clan clan, UUID uuid, Rank rank) throws SQLException {
        ClanMember member = memberDao.insert(uuid, clan.getId(), rank.getId(), System.currentTimeMillis());
        membersByUuid.put(uuid, member);
        return member;
    }

    public void removeMember(ClanMember member) throws SQLException {
        memberDao.delete(member.getUuid());
        membersByUuid.remove(member.getUuid());
    }

    public void persistClan(Clan clan) {
        try {
            clanDao.update(clan);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao salvar cla " + clan.getTag(), e);
        }
    }

    public void persistMember(ClanMember member) {
        try {
            memberDao.update(member);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao salvar membro " + member.getUuid(), e);
        }
    }

    public void renameTagIndex(String oldTag, String newTag, int clanId) {
        clanIdByTag.remove(oldTag.toLowerCase(Locale.ROOT));
        clanIdByTag.put(newTag.toLowerCase(Locale.ROOT), clanId);
    }

    public void touch(Clan clan) {
        clan.setLastUsedAt(System.currentTimeMillis());
        persistClan(clan);
    }

    private enum DefaultRank {
        LIDER("Líder", 40, EnumSet.allOf(ClanPermission.class), false),
        CAPITAO("Capitão", 30, EnumSet.of(ClanPermission.CONVIDAR, ClanPermission.EXPULSAR,
                ClanPermission.PROMOVER, ClanPermission.BOLETIM, ClanPermission.BANCO_DEPOSITAR,
                ClanPermission.BANCO_VER_SALDO, ClanPermission.GERENCIAR_ALIANCA, ClanPermission.GERENCIAR_RIVAL,
                ClanPermission.ACESSAR_BAU),
                false),
        MEMBRO("Membro", 20, EnumSet.of(ClanPermission.BANCO_DEPOSITAR, ClanPermission.BANCO_VER_SALDO), false),
        RECRUTA("Recruta", 10, EnumSet.noneOf(ClanPermission.class), true);

        private final String rankName;
        private final int priority;
        private final Set<ClanPermission> permissions;
        private final boolean isDefault;

        DefaultRank(String rankName, int priority, Set<ClanPermission> permissions, boolean isDefault) {
            this.rankName = rankName;
            this.priority = priority;
            this.permissions = permissions;
            this.isDefault = isDefault;
        }
    }
}
