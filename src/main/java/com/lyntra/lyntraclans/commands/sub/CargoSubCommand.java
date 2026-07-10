package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.domain.Rank;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CargoSubCommand extends AbstractClanSubCommand {

    private final Logger logger;

    public CargoSubCommand(ClanServices services, Logger logger) {
        super(services);
        this.logger = logger;
    }

    @Override
    public void execute(Player player, String[] args) {
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();

        if (args.length < 1) {
            listRanks(player, clan);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "criar" -> criar(player, clan, member, args);
            case "remover" -> remover(player, clan, member, args);
            case "permissao" -> permissao(player, clan, member, args);
            case "definir" -> definir(player, clan, member, args);
            case "desatribuir" -> desatribuir(player, clan, member, args);
            case "permissoes" -> permissoes(player, clan, args);
            case "nomeexibicao" -> nomeExibicao(player, clan, member, args);
            default -> listRanks(player, clan);
        }
    }

    private void desatribuir(Player player, Clan clan, ClanMember member, String[] args) {
        if (!requirePermission(player, clan, member, ClanPermission.GERENCIAR_CARGOS)) {
            return;
        }
        if (args.length < 2) {
            usage(player, "cargo-desatribuir-uso");
            return;
        }
        OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
        Optional<ClanMember> targetMemberOptional = services.clanManager().getMember(target.getUniqueId());
        if (targetMemberOptional.isEmpty() || targetMemberOptional.get().getClanId() != clan.getId()) {
            msg(player, "expulsar-nao-e-membro", "jogador", args[1]);
            return;
        }
        Rank defaultRank = clan.getDefaultRank();
        if (defaultRank == null) {
            msg(player, "erro-interno");
            return;
        }
        services.memberManager().setRank(targetMemberOptional.get(), defaultRank);
        msg(player, "cargo-desatribuido", "jogador", args[1], "cargo", defaultRank.getName());
    }

    private void permissoes(Player player, Clan clan, String[] args) {
        if (args.length < 2) {
            player.sendMessage(services.languageManager().get("cargo-permissoes-catalogo-cabecalho"));
            for (ClanPermission permission : ClanPermission.values()) {
                player.sendMessage(services.languageManager().get("cargo-permissoes-catalogo-item",
                        "permissao", permission.name()));
            }
            return;
        }
        Rank rank = findRank(clan, args[1]);
        if (rank == null) {
            msg(player, "cargo-nao-encontrado", "nome", args[1]);
            return;
        }
        if (rank.getPermissions().isEmpty()) {
            msg(player, "cargo-permissoes-vazio", "nome", rank.getDisplayName());
            return;
        }
        player.sendMessage(services.languageManager().get("cargo-permissoes-cabecalho", "nome", rank.getDisplayName()));
        for (ClanPermission permission : rank.getPermissions()) {
            player.sendMessage(services.languageManager().get("cargo-permissoes-item", "permissao", permission.name()));
        }
    }

    private void nomeExibicao(Player player, Clan clan, ClanMember member, String[] args) {
        if (!requirePermission(player, clan, member, ClanPermission.GERENCIAR_CARGOS)) {
            return;
        }
        if (args.length < 3) {
            usage(player, "cargo-nomeexibicao-uso");
            return;
        }
        Rank rank = findRank(clan, args[1]);
        if (rank == null) {
            msg(player, "cargo-nao-encontrado", "nome", args[1]);
            return;
        }
        String displayName = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        rank.setDisplayName(displayName);
        services.rankManager().persist(rank);
        msg(player, "cargo-nomeexibicao-sucesso", "nome", rank.getName(), "exibicao", displayName);
    }

    private void listRanks(Player player, Clan clan) {
        if (clan.getRanks().isEmpty()) {
            msg(player, "cargo-lista-vazia");
            return;
        }
        for (Rank rank : clan.getRanks()) {
            int count = (int) services.clanManager().getMembers(clan.getId()).stream()
                    .filter(m -> m.getRankId() == rank.getId()).count();
            player.sendMessage(services.languageManager().get("cargo-lista-item", "nome", rank.getDisplayName(),
                    "prioridade", String.valueOf(rank.getPriority()), "membros", String.valueOf(count)));
        }
    }

    private void criar(Player player, Clan clan, ClanMember member, String[] args) {
        if (!requirePermission(player, clan, member, ClanPermission.GERENCIAR_CARGOS)) {
            return;
        }
        if (args.length < 2) {
            usage(player, "cargo-criar-uso");
            return;
        }
        String name = args[1];
        if (findRank(clan, name) != null) {
            msg(player, "cargo-ja-existe", "nome", name);
            return;
        }
        try {
            services.rankManager().createRank(clan, name);
            msg(player, "cargo-criado", "nome", name);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao criar cargo", e);
            msg(player, "erro-interno");
        }
    }

    private void remover(Player player, Clan clan, ClanMember member, String[] args) {
        if (!requirePermission(player, clan, member, ClanPermission.GERENCIAR_CARGOS)) {
            return;
        }
        if (args.length < 2) {
            usage(player, "cargo-remover-uso");
            return;
        }
        Rank rank = findRank(clan, args[1]);
        if (rank == null) {
            msg(player, "cargo-nao-encontrado", "nome", args[1]);
            return;
        }
        Rank highest = clan.getHighestRank();
        if ((highest != null && highest.getId() == rank.getId()) || rank.isDefault()) {
            msg(player, "cargo-nao-pode-remover-liderança");
            return;
        }
        try {
            int usageCount = services.rankManager().countMembersUsingRank(rank);
            if (usageCount > 0) {
                msg(player, "cargo-em-uso", "quantidade", String.valueOf(usageCount));
                return;
            }
            services.rankManager().deleteRank(clan, rank);
            msg(player, "cargo-removido", "nome", rank.getName());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao remover cargo", e);
            msg(player, "erro-interno");
        }
    }

    private void permissao(Player player, Clan clan, ClanMember member, String[] args) {
        if (!requirePermission(player, clan, member, ClanPermission.GERENCIAR_CARGOS)) {
            return;
        }
        if (args.length < 4) {
            usage(player, "cargo-permissao-uso");
            return;
        }
        Rank rank = findRank(clan, args[1]);
        if (rank == null) {
            msg(player, "cargo-nao-encontrado", "nome", args[1]);
            return;
        }
        ClanPermission permission;
        try {
            permission = ClanPermission.valueOf(args[3].toUpperCase());
        } catch (IllegalArgumentException e) {
            msg(player, "cargo-permissao-invalida");
            return;
        }
        boolean grant = args[2].equalsIgnoreCase("conceder");
        if (grant) {
            services.rankManager().grantPermission(rank, permission);
            msg(player, "cargo-permissao-concedida", "permissao", permission.name(), "cargo", rank.getName());
        } else {
            services.rankManager().revokePermission(rank, permission);
            msg(player, "cargo-permissao-removida", "permissao", permission.name(), "cargo", rank.getName());
        }
    }

    private void definir(Player player, Clan clan, ClanMember member, String[] args) {
        if (!requirePermission(player, clan, member, ClanPermission.GERENCIAR_CARGOS)) {
            return;
        }
        if (args.length < 3) {
            usage(player, "cargo-definir-uso");
            return;
        }
        OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
        Optional<ClanMember> targetMemberOptional = services.clanManager().getMember(target.getUniqueId());
        if (targetMemberOptional.isEmpty() || targetMemberOptional.get().getClanId() != clan.getId()) {
            msg(player, "expulsar-nao-e-membro", "jogador", args[1]);
            return;
        }
        Rank rank = findRank(clan, args[2]);
        if (rank == null) {
            msg(player, "cargo-nao-encontrado", "nome", args[2]);
            return;
        }
        services.memberManager().setRank(targetMemberOptional.get(), rank);
        msg(player, "cargo-definido", "jogador", args[1], "cargo", rank.getName());
    }

    private Rank findRank(Clan clan, String name) {
        return clan.getRanks().stream().filter(r -> r.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
