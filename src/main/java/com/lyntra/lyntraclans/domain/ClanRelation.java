package com.lyntra.lyntraclans.domain;

public final class ClanRelation {

    private final int id;
    private final int clanId;
    private final int targetClanId;
    private RelationType type;
    private final long createdAt;

    public ClanRelation(int id, int clanId, int targetClanId, RelationType type, long createdAt) {
        this.id = id;
        this.clanId = clanId;
        this.targetClanId = targetClanId;
        this.type = type;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getClanId() {
        return clanId;
    }

    public int getTargetClanId() {
        return targetClanId;
    }

    public RelationType getType() {
        return type;
    }

    public void setType(RelationType type) {
        this.type = type;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
