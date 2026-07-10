/*
 * LyntraClans
 * Autor: zNeoaK_ (Gabriel Cerutt)
 * GitHub: https://github.com/ceruttgabriel0
 * Discord: gabrielneoak
 * Discord (server): https://discord.gg/yEyJAhPM2b
 * Site: https://lyntra.com.br
 */
package com.lyntra.lyntraclans;

import com.lyntra.lyntraclans.commands.ClanCommand;
import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.config.ConfigManager;
import com.lyntra.lyntraclans.config.LanguageManager;
import com.lyntra.lyntraclans.gui.AnvilInputListener;
import com.lyntra.lyntraclans.gui.ChestCloseListener;
import com.lyntra.lyntraclans.gui.FrameListener;
import com.lyntra.lyntraclans.gui.MainMenuFrame;
import com.lyntra.lyntraclans.hooks.LyntraChatHook;
import com.lyntra.lyntraclans.hooks.PlaceholderApiHook;
import com.lyntra.lyntraclans.hooks.VaultHook;
import com.lyntra.lyntraclans.listeners.ClanChatListener;
import com.lyntra.lyntraclans.listeners.DamageListener;
import com.lyntra.lyntraclans.listeners.JoinListener;
import com.lyntra.lyntraclans.listeners.PlayerDeathListener;
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
import com.lyntra.lyntraclans.managers.UpgradeManager;
import com.lyntra.lyntraclans.managers.WarManager;
import com.lyntra.lyntraclans.storage.DatabaseManager;
import com.lyntra.lyntraclans.storage.dao.ClanDao;
import com.lyntra.lyntraclans.storage.dao.InviteDao;
import com.lyntra.lyntraclans.storage.dao.MemberDao;
import com.lyntra.lyntraclans.storage.dao.NoticeDao;
import com.lyntra.lyntraclans.storage.dao.PlayerDataDao;
import com.lyntra.lyntraclans.storage.dao.RankDao;
import com.lyntra.lyntraclans.storage.dao.RelationDao;
import com.lyntra.lyntraclans.storage.dao.WarDao;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class LyntraClans extends JavaPlugin implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private ConfigManager configManager;
    private LanguageManager languageManager;
    private DatabaseManager databaseManager;
    private VaultHook vaultHook;
    private PlaceholderApiHook placeholderApiHook;
    private LyntraChatHook lyntraChatHook;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        languageManager = new LanguageManager(this);
        configManager.load();
        languageManager.load(configManager.language());

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        ClanDao clanDao = new ClanDao(databaseManager);
        RankDao rankDao = new RankDao(databaseManager);
        MemberDao memberDao = new MemberDao(databaseManager);
        RelationDao relationDao = new RelationDao(databaseManager);
        InviteDao inviteDao = new InviteDao(databaseManager);
        PlayerDataDao playerDataDao = new PlayerDataDao(databaseManager);
        WarDao warDao = new WarDao(databaseManager);
        NoticeDao noticeDao = new NoticeDao(databaseManager);

        ClanManager clanManager = new ClanManager(getLogger(), configManager, clanDao, rankDao, memberDao);
        clanManager.loadAll();

        RankManager rankManager = new RankManager(getLogger(), rankDao);
        MemberManager memberManager = new MemberManager(getLogger(), clanManager, playerDataDao);
        InviteManager inviteManager = new InviteManager(getLogger(), inviteDao);
        RelationManager relationManager = new RelationManager(getLogger(), relationDao);
        vaultHook = new VaultHook(this);
        BankManager bankManager = new BankManager(vaultHook, clanManager);
        UpgradeManager upgradeManager = new UpgradeManager(configManager, clanManager, bankManager);
        PlayerSettingsManager playerSettingsManager = new PlayerSettingsManager(getLogger(), playerDataDao);
        WarManager warManager = new WarManager(getLogger(), warDao);
        KillManager killManager = new KillManager(configManager, clanManager, relationManager,
                playerSettingsManager, warManager, languageManager);
        ChatModeManager chatModeManager = new ChatModeManager();
        NoticeManager noticeManager = new NoticeManager(getLogger(), noticeDao);
        AntiAbuseManager antiAbuseManager = new AntiAbuseManager();
        lyntraChatHook = new LyntraChatHook(this);
        placeholderApiHook = new PlaceholderApiHook(this, clanManager, killManager, bankManager);

        vaultHook.setup();
        placeholderApiHook.setup();
        lyntraChatHook.setup();

        ClanServices services = new ClanServices(languageManager, configManager, clanManager, rankManager,
                memberManager, inviteManager, relationManager, bankManager, upgradeManager, killManager,
                chatModeManager, vaultHook, lyntraChatHook, warManager, noticeManager, playerSettingsManager,
                playerDataDao, getLogger(), antiAbuseManager);

        MainMenuFrame mainMenuFrame = new MainMenuFrame(services);
        ClanCommand clanCommand = new ClanCommand(services, mainMenuFrame, getLogger());
        getCommand("clan").setExecutor(clanCommand);
        getCommand("clan").setTabCompleter(clanCommand);

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new FrameListener(), this);
        getServer().getPluginManager().registerEvents(new AnvilInputListener(), this);
        getServer().getPluginManager().registerEvents(new ChestCloseListener(), this);
        getServer().getPluginManager().registerEvents(
                new ClanChatListener(clanManager, chatModeManager, relationManager, languageManager), this);
        getServer().getPluginManager().registerEvents(new DamageListener(clanManager, killManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(clanManager, killManager), this);
        getServer().getPluginManager().registerEvents(
                new JoinListener(clanManager, noticeManager, playerSettingsManager, languageManager), this);

        getComponentLogger().info(startupBanner());
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (event.getProvider().getService().equals(Economy.class)) {
            vaultHook.setup();
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        String name = event.getPlugin().getName();
        if (name.equals("Vault")) {
            vaultHook.setup();
        } else if (name.equals("PlaceholderAPI")) {
            placeholderApiHook.setup();
        } else if (name.equals("LyntraChat")) {
            lyntraChatHook.setup();
        }
    }

    private Component startupBanner() {
        return MM.deserialize(
                """
                <gray>[<gold>LyntraClans</gold>] <white>habilitado.
                <gray>GitHub: <aqua>https://github.com/ceruttgabriel0
                <gray>Discord: <aqua>gabrielneoak <gray>| <aqua>https://discord.gg/yEyJAhPM2b
                <gray>Site: <aqua>https://lyntra.com.br"""
        );
    }
}
