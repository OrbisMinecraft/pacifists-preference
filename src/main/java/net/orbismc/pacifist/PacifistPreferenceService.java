// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class PacifistPreferenceService {
    private static final NamespacedKey PACIFIST_PREFERENCE_SETTING = new NamespacedKey(PacifistsPreferencePlugin.getPlugin(PacifistsPreferencePlugin.class), "pvp_enabled");

    public static boolean isPvpEnabled(Player player) {
        var container = player.getPersistentDataContainer();
        Boolean setting = container.get(PACIFIST_PREFERENCE_SETTING, PersistentDataType.BOOLEAN);
        return setting == null ? false : setting;
    }

    public static void setPvpEnabled(Player player, boolean enabled) {
        var container = player.getPersistentDataContainer();
        container.set(PACIFIST_PREFERENCE_SETTING, PersistentDataType.BOOLEAN, enabled);
    }
}
