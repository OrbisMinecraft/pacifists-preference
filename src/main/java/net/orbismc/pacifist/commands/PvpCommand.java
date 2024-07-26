// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist.commands;

import net.orbismc.pacifist.PacifistPreferencePlaceholders;
import net.orbismc.pacifist.PacifistPreferenceService;
import net.orbismc.pacifist.PacifistsPreferencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("The console cannot execute this command.");
            return true;
        }

        boolean toggle = !PacifistPreferenceService.isPvpEnabled(player);
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("on")) {
                toggle = true;
            } else if (args[0].equalsIgnoreCase("off")) {
                toggle = false;
            }
        }

        PacifistPreferenceService.setPvpEnabled(player, toggle);

        player.sendMessage(ChatColor.RED + "[Pacifist's Preference]" + ChatColor.RESET + " PvP is now " + (toggle ? ChatColor.RED + "ON" : ChatColor.GREEN + "OFF"));

        if (JavaPlugin.getPlugin(PacifistsPreferencePlugin.class).getConfiguration().showGlowing) {
            player.setGlowing(toggle);
        }
        return true;
    }
}
