// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PacifistService {
    private static final NamespacedKey PACIFIST_PREFERENCE_SETTING = new NamespacedKey(PacifistsPreference.getPlugin(PacifistsPreference.class), "pvp_enabled");
    private static final NamespacedKey PACIFIST_PREFERENCE_OWNER = new NamespacedKey(PacifistsPreference.getPlugin(PacifistsPreference.class), "owner");

    public static boolean isPvpEnabled(Player player) {
        var container = player.getPersistentDataContainer();
        Boolean setting = container.get(PACIFIST_PREFERENCE_SETTING, PersistentDataType.BOOLEAN);
        return setting != null && setting;
    }

    public static boolean isPvpEnabled(Player playerA, Player playerB) {
        return isPvpEnabled(playerA) && isPvpEnabled(playerB);
    }

    public static void setPvpEnabled(Player player, boolean enabled) {
        var container = player.getPersistentDataContainer();
        container.set(PACIFIST_PREFERENCE_SETTING, PersistentDataType.BOOLEAN, enabled);
    }

    public static void setOwnerTag(Entity entity, Player player) {
        if (entity instanceof Player) {
            throw new RuntimeException("NEVER setting owner tag on a player: " + entity);
        }

        var container = entity.getPersistentDataContainer();
        container.set(PACIFIST_PREFERENCE_OWNER, PersistentDataType.STRING, player.getUniqueId().toString());
    }

    public static @Nullable Player getOwnerTag(@NotNull Entity entity) {
        var container = entity.getPersistentDataContainer();
        var owner = container.get(PACIFIST_PREFERENCE_OWNER, PersistentDataType.STRING);
        return owner != null ? Bukkit.getPlayer(UUID.fromString(owner)) : null;
    }

    /**
     * @param entity The entity from an EntityDamageByEntityEvent
     * @return The player doing the damaging or null of it's not a player
     */
    public static @Nullable Player getAttackingPlayerFromOriginEntity(@NotNull Entity entity) {
        return switch (entity) {
            // Direct player-to-player damage
            case Player player -> player;

            // Player-tamed pet damage, player-lit explosives, player-launched projectiles, ...
            default -> getOwnerTag(entity);
        };
    }

    public static @Nullable Player getDamagedPlayerFromOriginEntity(Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }

        if (entity instanceof Tameable tameable) {
            if (!tameable.isTamed()) {
                return null;
            }

            if (!(tameable.getOwner() instanceof Player player)) {
                return null;
            }

            return player;
        }

        return null;
    }
}
