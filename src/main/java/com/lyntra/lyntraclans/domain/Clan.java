package com.lyntra.lyntraclans.domain;

import java.util.ArrayList;
import java.util.List;

public final class Clan {

    private final int id;
    private String tag;
    private String name;
    private String color;
    private String description;
    private double balance;
    private double fee;
    private boolean feeEnabled;
    private boolean friendlyFire;
    private int maxMembers;
    private int chestSize;
    private String homeWorld;
    private double homeX;
    private double homeY;
    private double homeZ;
    private float homeYaw;
    private float homePitch;
    private final long foundedAt;
    private long lastUsedAt;
    private boolean verified;
    private String chestContents;
    private final List<Rank> ranks = new ArrayList<>();

    public Clan(int id, String tag, String name, String color, String description, double balance, double fee,
                boolean feeEnabled, boolean friendlyFire, int maxMembers, int chestSize, long foundedAt,
                long lastUsedAt, boolean verified, String chestContents) {
        this.id = id;
        this.tag = tag;
        this.name = name;
        this.color = color;
        this.description = description;
        this.balance = balance;
        this.fee = fee;
        this.feeEnabled = feeEnabled;
        this.friendlyFire = friendlyFire;
        this.maxMembers = maxMembers;
        this.chestSize = chestSize;
        this.foundedAt = foundedAt;
        this.lastUsedAt = lastUsedAt;
        this.verified = verified;
        this.chestContents = chestContents == null ? "" : chestContents;
    }

    public int getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public boolean isFeeEnabled() {
        return feeEnabled;
    }

    public void setFeeEnabled(boolean feeEnabled) {
        this.feeEnabled = feeEnabled;
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public int getChestSize() {
        return chestSize;
    }

    public void setChestSize(int chestSize) {
        this.chestSize = chestSize;
    }

    public boolean hasHome() {
        return homeWorld != null;
    }

    public String getHomeWorld() {
        return homeWorld;
    }

    public double getHomeX() {
        return homeX;
    }

    public double getHomeY() {
        return homeY;
    }

    public double getHomeZ() {
        return homeZ;
    }

    public float getHomeYaw() {
        return homeYaw;
    }

    public float getHomePitch() {
        return homePitch;
    }

    public void setHome(String world, double x, double y, double z, float yaw, float pitch) {
        this.homeWorld = world;
        this.homeX = x;
        this.homeY = y;
        this.homeZ = z;
        this.homeYaw = yaw;
        this.homePitch = pitch;
    }

    public long getFoundedAt() {
        return foundedAt;
    }

    public long getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(long lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getChestContents() {
        return chestContents;
    }

    public void setChestContents(String chestContents) {
        this.chestContents = chestContents == null ? "" : chestContents;
    }

    public List<Rank> getRanks() {
        return ranks;
    }

    public Rank getHighestRank() {
        return ranks.stream().max((a, b) -> Integer.compare(a.getPriority(), b.getPriority())).orElse(null);
    }

    /**
     * Cargo de entrada pra novos membros e alvo de rebaixamento ao perder a lideranca.
     * Marcado explicitamente por uma flag (nao pelo menor priority atual): um cargo
     * customizado com prioridade mais baixa que o Recruta nao pode roubar esse papel
     * silenciosamente.
     */
    public Rank getDefaultRank() {
        return ranks.stream().filter(Rank::isDefault).findFirst()
                .orElseGet(() -> ranks.stream().min((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                        .orElse(null));
    }

    public Rank getRankById(int rankId) {
        return ranks.stream().filter(rank -> rank.getId() == rankId).findFirst().orElse(null);
    }
}
