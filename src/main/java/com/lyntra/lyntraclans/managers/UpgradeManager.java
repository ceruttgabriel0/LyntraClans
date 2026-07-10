package com.lyntra.lyntraclans.managers;

import com.lyntra.lyntraclans.config.ConfigManager;
import com.lyntra.lyntraclans.domain.Clan;

public final class UpgradeManager {

    public enum Result {
        OK,
        MAX_REACHED,
        INSUFFICIENT_CLAN_BALANCE
    }

    private final ConfigManager configManager;
    private final ClanManager clanManager;
    private final BankManager bankManager;

    public UpgradeManager(ConfigManager configManager, ClanManager clanManager, BankManager bankManager) {
        this.configManager = configManager;
        this.clanManager = clanManager;
        this.bankManager = bankManager;
    }

    public Result upgradeMemberSlot(Clan clan) {
        if (clan.getMaxMembers() >= configManager.absoluteMaxMembers()) {
            return Result.MAX_REACHED;
        }
        double price = configManager.memberSlotPrice();
        if (!bankManager.debitClan(clan, price)) {
            return Result.INSUFFICIENT_CLAN_BALANCE;
        }
        clan.setMaxMembers(clan.getMaxMembers() + 1);
        clanManager.persistClan(clan);
        return Result.OK;
    }

    public Result upgradeChest(Clan clan) {
        if (clan.getChestSize() >= configManager.chestSlotMax()) {
            return Result.MAX_REACHED;
        }
        double price = configManager.chestSlotPrice();
        if (!bankManager.debitClan(clan, price)) {
            return Result.INSUFFICIENT_CLAN_BALANCE;
        }
        int newSize = Math.min(clan.getChestSize() + configManager.chestSlotUpgradeSize(),
                configManager.chestSlotMax());
        clan.setChestSize(newSize);
        clanManager.persistClan(clan);
        return Result.OK;
    }
}
