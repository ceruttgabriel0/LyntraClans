package com.lyntra.lyntraclans.managers;

import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Garante que todo mundo que abre o bau do mesmo cla ao mesmo tempo compartilha o MESMO objeto
 * Inventory (igual um bau de verdade no mundo) em vez de cada abertura criar uma copia
 * independente deserializada do banco. Sem isso, dois jogadores guardando itens diferentes ao
 * mesmo tempo faziam o segundo fechamento sobrescrever silenciosamente o que o primeiro tinha
 * guardado (last-write-wins) - bug real reproduzido com dois bots guardando itens diferentes
 * simultaneamente, um deles sumia.
 */
public final class ClanChestRegistry {

    private final Map<Integer, Inventory> openInventories = new HashMap<>();

    public Inventory getOrCreate(int clanId, Supplier<Inventory> factory) {
        return openInventories.computeIfAbsent(clanId, id -> factory.get());
    }

    /** Chamado depois que um viewer fecha - se ninguem mais estiver olhando, libera pra proxima abertura recarregar do banco. */
    public void evictIfEmpty(int clanId, Inventory inventory) {
        Inventory tracked = openInventories.get(clanId);
        if (tracked == inventory && inventory.getViewers().isEmpty()) {
            openInventories.remove(clanId);
        }
    }
}
