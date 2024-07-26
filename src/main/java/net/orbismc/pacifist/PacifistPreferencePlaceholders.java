package net.orbismc.pacifist;

import com.sun.source.tree.BreakTree;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PacifistPreferencePlaceholders extends PlaceholderExpansion {
	private final PacifistsPreferencePlugin plugin;

	public PacifistPreferencePlaceholders(PacifistsPreferencePlugin plugin) {
		this.plugin = plugin;
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
		return switch (params) {
			case "pvp_enabled" -> {
				if (!player.isOnline()) {
					yield "???";
				}

				yield PacifistPreferenceService.isPvpEnabled(player.getPlayer()) ? "yes" : "no";
			}
			case "pvp_enabled_marker" -> {
				if (!player.isOnline()) {
					yield "?";
				}

				yield PacifistPreferenceService.isPvpEnabled(player.getPlayer()) ? "*" : "";
			}
			default -> params;
		};
	}
}
