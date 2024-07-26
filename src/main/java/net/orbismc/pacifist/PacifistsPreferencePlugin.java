// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist;

import net.orbismc.pacifist.commands.PvpCommand;
import net.orbismc.pacifist.listener.CombatEventListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class PacifistsPreferencePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new CombatEventListener(), this);
        this.getCommand("pvp").setExecutor(new PvpCommand());
        getLogger().info("Loaded.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
