package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.hooks.VaultHook;
import org.bukkit.entity.Player;

public final class BankManager {

    public enum Result {
        OK,
        ECONOMY_UNAVAILABLE,
        INVALID_AMOUNT,
        INSUFFICIENT_PLAYER_BALANCE,
        INSUFFICIENT_CLAN_BALANCE
    }

    private final VaultHook vaultHook;
    private final ClanManager clanManager;

    public BankManager(VaultHook vaultHook, ClanManager clanManager) {
        this.vaultHook = vaultHook;
        this.clanManager = clanManager;
    }

    public Result deposit(Player player, Clan clan, double amount) {
        if (!vaultHook.isEnabled()) {
            return Result.ECONOMY_UNAVAILABLE;
        }
        if (amount <= 0) {
            return Result.INVALID_AMOUNT;
        }
        if (!vaultHook.has(player, amount)) {
            return Result.INSUFFICIENT_PLAYER_BALANCE;
        }
        vaultHook.withdraw(player, amount);
        clan.setBalance(clan.getBalance() + amount);
        clanManager.persistClan(clan);
        return Result.OK;
    }

    public Result withdraw(Player player, Clan clan, double amount) {
        if (!vaultHook.isEnabled()) {
            return Result.ECONOMY_UNAVAILABLE;
        }
        if (amount <= 0) {
            return Result.INVALID_AMOUNT;
        }
        if (clan.getBalance() < amount) {
            return Result.INSUFFICIENT_CLAN_BALANCE;
        }
        clan.setBalance(clan.getBalance() - amount);
        clanManager.persistClan(clan);
        vaultHook.deposit(player, amount);
        return Result.OK;
    }

    public boolean debitClan(Clan clan, double amount) {
        if (clan.getBalance() < amount) {
            return false;
        }
        clan.setBalance(clan.getBalance() - amount);
        clanManager.persistClan(clan);
        return true;
    }

    public String format(double amount) {
        return vaultHook.format(amount);
    }
}
