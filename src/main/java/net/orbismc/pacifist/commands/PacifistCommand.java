// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist.commands;

import net.orbismc.pacifist.PacifistService;
import net.orbismc.pacifist.PacifistsPreference;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public final class PacifistCommand implements CommandExecutor, TabCompleter {
    private final PacifistsPreference plugin;

    public PacifistCommand(final @NotNull PacifistsPreference plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("The console cannot execute this command.");
            return true;
        }

        boolean toggle = !PacifistService.isPvpEnabled(player);
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("on")) {
                toggle = true;
            } else if (args[0].equalsIgnoreCase("off")) {
                toggle = false;
            }
        }

        PacifistService.setPvpEnabled(player, toggle);

        player.sendMessage(ChatColor.RED + " PvP is now " + (toggle ? ChatColor.RED + "" + ChatColor.BOLD + "ON" : ChatColor.GREEN + "" + ChatColor.BOLD + "OFF"));

        if (plugin.getConfiguration().showGlowing) {
            player.setGlowing(toggle);
        }
        return true;
    }

    @Override
    public @Unmodifiable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return List.of("on", "off");
        }

        return List.of();
    }
}
