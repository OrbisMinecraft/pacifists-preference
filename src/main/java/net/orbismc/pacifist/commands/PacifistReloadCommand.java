// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist.commands;

import net.orbismc.pacifist.PacifistMessaging;
import net.orbismc.pacifist.PacifistsPreference;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;

public final class PacifistReloadCommand implements CommandExecutor {
    private final PacifistsPreference plugin;

    public PacifistReloadCommand(final @NotNull PacifistsPreference plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] args) {
        try {
            plugin.reload();
            PacifistMessaging.sendChatMessage(commandSender, "§aPlugin reloaded.");
        } catch (FileNotFoundException e) {
            PacifistMessaging.sendChatMessage(commandSender, "§cPlugin reload failed. Check server logs.");
            throw new RuntimeException(e);
        }
        return true;
    }
}
