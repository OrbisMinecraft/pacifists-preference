// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist.listener;

import net.orbismc.pacifist.PacifistMessaging;
import net.orbismc.pacifist.PacifistService;
import net.orbismc.pacifist.config.PacifistConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class BlockEventListener implements Listener {
    private final HashMap<Material, Double> blockedMaterials;

    public BlockEventListener(final @NotNull PacifistConfig config) {
        this.blockedMaterials = config.blockedMaterials;
    }

    public boolean isHarmfulBlock(Material material) {
        return blockedMaterials.containsKey(material);
    }

    private boolean isAllowedToPlaceBlock(Material material, Player player, Location location) {
        if (!isHarmfulBlock(material)) {
            return true;
        }

        var world = location.getWorld();
        if (world == null) {
            return true;
        }

        var radius = blockedMaterials.get(material);
        var nearby = world.getNearbyEntities(location, radius, radius, radius, entity -> entity instanceof Player && !entity.equals(player));

        return nearby.stream().allMatch(entity -> PacifistService.isPvpEnabled(player, (Player) entity));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBucketEmpty(final @NotNull PlayerBucketEmptyEvent event) {
        if (isAllowedToPlaceBlock(event.getBucket(), event.getPlayer(), event.getBlock().getLocation())) {
            return;
        }

        PacifistMessaging.sendGenericDenialMessage(
                event.getPlayer(),
                event.getPlayer(),
                "can't place %s near PvP-disabled players".formatted(event.getBucket().name())
        );

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(final @NotNull BlockPlaceEvent event) {
        var block = event.getBlockPlaced();

        // Hardcoded checks for exploding blocks
        // TODO: Make this nicer please, my brain can't handle it right now
        var world = block.getLocation().getWorld();
        if (event.getBlockPlaced().getBlockData() instanceof Bed) {
            if (world.isBedWorks()) {
                return;
            }

            if (!PacifistService.isPvpDisabledPlayerInRadius(block.getLocation(), blockedMaterials.get(block.getType()), event.getPlayer())) {
                return;
            }

            event.setCancelled(true);
            return;
        } else if (block.getType() == Material.RESPAWN_ANCHOR) {
            if (world.isRespawnAnchorWorks()) {
                return;
            }

            if (!PacifistService.isPvpDisabledPlayerInRadius(block.getLocation(), blockedMaterials.get(block.getType()), event.getPlayer())) {
                return;
            }

            event.setCancelled(true);
            return;
        }

        if (isAllowedToPlaceBlock(block.getType(), event.getPlayer(), event.getBlockPlaced().getLocation())) {
            return;
        }

        PacifistMessaging.sendGenericDenialMessage(
                event.getPlayer(),
                event.getPlayer(),
                "can't place %s near PvP-disabled players".formatted(event.getBlock().getType().name())
        );

        event.setCancelled(true);
    }
}
