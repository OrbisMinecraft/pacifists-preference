// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist.commands;

import net.orbismc.pacifist.PacifistMessaging;
import net.orbismc.pacifist.PacifistService;
import net.orbismc.pacifist.PacifistsPreference;
import org.bukkit.Bukkit;
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

        var canChangeOtherState = commandSender.hasPermission(PacifistsPreference.PERMISSION_OTHERS);
        if (args.length == 2 && canChangeOtherState) {
            player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                PacifistMessaging.sendChatMessage(commandSender, "§cPlayer not found");
                return true;
            }

            player.sendMessage("§cPvP has been " + (toggle ? "§c§lENABLED" : "§a§lDISABLED") + " §r§cby a moderator");
        } else if (args.length == 2) {
            player.sendMessage("§cYou do not have permission to change another player's PvP state");
            return true;
        }

        PacifistService.setPvpEnabled(player, toggle);
        commandSender.sendMessage("§cPvP is now " + (toggle ? "§c§lENABLED" : "§a§lDISABLED") + " §r§cfor " + player.getDisplayName());

        if (plugin.config.showGlowing) {
            player.setGlowing(toggle);
        }
        return true;
    }

    @Override
    public @Unmodifiable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return List.of("on", "off");
        }

        if (args.length == 2 && sender.hasPermission(PacifistsPreference.PERMISSION_OTHERS)) {
            // Auto-complete player names
            return null;
        }

        return List.of();
    }
}
