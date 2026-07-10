package com.lyntra.lyntraclans.domain;

import java.util.UUID;

public final class ClanMember {

    private final UUID uuid;
    private final int clanId;
    private int rankId;
    private final long joinedAt;
    private int killsRival;
    private int killsAlly;
    private int killsNeutral;
    private int killsCivil;
    private int deaths;
    private boolean trusted;

    public ClanMember(UUID uuid, int clanId, int rankId, long joinedAt, int killsRival, int killsAlly,
                       int killsNeutral, int killsCivil, int deaths, boolean trusted) {
        this.uuid = uuid;
        this.clanId = clanId;
        this.rankId = rankId;
        this.joinedAt = joinedAt;
        this.killsRival = killsRival;
        this.killsAlly = killsAlly;
        this.killsNeutral = killsNeutral;
        this.killsCivil = killsCivil;
        this.deaths = deaths;
        this.trusted = trusted;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getClanId() {
        return clanId;
    }

    public int getRankId() {
        return rankId;
    }

    public void setRankId(int rankId) {
        this.rankId = rankId;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public int getKills(KillCategory category) {
        return switch (category) {
            case RIVAL -> killsRival;
            case ALIADO -> killsAlly;
            case NEUTRO -> killsNeutral;
            case CIVIL -> killsCivil;
        };
    }

    public void addKill(KillCategory category) {
        switch (category) {
            case RIVAL -> killsRival++;
            case ALIADO -> killsAlly++;
            case NEUTRO -> killsNeutral++;
            case CIVIL -> killsCivil++;
        }
    }

    public int getTotalKills() {
        return killsRival + killsAlly + killsNeutral + killsCivil;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        deaths++;
    }

    public double getWeightedKdr(double weightRival, double weightAlly, double weightNeutral, double weightCivil) {
        double weighted = killsRival * weightRival + killsAlly * weightAlly
                + killsNeutral * weightNeutral + killsCivil * weightCivil;
        return deaths == 0 ? weighted : weighted / deaths;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }
}
