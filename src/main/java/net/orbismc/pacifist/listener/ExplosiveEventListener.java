// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist.listener;

import net.orbismc.pacifist.PacifistService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.jetbrains.annotations.NotNull;

public final class ExplosiveEventListener implements Listener {
    /**
     * Handler to assign an owner to every entity placed by a player.
     * <p>
     * Doing this will cause the player damage handler to detect
     *
     * @param event The event caused by players placing entities.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlaceEntity(final @NotNull EntityPlaceEvent event) {
        PacifistService.setOwnerTag(event.getEntity(), event.getPlayer());
    }

    @EventHandler
    public void onTNTPrime(final @NotNull TNTPrimeEvent event) {
        Entity cause = event.getPrimingEntity();
        if (cause == null) {
            return;
        }

        OfflinePlayer attacker = PacifistService.getAttackingPlayerFromOriginEntity(cause);

        // If the TNT block was primed by something other than a player or projectile, the
        // event is allowed to go through.
        //
        // Note:
        //  * This also captures TNT primed by another block of TNT which was in itself
        //    primed by a player, because the attacker of an explosive can be a player.
        //
        // Known issues:
        //  * Priming a TNT block using redstone is not captured
        //  * Fire spread causing a TNT block to become primed is not captured
        //  * A dispenser firing a burning projectile at a TNT block to prime it is not captured
        if (attacker == null) {
            return;
        }

        // Now, since we have determined the player responsible for igniting the TNT block, the idea
        // is to spawn our own TNTPrimed entity instead and add a tag indicating the owner to it, which
        // we can check later when it explodes.
        event.setCancelled(true);

        // Remove the TNT block.
        Block block = event.getBlock();
        block.setType(Material.AIR);

        // Spawn our own TNTPrimed
        World world = block.getWorld();
        Location target = block.getLocation().add(0.5, 0, 0.5);  // We need to spawn it in the center of the block
        TNTPrimed primed = world.spawn(target, TNTPrimed.class);

        // Tag it with the player igniting it
        PacifistService.setOwnerTag(primed, attacker);
    }

    /**
     * Special damage event handler for assigning an owner to an end crystal being damaged by a player.
     * <p>
     * The purpose of this handler is to determine the attacker for exploding end crystals. To do this, we tag the end
     * crystal with the last player to damage it which we can the retrieve later when the explosion deals damage.
     *
     * @param event The event caused by players damaging end crystals.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEndCrystalDamageByEntity(final @NotNull EntityDamageByEntityEvent event) {
        // We're only interested in end crystals specifically.
        if (!(event.getEntity() instanceof EnderCrystal target)) {
            return;
        }

        var attacker = PacifistService.getAttackingPlayerFromOriginEntity(event.getDamager());

        // If we can't determine an attacking player from this event, then it was a projectile
        // fired by something other than a player. In this case, the event is allowed to go through.
        if (attacker == null) {
            return;
        }

        // Tag the crystal with the player damaging it
        PacifistService.setOwnerTag(target, attacker);
    }
}
