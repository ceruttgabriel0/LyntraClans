package com.lyntra.lyntraclans.commands;

import com.lyntra.lyntraclans.commands.sub.AceitarSubCommand;
import com.lyntra.lyntraclans.commands.sub.AchatSubCommand;
import com.lyntra.lyntraclans.commands.sub.AdminSubCommand;
import com.lyntra.lyntraclans.commands.sub.AjudaSubCommand;
import com.lyntra.lyntraclans.commands.sub.AliancaSubCommand;
import com.lyntra.lyntraclans.commands.sub.AliancasSubCommand;
import com.lyntra.lyntraclans.commands.sub.AlternarSubCommand;
import com.lyntra.lyntraclans.commands.sub.AvisosSubCommand;
import com.lyntra.lyntraclans.commands.sub.BancoSubCommand;
import com.lyntra.lyntraclans.commands.sub.CargoSubCommand;
import com.lyntra.lyntraclans.commands.sub.ChatSubCommand;
import com.lyntra.lyntraclans.commands.sub.ClanffSubCommand;
import com.lyntra.lyntraclans.commands.sub.ConfiarSubCommand;
import com.lyntra.lyntraclans.commands.sub.ConvidarSubCommand;
import com.lyntra.lyntraclans.commands.sub.ConvitesSubCommand;
import com.lyntra.lyntraclans.commands.sub.CoordenadasSubCommand;
import com.lyntra.lyntraclans.commands.sub.CorSubCommand;
import com.lyntra.lyntraclans.commands.sub.CriarSubCommand;
import com.lyntra.lyntraclans.commands.sub.DescricaoSubCommand;
import com.lyntra.lyntraclans.commands.sub.DesfazerSubCommand;
import com.lyntra.lyntraclans.commands.sub.DetalhesSubCommand;
import com.lyntra.lyntraclans.commands.sub.EstatisticasSubCommand;
import com.lyntra.lyntraclans.commands.sub.ExpulsarSubCommand;
import com.lyntra.lyntraclans.commands.sub.FfSubCommand;
import com.lyntra.lyntraclans.commands.sub.GuerraSubCommand;
import com.lyntra.lyntraclans.commands.sub.HomeSubCommand;
import com.lyntra.lyntraclans.commands.sub.InfoSubCommand;
import com.lyntra.lyntraclans.commands.sub.ListaSubCommand;
import com.lyntra.lyntraclans.commands.sub.ListarSaldoSubCommand;
import com.lyntra.lyntraclans.commands.sub.MembrosSubCommand;
import com.lyntra.lyntraclans.commands.sub.MortesSubCommand;
import com.lyntra.lyntraclans.commands.sub.MudarTagSubCommand;
import com.lyntra.lyntraclans.commands.sub.NegarSubCommand;
import com.lyntra.lyntraclans.commands.sub.PerfilSubCommand;
import com.lyntra.lyntraclans.commands.sub.PromoverSubCommand;
import com.lyntra.lyntraclans.commands.sub.RankingSubCommand;
import com.lyntra.lyntraclans.commands.sub.RebaixarSubCommand;
import com.lyntra.lyntraclans.commands.sub.RivalSubCommand;
import com.lyntra.lyntraclans.commands.sub.RivalidadesSubCommand;
import com.lyntra.lyntraclans.commands.sub.SairSubCommand;
import com.lyntra.lyntraclans.commands.sub.SaudeSubCommand;
import com.lyntra.lyntraclans.commands.sub.SethomeSubCommand;
import com.lyntra.lyntraclans.commands.sub.TransferirSubCommand;
import com.lyntra.lyntraclans.commands.sub.UpgradesSubCommand;
import com.lyntra.lyntraclans.gui.MainMenuFrame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class ClanCommand implements CommandExecutor, TabCompleter {

    private final ClanServices services;
    private final MainMenuFrame mainMenuFrame;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    public ClanCommand(ClanServices services, MainMenuFrame mainMenuFrame, Logger logger) {
        this.services = services;
        this.mainMenuFrame = mainMenuFrame;

        subCommands.put("criar", new CriarSubCommand(services, logger));
        DesfazerSubCommand desfazerSubCommand = new DesfazerSubCommand(services, logger);
        subCommands.put("desfazer", desfazerSubCommand);
        subCommands.put("debandar", desfazerSubCommand);
        SairSubCommand sairSubCommand = new SairSubCommand(services, logger);
        subCommands.put("sair", sairSubCommand);
        subCommands.put("abandonar", sairSubCommand);
        subCommands.put("convidar", new ConvidarSubCommand(services));
        subCommands.put("convites", new ConvitesSubCommand(services));
        subCommands.put("aceitar", new AceitarSubCommand(services, logger));
        subCommands.put("negar", new NegarSubCommand(services));
        subCommands.put("expulsar", new ExpulsarSubCommand(services));
        subCommands.put("transferir", new TransferirSubCommand(services));
        subCommands.put("info", new InfoSubCommand(services));
        subCommands.put("perfil", new PerfilSubCommand(services, services.playerDataDao(), logger));
        subCommands.put("lista", new ListaSubCommand(services));
        subCommands.put("listar", new ListaSubCommand(services));
        subCommands.put("ranking", new RankingSubCommand(services));
        subCommands.put("banco", new BancoSubCommand(services));
        subCommands.put("alianca", new AliancaSubCommand(services, logger));
        subCommands.put("rival", new RivalSubCommand(services, logger));
        subCommands.put("upgrades", new UpgradesSubCommand(services));
        subCommands.put("cargo", new CargoSubCommand(services, logger));
        subCommands.put("home", new HomeSubCommand(services));
        subCommands.put("sethome", new SethomeSubCommand(services));
        subCommands.put("cor", new CorSubCommand(services));
        subCommands.put("descricao", new DescricaoSubCommand(services));
        subCommands.put("chat", new ChatSubCommand(services));
        subCommands.put("achat", new AchatSubCommand(services));
        subCommands.put("ajuda", new AjudaSubCommand(services));
        subCommands.put("help", new AjudaSubCommand(services));
        subCommands.put("admin", new AdminSubCommand(services, logger));
        subCommands.put("confiar", new ConfiarSubCommand(services, true));
        subCommands.put("naoconfiar", new ConfiarSubCommand(services, false));
        subCommands.put("promover", new PromoverSubCommand(services));
        subCommands.put("rebaixar", new RebaixarSubCommand(services));
        subCommands.put("membros", new MembrosSubCommand(services));
        subCommands.put("estatisticas", new EstatisticasSubCommand(services));
        subCommands.put("mortes", new MortesSubCommand(services));
        subCommands.put("detalhes", new DetalhesSubCommand(services));
        subCommands.put("aliancas", new AliancasSubCommand(services));
        subCommands.put("rivalidades", new RivalidadesSubCommand(services));
        subCommands.put("coordenadas", new CoordenadasSubCommand(services));
        subCommands.put("saude", new SaudeSubCommand(services));
        subCommands.put("listarsaldo", new ListarSaldoSubCommand(services));
        subCommands.put("avisos", new AvisosSubCommand(services));
        subCommands.put("alternar", new AlternarSubCommand(services));
        subCommands.put("ff", new FfSubCommand(services));
        subCommands.put("clanff", new ClanffSubCommand(services));
        subCommands.put("guerra", new GuerraSubCommand(services));
        subCommands.put("mudartag", new MudarTagSubCommand(services));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Somente jogadores podem usar este comando.");
            return true;
        }
        if (args.length == 0) {
            mainMenuFrame.open(player);
            return true;
        }
        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand == null) {
            player.sendMessage(services.languageManager().get("comando-desconhecido"));
            return true;
        }
        String[] rest = args.length > 1 ? java.util.Arrays.copyOfRange(args, 1, args.length) : new String[0];
        subCommand.execute(player, rest);
        return true;
    }

    private static final java.util.Set<String> PLAYER_ARG_COMMANDS = java.util.Set.of(
            "convidar", "expulsar", "promover", "rebaixar", "transferir", "confiar", "naoconfiar",
            "detalhes", "mortes");
    private static final java.util.Set<String> CLAN_TAG_ARG_COMMANDS = java.util.Set.of(
            "info", "perfil", "membros");
    private static final java.util.Set<String> CONFIRM_ARG_COMMANDS = java.util.Set.of(
            "sair", "abandonar", "desfazer", "debandar");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> result = new ArrayList<>();
            for (String key : subCommands.keySet()) {
                if (key.startsWith(prefix)) {
                    result.add(key);
                }
            }
            return result;
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String prefix = args[1].toLowerCase();
            if (PLAYER_ARG_COMMANDS.contains(sub)) {
                return matchingOnlinePlayers(prefix);
            }
            if (CLAN_TAG_ARG_COMMANDS.contains(sub)) {
                return matchingClanTags(prefix);
            }
            if (CONFIRM_ARG_COMMANDS.contains(sub)) {
                return "confirmar".startsWith(prefix) ? List.of("confirmar") : List.of();
            }
            if (sub.equals("rival") || sub.equals("alianca")) {
                List<String> options = new ArrayList<>();
                if ("remover".startsWith(prefix)) {
                    options.add("remover");
                }
                options.addAll(matchingClanTags(prefix));
                return options;
            }
            if (sub.equals("guerra")) {
                return java.util.stream.Stream.of("iniciar", "finalizar")
                        .filter(s -> s.startsWith(prefix)).toList();
            }
            if (sub.equals("cargo")) {
                return java.util.stream.Stream.of("criar", "remover", "permissao", "definir", "desatribuir",
                                "permissoes", "nomeexibicao")
                        .filter(s -> s.startsWith(prefix)).toList();
            }
            if (sub.equals("ff")) {
                return java.util.stream.Stream.of("auto", "permitir", "bloquear").filter(s -> s.startsWith(prefix)).toList();
            }
            if (sub.equals("clanff")) {
                return java.util.stream.Stream.of("permitir", "bloquear").filter(s -> s.startsWith(prefix)).toList();
            }
            if (sub.equals("alternar")) {
                return java.util.stream.Stream.of("convidar", "avisos", "tag", "sidebar").filter(s -> s.startsWith(prefix)).toList();
            }
            if (sub.equals("banco")) {
                return java.util.stream.Stream.of("saldo", "depositar", "sacar").filter(s -> s.startsWith(prefix)).toList();
            }
            if (sub.equals("upgrades")) {
                return java.util.stream.Stream.of("membros", "bau").filter(s -> s.startsWith(prefix)).toList();
            }
            if (sub.equals("listar") || sub.equals("lista")) {
                return "ordem".startsWith(prefix) ? List.of("ordem") : List.of();
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("cargo")
                && (args[1].equalsIgnoreCase("definir") || args[1].equalsIgnoreCase("desatribuir"))) {
            return matchingOnlinePlayers(args[2].toLowerCase());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("listar") && args[1].equalsIgnoreCase("ordem")) {
            String prefix = args[2].toLowerCase();
            return java.util.stream.Stream.of("nome", "membros", "kdr").filter(s -> s.startsWith(prefix)).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("guerra")
                && (args[1].equalsIgnoreCase("iniciar") || args[1].equalsIgnoreCase("finalizar"))) {
            return matchingClanTags(args[2].toLowerCase());
        }
        return List.of();
    }

    private List<String> matchingOnlinePlayers(String prefix) {
        List<String> result = new ArrayList<>();
        for (org.bukkit.entity.Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (online.getName().toLowerCase().startsWith(prefix)) {
                result.add(online.getName());
            }
        }
        return result;
    }

    private List<String> matchingClanTags(String prefix) {
        List<String> result = new ArrayList<>();
        for (var clan : services.clanManager().getAllClans()) {
            if (clan.getTag().toLowerCase().startsWith(prefix)) {
                result.add(clan.getTag());
            }
        }
        return result;
    }
}
