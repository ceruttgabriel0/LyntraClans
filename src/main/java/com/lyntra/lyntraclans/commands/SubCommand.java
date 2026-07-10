package com.lyntra.lyntraclans.commands;

import org.bukkit.entity.Player;

import java.util.List;

public interface SubCommand {

    void execute(Player player, String[] args);

    default List<String> tabComplete(Player player, String[] args) {
        return List.of();
    }
}
