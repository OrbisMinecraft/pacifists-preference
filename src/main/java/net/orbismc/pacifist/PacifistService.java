// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PacifistService {
    private static final NamespacedKey PACIFIST_PREFERENCE_SETTING = new NamespacedKey(PacifistsPreference.getPlugin(PacifistsPreference.class), "pvp_enabled");
    private static final NamespacedKey PACIFIST_PREFERENCE_OWNER = new NamespacedKey(PacifistsPreference.getPlugin(PacifistsPreference.class), "owner");

    public static boolean isPvpEnabled(OfflinePlayer player) {
        if (!player.isOnline()) {
            return false;
        }

        var onlinePlayer = player.getPlayer();
        var container = onlinePlayer.getPersistentDataContainer();
        Boolean setting = container.get(PACIFIST_PREFERENCE_SETTING, PersistentDataType.BOOLEAN);
        return setting != null && setting;
    }

    public static boolean isPvpEnabled(OfflinePlayer playerA, OfflinePlayer playerB) {
        return isPvpEnabled(playerA) && isPvpEnabled(playerB);
    }

    public static void setPvpEnabled(Player player, boolean enabled) {
        var container = player.getPersistentDataContainer();
        container.set(PACIFIST_PREFERENCE_SETTING, PersistentDataType.BOOLEAN, enabled);
    }

    public static void setOwnerTag(PersistentDataHolder entity, OfflinePlayer player) {
        if (!player.isOnline()) {
            return;
        }

        if (entity instanceof OfflinePlayer) {
            throw new RuntimeException("NEVER setting owner tag on a player: " + entity);
        }

        var container = entity.getPersistentDataContainer();
        container.set(PACIFIST_PREFERENCE_OWNER, PersistentDataType.STRING, player.getUniqueId().toString());
    }

    public static void setOwnerTag(Block block, OfflinePlayer player) {
        if (!player.isOnline()) {
            return;
        }

        var plugin = PacifistsPreference.getPlugin(PacifistsPreference.class);
        block.setMetadata(PACIFIST_PREFERENCE_OWNER.toString(), new FixedMetadataValue(plugin, player.getUniqueId().toString()));
    }

    public static @Nullable OfflinePlayer getOwnerTag(@NotNull PersistentDataHolder entity) {
        var container = entity.getPersistentDataContainer();
        var owner = container.get(PACIFIST_PREFERENCE_OWNER, PersistentDataType.STRING);
        return owner != null ? Bukkit.getPlayer(UUID.fromString(owner)) : null;
    }

    public static @Nullable OfflinePlayer getOwnerTag(@NotNull Block block) {
        if (!block.hasMetadata(PACIFIST_PREFERENCE_OWNER.toString())) {
            return null;
        }

        String uuid = block.getMetadata(PACIFIST_PREFERENCE_OWNER.toString()).getFirst().asString();
        return Bukkit.getOfflinePlayer(UUID.fromString(uuid));
    }

    public static boolean isPvpDisabledPlayerInRadius(@NotNull Location loc, double radius, OfflinePlayer self) {
        return loc.getWorld().getNearbyEntities(loc, radius, radius, radius).stream().filter(entity -> !entity.equals(self)).anyMatch(entity -> entity instanceof Player player && !isPvpEnabled(player, self));
    }

    /**
     * @param entity The entity from an EntityDamageByEntityEvent
     * @return The player doing the damaging or null of it's not a player
     */
    public static @Nullable OfflinePlayer getAttackingPlayerFromOriginEntity(@NotNull Entity entity) {
        return switch (entity) {
            // Direct player-to-player damage
            case Player player -> player;

            // Player-tamed pet damage, player-lit explosives, player-launched projectiles, ...
            default -> getOwnerTag(entity);
        };
    }

    public static @Nullable OfflinePlayer getDamagedPlayerFromOriginEntity(Entity entity) {
        if (entity instanceof OfflinePlayer player) {
            return player;
        }

        if (entity instanceof Tameable tameable) {
            if (!tameable.isTamed()) {
                return null;
            }

            if (!(tameable.getOwner() instanceof OfflinePlayer player)) {
                return null;
            }

            return player;
        }

        return null;
    }
}
