package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.config.ConfigManager;
import com.lyntra.lyntraclans.domain.Clan;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CriarSubCommand extends AbstractClanSubCommand {

    private final Logger logger;

    public CriarSubCommand(ClanServices services, Logger logger) {
        super(services);
        this.logger = logger;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            usage(player, "criar-uso");
            return;
        }
        if (!requireNoClan(player)) {
            return;
        }
        String tag = args[0];
        String name = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        ConfigManager config = services.configManager();

        if (tag.length() < config.tagMinLength() || tag.length() > config.tagMaxLength()) {
            msg(player, "criar-tag-tamanho", "min", String.valueOf(config.tagMinLength()),
                    "max", String.valueOf(config.tagMaxLength()));
            return;
        }
        if (!tag.matches("[a-zA-Z0-9]+")) {
            msg(player, "criar-tag-caracteres");
            return;
        }
        if (name.length() < config.nameMinLength() || name.length() > config.nameMaxLength()) {
            msg(player, "criar-nome-tamanho", "min", String.valueOf(config.nameMinLength()),
                    "max", String.valueOf(config.nameMaxLength()));
            return;
        }
        if (services.clanManager().tagInUse(tag)) {
            msg(player, "criar-tag-em-uso", "tag", tag);
            return;
        }
        if (services.clanManager().nameInUse(name)) {
            msg(player, "criar-nome-em-uso", "nome", name);
            return;
        }
        if (config.requireCoinsToCreate() && services.vaultHook().isEnabled()) {
            double cost = config.creationCost();
            if (!services.vaultHook().has(player, cost)) {
                msg(player, "criar-sem-coins", "custo", services.bankManager().format(cost));
                return;
            }
            services.vaultHook().withdraw(player, cost);
        }

        try {
            Clan clan = services.clanManager().createClan(tag, name, player.getUniqueId());
            msg(player, "criar-sucesso", "tag", clan.getTag(), "nome", clan.getName());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao criar cla", e);
            msg(player, "erro-interno");
        }
    }
}
