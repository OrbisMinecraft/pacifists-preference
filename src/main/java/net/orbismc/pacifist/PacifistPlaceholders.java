// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PacifistPlaceholders extends PlaceholderExpansion {
    private final PacifistsPreference plugin;
    private final String placeholderEnabled;
    private final String placeholderDisabled;

    public PacifistPlaceholders(@NotNull PacifistsPreference plugin) {
        this.plugin = plugin;
        this.placeholderDisabled = plugin.getConfiguration().placeholderPvpDisabled;
        this.placeholderEnabled = plugin.getConfiguration().placeholderPvpEnabled;
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getName();
    }

    @Override
    public @NotNull String getAuthor() {
        return "OrbisMC";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (!player.isOnline()) {
            return "";
        }

        var onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            return "";
        }

        return PacifistService.isPvpEnabled(onlinePlayer) ? placeholderEnabled : placeholderDisabled;
    }
}