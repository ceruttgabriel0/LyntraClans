package com.lyntra.lyntraclans.commands;

import com.lyntra.lyntraclans.config.ConfigManager;
import com.lyntra.lyntraclans.config.LanguageManager;
import com.lyntra.lyntraclans.hooks.LyntraChatHook;
import com.lyntra.lyntraclans.hooks.VaultHook;
import com.lyntra.lyntraclans.managers.AntiAbuseManager;
import com.lyntra.lyntraclans.managers.BankManager;
import com.lyntra.lyntraclans.managers.ChatModeManager;
import com.lyntra.lyntraclans.managers.ClanManager;
import com.lyntra.lyntraclans.managers.InviteManager;
import com.lyntra.lyntraclans.managers.KillManager;
import com.lyntra.lyntraclans.managers.MemberManager;
import com.lyntra.lyntraclans.managers.NoticeManager;
import com.lyntra.lyntraclans.managers.PlayerSettingsManager;
import com.lyntra.lyntraclans.managers.RankManager;
import com.lyntra.lyntraclans.managers.RelationManager;
import com.lyntra.lyntraclans.managers.ScoreboardManager;
import com.lyntra.lyntraclans.managers.UpgradeManager;
import com.lyntra.lyntraclans.managers.WarManager;
import com.lyntra.lyntraclans.storage.dao.PlayerDataDao;

import java.util.logging.Logger;

public record ClanServices(
        LanguageManager languageManager,
        ConfigManager configManager,
        ClanManager clanManager,
        RankManager rankManager,
        MemberManager memberManager,
        InviteManager inviteManager,
        RelationManager relationManager,
        BankManager bankManager,
        UpgradeManager upgradeManager,
        KillManager killManager,
        ChatModeManager chatModeManager,
        VaultHook vaultHook,
        LyntraChatHook lyntraChatHook,
        WarManager warManager,
        NoticeManager noticeManager,
        PlayerSettingsManager playerSettingsManager,
        PlayerDataDao playerDataDao,
        Logger logger,
        AntiAbuseManager antiAbuseManager,
        ScoreboardManager scoreboardManager
) {
}
