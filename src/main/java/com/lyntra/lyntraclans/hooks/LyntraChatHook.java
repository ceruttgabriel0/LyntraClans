package com.lyntra.lyntraclans.hooks;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Ponto de integracao com o LyntraChat (canal de cla via sistema de canais temporarios).
 *
 * <p>Estado atual (2026-07-09): o LyntraChat ainda nao expoe uma API publica pra outros
 * plugins (nem via {@code ServicesManager}, nem via um getter publico de {@code ChannelManager}
 * na classe principal). Por isso esse hook so detecta a presenca do plugin e degrada de
 * forma graciosa, sem quebrar o LyntraClans. Quando o LyntraChat passar a expor
 * {@code getServer().getServicesManager().register(ChannelManager.class, ...)} (ou uma classe
 * de API dedicada), trocar {@link #createClanChannel} pela chamada real.</p>
 */
public final class LyntraChatHook {

    private final JavaPlugin plugin;
    private boolean present;

    public LyntraChatHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        present = plugin.getServer().getPluginManager().getPlugin("LyntraChat") != null;
        if (present) {
            plugin.getLogger().info("LyntraChat detectado. Integracao de canal de cla ainda depende de uma API "
                    + "publica no LyntraChat que nao existe hoje (ver LyntraChatHook.java). Chat de cla vai "
                    + "funcionar via broadcast interno ate isso ser resolvido.");
        }
    }

    public boolean isPresent() {
        return present;
    }

    /**
     * Tenta criar/associar um canal temporario de cla no LyntraChat. Retorna false quando o
     * LyntraChat nao esta presente ou nao expoe API de integracao ainda (fallback gracioso).
     */
    public boolean createClanChannel(String clanTag, String clanName) {
        if (!present) {
            return false;
        }
        try {
            // Integracao real pendente de API publica no LyntraChat, ver javadoc da classe.
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Falha ao integrar canal de cla com o LyntraChat", e);
            return false;
        }
    }
}
