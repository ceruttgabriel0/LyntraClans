package com.lyntra.lyntraclans.domain;

import java.util.EnumSet;
import java.util.Set;

public final class Rank {

    private final int id;
    private final int clanId;
    private String name;
    private int priority;
    private boolean isDefault;
    private String displayName;
    private final Set<ClanPermission> permissions;

    public Rank(int id, int clanId, String name, int priority, Set<ClanPermission> permissions) {
        this(id, clanId, name, priority, permissions, false);
    }

    public Rank(int id, int clanId, String name, int priority, Set<ClanPermission> permissions, boolean isDefault) {
        this.id = id;
        this.clanId = clanId;
        this.name = name;
        this.priority = priority;
        this.isDefault = isDefault;
        this.permissions = EnumSet.noneOf(ClanPermission.class);
        this.permissions.addAll(permissions);
    }

    public int getId() {
        return id;
    }

    public int getClanId() {
        return clanId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Set<ClanPermission> getPermissions() {
        return permissions;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /** Nome mostrado pro jogador; cai pro {@link #getName()} interno se nao houver um customizado. */
    public String getDisplayName() {
        return displayName == null || displayName.isBlank() ? name : displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean has(ClanPermission permission) {
        return permissions.contains(permission);
    }

    public void grant(ClanPermission permission) {
        permissions.add(permission);
    }

    public void revoke(ClanPermission permission) {
        permissions.remove(permission);
    }

    public String serializePermissions() {
        StringBuilder builder = new StringBuilder();
        for (ClanPermission permission : permissions) {
            if (!builder.isEmpty()) {
                builder.append(',');
            }
            builder.append(permission.name());
        }
        return builder.toString();
    }

    public static Set<ClanPermission> deserializePermissions(String raw) {
        Set<ClanPermission> result = EnumSet.noneOf(ClanPermission.class);
        if (raw == null || raw.isBlank()) {
            return result;
        }
        for (String part : raw.split(",")) {
            try {
                result.add(ClanPermission.valueOf(part.trim()));
            } catch (IllegalArgumentException ignored) {
                // permissao desconhecida (versao antiga do plugin), ignora
            }
        }
        return result;
    }
}
