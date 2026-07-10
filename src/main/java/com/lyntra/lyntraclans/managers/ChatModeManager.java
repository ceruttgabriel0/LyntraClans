package com.lyntra.lyntraclans.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ChatModeManager {

    public enum Mode {
        NORMAL,
        CLAN,
        ALIANCA
    }

    private final Map<UUID, Mode> modes = new HashMap<>();

    public Mode getMode(UUID uuid) {
        return modes.getOrDefault(uuid, Mode.NORMAL);
    }

    public void setMode(UUID uuid, Mode mode) {
        if (mode == Mode.NORMAL) {
            modes.remove(uuid);
        } else {
            modes.put(uuid, mode);
        }
    }

    public void clear(UUID uuid) {
        modes.remove(uuid);
    }
}
