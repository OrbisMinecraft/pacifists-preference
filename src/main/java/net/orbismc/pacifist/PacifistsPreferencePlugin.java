// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist;

import net.orbismc.pacifist.commands.PvpCommand;
import net.orbismc.pacifist.config.PacifistPreferenceConfig;
import net.orbismc.pacifist.listener.CombatEventListener;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;

public final class PacifistsPreferencePlugin extends JavaPlugin {
    private PacifistPreferenceConfig config = new PacifistPreferenceConfig();

    @Override
    public void onEnable() {
        try {
            this.saveDefaultConfig();

            final var configFile = new File(this.getDataFolder(), "config.yml");
            this.config = PacifistPreferenceConfig.load(configFile);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Failed to load configuration", e);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PacifistPreferencePlaceholders(this).register();
        }

        Bukkit.getPluginManager().registerEvents(new CombatEventListener(), this);
        this.getCommand("pvp").setExecutor(new PvpCommand());
        getLogger().info("Loaded.");

        if (config.showParticles) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
                for (var player : Bukkit.getOnlinePlayers()) {
                    if (player.isInvisible()) {
                        continue;
                    }

                    if (PacifistPreferenceService.isPvpEnabled(player)) {
                        player.getWorld().spawnParticle(Particle.RAID_OMEN, player.getLocation(), 1, 0.001, 0.001, 0.001, 0.005);
                    }
                }
            }, 0, 3);
        }
    }

    public PacifistPreferenceConfig getConfiguration() {
        return config;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
