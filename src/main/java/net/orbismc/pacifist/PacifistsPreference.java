// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist;

import net.orbismc.pacifist.commands.PacifistCommand;
import net.orbismc.pacifist.commands.PacifistReloadCommand;
import net.orbismc.pacifist.config.PacifistConfig;
import net.orbismc.pacifist.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.logging.Logger;

public final class PacifistsPreference extends JavaPlugin {
    public static final String PERMISSION_OTHERS = "pacifistspreference.other";
    public static Logger LOGGER;
    public PacifistConfig config = new PacifistConfig();

    @Override
    public void onEnable() {
        LOGGER = getLogger();

        try {
            this.reload();
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Failed to load configuration", e);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PacifistPlaceholders(this).register();
        }

        Bukkit.getPluginManager().registerEvents(new CombatEventListener(), this);
        Bukkit.getPluginManager().registerEvents(new ExplosiveEventListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProjectileEventListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockEventListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TamingEventListener(), this);

        var command = this.getCommand("pvp");
        Objects.requireNonNull(command);

        var commandHandler = new PacifistCommand(this);
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);

        command = this.getCommand("pvp-reload");
        Objects.requireNonNull(command);
        command.setExecutor(new PacifistReloadCommand(this));

        if (config.showParticles) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, this::spawnParticles, 0, 3);
        }

        LOGGER.info("Loaded.");
    }

    public void reload() throws FileNotFoundException {
        this.saveDefaultConfig();

        final var configFile = new File(this.getDataFolder(), "config.yml");
        this.config = PacifistConfig.load(configFile);
    }

    public void spawnParticles() {
        for (var player : Bukkit.getOnlinePlayers()) {
            if (player.isInvisible() || player.isDead()) {
                continue;
            }

            if (PacifistService.isPvpEnabled(player)) {
                player.getWorld().spawnParticle(Particle.RAID_OMEN, player.getLocation(), 1, 0.001, 0.001, 0.001, 0.005);
            }
        }
    }
}
