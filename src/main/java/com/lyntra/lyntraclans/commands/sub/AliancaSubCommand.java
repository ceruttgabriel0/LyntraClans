package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.managers.RelationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AliancaSubCommand extends AbstractClanSubCommand {

    private final Logger logger;

    public AliancaSubCommand(ClanServices services, Logger logger) {
        super(services);
        this.logger = logger;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "alianca-uso");
            return;
        }
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
        if (!requirePermission(player, clan, member, ClanPermission.GERENCIAR_ALIANCA)) {
            return;
        }

        boolean removing = args[0].equalsIgnoreCase("remover");
        if (removing && args.length < 2) {
            usage(player, "alianca-remover-uso");
            return;
        }
        String tagArg = removing ? args[1] : args[0];

        Optional<Clan> targetOptional = services.clanManager().getClanByTagOrName(tagArg);
        if (targetOptional.isEmpty()) {
            msg(player, "clan-nao-encontrado", "clan", tagArg);
            return;
        }
        Clan target = targetOptional.get();
        if (target.getId() == clan.getId()) {
            msg(player, "alianca-mesmo-clan");
            return;
        }

        if (removing) {
            if (!services.relationManager().isAlly(clan.getId(), target.getId())) {
                msg(player, "alianca-nao-e-aliado", "tag", target.getTag());
                return;
            }
            try {
                services.relationManager().removeAlliance(clan, target);
                msg(player, "alianca-removida", "tag", target.getTag(), "nome", target.getName());
                notifyClan(target, "alianca-removida-pelo-outro", "tag", clan.getTag(), "nome", clan.getName());
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Falha ao remover alianca", e);
                msg(player, "erro-interno");
            }
            return;
        }

        if (services.relationManager().isAlly(clan.getId(), target.getId())) {
            msg(player, "alianca-ja-aliado", "tag", target.getTag());
            return;
        }

        try {
            RelationManager.AllianceResult result = services.relationManager().proposeOrAcceptAlliance(clan, target);
            switch (result) {
                case REQUEST_SENT -> {
                    msg(player, "alianca-pedido-enviado", "tag", target.getTag());
                    notifyClan(target, "alianca-pedido-recebido", "tag", clan.getTag(), "nome", clan.getName());
                }
                case ACCEPTED -> {
                    notifyClan(clan, "alianca-sucesso", "tag", target.getTag(), "nome", target.getName());
                    notifyClan(target, "alianca-sucesso", "tag", clan.getTag(), "nome", clan.getName());
                    // Aliar com quem estava em guerra encerra a guerra automaticamente - os dois estados
                    // sao contraditorios (mesma logica que aliar ja limpa rivalidade em RelationManager).
                    if (services.warManager().isAtWar(clan.getId(), target.getId())
                            && services.warManager().endWar(clan, target)) {
                        notifyClan(clan, "guerra-finalizada-anuncio", "tag", target.getTag());
                        notifyClan(target, "guerra-finalizada-anuncio", "tag", clan.getTag());
                    }
                }
                case ALREADY_PENDING -> msg(player, "alianca-ja-pendente", "tag", target.getTag());
                case ALREADY_ALLIED -> msg(player, "alianca-ja-aliado", "tag", target.getTag());
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao processar alianca", e);
            msg(player, "erro-interno");
        }
    }

    private void notifyClan(Clan clan, String key, String... pairs) {
        services.clanManager().getMembers(clan.getId()).forEach(m -> {
            Player online = Bukkit.getPlayer(m.getUuid());
            if (online != null) {
                online.sendMessage(services.languageManager().get(key, pairs));
            }
        });
    }
}
