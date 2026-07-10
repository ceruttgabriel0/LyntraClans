package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanRelation;
import com.lyntra.lyntraclans.domain.RelationType;
import com.lyntra.lyntraclans.storage.dao.RelationDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RelationManager {

    public enum AllianceResult {
        REQUEST_SENT,
        ACCEPTED,
        ALREADY_PENDING,
        ALREADY_ALLIED
    }

    private final Logger logger;
    private final RelationDao relationDao;

    public RelationManager(Logger logger, RelationDao relationDao) {
        this.logger = logger;
        this.relationDao = relationDao;
    }

    public AllianceResult proposeOrAcceptAlliance(Clan requester, Clan target) throws SQLException {
        Optional<ClanRelation> incoming = relationDao.find(target.getId(), requester.getId());
        if (incoming.isPresent() && incoming.get().getType() == RelationType.ALLY_PENDING) {
            relationDao.updateType(incoming.get().getId(), RelationType.ALLY);
            Optional<ClanRelation> outgoing = relationDao.find(requester.getId(), target.getId());
            if (outgoing.isPresent()) {
                relationDao.updateType(outgoing.get().getId(), RelationType.ALLY);
            } else {
                relationDao.insert(requester.getId(), target.getId(), RelationType.ALLY, System.currentTimeMillis());
            }
            clearRivalry(requester.getId(), target.getId());
            return AllianceResult.ACCEPTED;
        }

        Optional<ClanRelation> outgoing = relationDao.find(requester.getId(), target.getId());
        if (outgoing.isPresent()) {
            if (outgoing.get().getType() == RelationType.ALLY) {
                return AllianceResult.ALREADY_ALLIED;
            }
            if (outgoing.get().getType() == RelationType.ALLY_PENDING) {
                return AllianceResult.ALREADY_PENDING;
            }
            // Sobra so o caso RIVAL aqui (relacao unilateral existente na mesma direcao) - precisa
            // apagar antes de inserir, senao bate na constraint UNIQUE(clan_id, target_clan_id) e
            // vira excecao (bug real achado propondo alianca com quem ja era rival).
            relationDao.delete(outgoing.get().getId());
        }
        relationDao.insert(requester.getId(), target.getId(), RelationType.ALLY_PENDING, System.currentTimeMillis());
        return AllianceResult.REQUEST_SENT;
    }

    public void declareRival(Clan clan, Clan target) throws SQLException {
        relationDao.deleteBetween(clan.getId(), target.getId());
        relationDao.insert(clan.getId(), target.getId(), RelationType.RIVAL, System.currentTimeMillis());
    }

    public void removeRival(Clan clan, Clan target) throws SQLException {
        relationDao.deleteBetween(clan.getId(), target.getId());
    }

    public void removeAlliance(Clan clan, Clan target) throws SQLException {
        relationDao.deleteBetween(clan.getId(), target.getId());
        relationDao.deleteBetween(target.getId(), clan.getId());
    }

    private void clearRivalry(int clanIdA, int clanIdB) throws SQLException {
        Optional<ClanRelation> a = relationDao.find(clanIdA, clanIdB);
        if (a.isPresent() && a.get().getType() == RelationType.RIVAL) {
            relationDao.delete(a.get().getId());
        }
        Optional<ClanRelation> b = relationDao.find(clanIdB, clanIdA);
        if (b.isPresent() && b.get().getType() == RelationType.RIVAL) {
            relationDao.delete(b.get().getId());
        }
    }

    public boolean isRival(int clanId, int otherClanId) {
        return relationTypeBetween(clanId, otherClanId) == RelationType.RIVAL;
    }

    public boolean isAlly(int clanId, int otherClanId) {
        return relationTypeBetween(clanId, otherClanId) == RelationType.ALLY;
    }

    private RelationType relationTypeBetween(int clanId, int otherClanId) {
        try {
            Optional<ClanRelation> a = relationDao.find(clanId, otherClanId);
            if (a.isPresent()) {
                return a.get().getType();
            }
            Optional<ClanRelation> b = relationDao.find(otherClanId, clanId);
            return b.map(ClanRelation::getType).orElse(null);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao consultar relacao entre clas", e);
            return null;
        }
    }

    public List<Clan> getAllies(Clan clan, ClanManager clanManager) {
        return getRelated(clan, clanManager, RelationType.ALLY);
    }

    public List<Clan> getRivals(Clan clan, ClanManager clanManager) {
        return getRelated(clan, clanManager, RelationType.RIVAL);
    }

    private List<Clan> getRelated(Clan clan, ClanManager clanManager, RelationType type) {
        List<Clan> result = new ArrayList<>();
        java.util.Set<Integer> seenIds = new java.util.HashSet<>();
        try {
            for (ClanRelation relation : relationDao.findByClan(clan.getId())) {
                if (relation.getType() != type) {
                    continue;
                }
                // Alianca fica gravada nas duas direcoes (uma linha por clan), entao o mesmo par
                // aparece duas vezes na consulta - so a rivalidade e unidirecional. Sem o dedupe
                // aqui o outro cla apareceria repetido em /clan info.
                int otherId = relation.getClanId() == clan.getId() ? relation.getTargetClanId() : relation.getClanId();
                if (!seenIds.add(otherId)) {
                    continue;
                }
                clanManager.getClanById(otherId).ifPresent(result::add);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao consultar relacoes do cla", e);
        }
        return result;
    }

    public record RelationPair(Clan clanA, Clan clanB) {
    }

    /** Lista cada par uma unica vez (aliança/rival), pra telas de "todas as alianças/rivalidades do servidor". */
    public List<RelationPair> getAllServerWide(RelationType type, ClanManager clanManager) {
        List<RelationPair> result = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (Clan clan : clanManager.getAllClans()) {
            for (Clan related : getRelated(clan, clanManager, type)) {
                String key = Math.min(clan.getId(), related.getId()) + "-" + Math.max(clan.getId(), related.getId());
                if (seen.add(key)) {
                    result.add(new RelationPair(clan, related));
                }
            }
        }
        return result;
    }

    public void removeAllRelations(int clanId) {
        try {
            relationDao.deleteByClan(clanId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao remover relacoes do cla", e);
        }
    }
}
