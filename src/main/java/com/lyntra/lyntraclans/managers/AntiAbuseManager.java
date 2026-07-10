package com.lyntra.lyntraclans.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Impede o ciclo criar->desfazer->criar repetido (spam de nome/tag, ou abuso de qualquer bonus
 * de criacao). Estado em memoria, nao precisa persistir - reiniciar o servidor "perdoa" o
 * cooldown, o que e aceitavel pra esse tipo de protecao.
 */
public final class AntiAbuseManager {

    private final Map<UUID, Long> lastDisbandAt = new ConcurrentHashMap<>();

    public void recordDisband(UUID player) {
        lastDisbandAt.put(player, System.currentTimeMillis());
    }

    /** Segundos restantes de cooldown, ou 0 se ja pode criar de novo. */
    public long remainingCooldownSeconds(UUID player, long cooldownSeconds) {
        Long last = lastDisbandAt.get(player);
        if (last == null) {
            return 0;
        }
        long elapsedSeconds = (System.currentTimeMillis() - last) / 1000;
        long remaining = cooldownSeconds - elapsedSeconds;
        return Math.max(0, remaining);
    }
}
