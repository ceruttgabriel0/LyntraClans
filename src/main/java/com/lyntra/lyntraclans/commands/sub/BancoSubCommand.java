package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.managers.BankManager;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class BancoSubCommand extends AbstractClanSubCommand {

    public BancoSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "banco-uso");
            return;
        }
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();

        switch (args[0].toLowerCase()) {
            case "saldo" -> {
                if (!requirePermission(player, clan, member, ClanPermission.BANCO_VER_SALDO)) {
                    return;
                }
                msg(player, "banco-saldo", "saldo", services.bankManager().format(clan.getBalance()));
            }
            case "depositar" -> handleDeposit(player, clan, member, args);
            case "sacar" -> handleWithdraw(player, clan, member, args);
            default -> usage(player, "banco-uso");
        }
    }

    private void handleDeposit(Player player, Clan clan, ClanMember member, String[] args) {
        if (!requirePermission(player, clan, member, ClanPermission.BANCO_DEPOSITAR)) {
            return;
        }
        Double amount = parseAmount(player, args);
        if (amount == null) {
            return;
        }
        BankManager.Result result = services.bankManager().deposit(player, clan, amount);
        handleResult(player, result, "banco-depositar-sucesso", amount);
    }

    private void handleWithdraw(Player player, Clan clan, ClanMember member, String[] args) {
        if (!requirePermission(player, clan, member, ClanPermission.BANCO_SACAR)) {
            return;
        }
        Double amount = parseAmount(player, args);
        if (amount == null) {
            return;
        }
        BankManager.Result result = services.bankManager().withdraw(player, clan, amount);
        handleResult(player, result, "banco-sacar-sucesso", amount);
    }

    private Double parseAmount(Player player, String[] args) {
        if (args.length < 2) {
            usage(player, "banco-uso");
            return null;
        }
        try {
            double amount = Double.parseDouble(args[1].replace(',', '.'));
            if (amount <= 0) {
                msg(player, "banco-quantia-invalida");
                return null;
            }
            return amount;
        } catch (NumberFormatException e) {
            msg(player, "banco-quantia-invalida");
            return null;
        }
    }

    private void handleResult(Player player, BankManager.Result result, String successKey, double amount) {
        switch (result) {
            case OK -> msg(player, successKey, "quantia", services.bankManager().format(amount));
            case ECONOMY_UNAVAILABLE -> msg(player, "banco-sem-vault");
            case INVALID_AMOUNT -> msg(player, "banco-quantia-invalida");
            case INSUFFICIENT_PLAYER_BALANCE -> msg(player, "banco-sem-saldo-pessoal", "quantia",
                    services.bankManager().format(amount));
            case INSUFFICIENT_CLAN_BALANCE -> msg(player, "banco-sem-saldo-clan", "quantia",
                    services.bankManager().format(amount));
        }
    }
}
