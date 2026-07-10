package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.managers.UpgradeManager;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class UpgradesSubCommand extends AbstractClanSubCommand {

    public UpgradesSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            usage(player, "upgrades-uso");
            return;
        }
        Optional<Clan> clanOptional = requireClan(player);
        if (clanOptional.isEmpty()) {
            return;
        }
        Clan clan = clanOptional.get();
        ClanMember member = services.clanManager().getMember(player.getUniqueId()).orElseThrow();
        if (!requirePermission(player, clan, member, ClanPermission.UPGRADES)) {
            return;
        }

        switch (args[0].toLowerCase()) {
            case "membros" -> {
                UpgradeManager.Result result = services.upgradeManager().upgradeMemberSlot(clan);
                switch (result) {
                    case OK -> msg(player, "upgrades-membros-sucesso", "novo", String.valueOf(clan.getMaxMembers()),
                            "custo", services.bankManager().format(services.configManager().memberSlotPrice()));
                    case MAX_REACHED -> msg(player, "upgrades-membros-teto",
                            "max", String.valueOf(services.configManager().absoluteMaxMembers()));
                    case INSUFFICIENT_CLAN_BALANCE -> msg(player, "banco-sem-saldo-clan", "quantia",
                            services.bankManager().format(services.configManager().memberSlotPrice()));
                }
            }
            case "bau" -> {
                UpgradeManager.Result result = services.upgradeManager().upgradeChest(clan);
                switch (result) {
                    case OK -> msg(player, "upgrades-bau-sucesso", "novo", String.valueOf(clan.getChestSize()),
                            "custo", services.bankManager().format(services.configManager().chestSlotPrice()));
                    case INSUFFICIENT_CLAN_BALANCE -> msg(player, "banco-sem-saldo-clan", "quantia",
                            services.bankManager().format(services.configManager().chestSlotPrice()));
                    case MAX_REACHED -> msg(player, "erro-interno");
                }
            }
            default -> usage(player, "upgrades-uso");
        }
    }
}
