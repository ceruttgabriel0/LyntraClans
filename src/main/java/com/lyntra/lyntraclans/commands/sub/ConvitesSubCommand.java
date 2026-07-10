package com.lyntra.lyntraclans.commands.sub;

import com.lyntra.lyntraclans.commands.AbstractClanSubCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.storage.dao.InviteDao;
import org.bukkit.entity.Player;

import java.util.List;

public final class ConvitesSubCommand extends AbstractClanSubCommand {

    public ConvitesSubCommand(ClanServices services) {
        super(services);
    }

    @Override
    public void execute(Player player, String[] args) {
        List<InviteDao.Invite> invites = services.inviteManager().getInvites(player.getUniqueId());
        if (invites.isEmpty()) {
            msg(player, "convites-vazio");
            return;
        }
        msg(player, "convites-cabecalho");
        for (InviteDao.Invite invite : invites) {
            services.clanManager().getClanById(invite.clanId()).ifPresent(clan ->
                    player.sendMessage(services.languageManager().get("convites-item", "tag", clan.getTag(),
                            "nome", clan.getName())));
        }
    }
}
