// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist.listener;

import net.orbismc.pacifist.PacifistService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.jetbrains.annotations.NotNull;

public final class TamingEventListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onEntityTame(final @NotNull EntityTameEvent event) {
        // If the animal is not tamed by a player, ignore this event
        if (!(event.getOwner() instanceof Player player)) {
            return;
        }

        PacifistService.setOwnerTag(event.getEntity(), player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent event) {
        // Breeding two animals makes your their offspring owner
        if (!(event.getBreeder() instanceof Player player)) {
            return;
        }

        PacifistService.setOwnerTag(event.getEntity(), player);
    }
}
