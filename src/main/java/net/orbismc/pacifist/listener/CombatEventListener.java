// Copyright © 2024. OrbisMC Contributors 
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist.listener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.orbismc.pacifist.PacifistPreferenceService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

// TODO: Players can push other players
// TODO: We can't determine lingering potion throwers rn

public final class CombatEventListener implements Listener {
    public static final EnumSet<Material> DISALLOWED_MATERIALS = EnumSet.of(
            Material.LAVA_BUCKET,
            Material.TNT,
            Material.FIRE
    );

    public static final EnumSet<EntityType> DISALLOWED_ENTITIES = EnumSet.of(
            EntityType.TNT_MINECART,
            EntityType.END_CRYSTAL
    );

    public static final Set<PotionEffectType> HARMFUL_POTIONS = Set.of(
            PotionEffectType.INSTANT_DAMAGE,
            PotionEffectType.POISON,
            PotionEffectType.WEAKNESS,
            PotionEffectType.SLOWNESS
    );

    //TODO xander invert
    public boolean isPvpDisabledPlayerInRadius(Location location, double radius, Player self) {
        var world = location.getWorld();
        if (world == null) {
            return false;
        }

        for (var entity : world.getNearbyEntities(location, radius, radius, radius)) {
            var target = getDamagedPlayerFromOriginEntity(entity);

            // There is no target player when considering the current entity
            if (target == null) {
                continue;
            }

            // The player is allowed to place the blocks if the only entity in the range is themselves
            if (target.equals(self)) {
                continue;
            }

            if (!PacifistPreferenceService.isPvpEnabled(target)) {
                return true;
            }
        }

        return false;
    }

    public boolean doesPotionHaveHarmfulEffect(Collection<PotionEffect> effects) {
        return effects.stream().anyMatch(v -> HARMFUL_POTIONS.contains(v.getType()));
    }

    public boolean isAllowedToPlaceBlock(@NotNull Location location, Player player, Material material) {
        if (!DISALLOWED_MATERIALS.contains(material)) {
            return true;
        }

        var radius = 10;
        return !isPvpDisabledPlayerInRadius(location, radius, player);
    }

    public boolean isAllowedToPlaceEntity(@NotNull Location location, Player player, EntityType entityType) {
        if (!DISALLOWED_ENTITIES.contains(entityType) && Monster.class.isAssignableFrom(entityType.getEntityClass())) {
            return true;
        }

        var radius = 15;
        return !isPvpDisabledPlayerInRadius(location, radius, player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (isAllowedToPlaceBlock(event.getBlock().getLocation(), event.getPlayer(), event.getBucket())) {
            return;
        }

        sendMessageToPlayer(event.getPlayer(), "You can't place this block near PvP-disabled players [/pvp]", ChatColor.RED);

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isAllowedToPlaceBlock(event.getBlock().getLocation(), event.getPlayer(), event.getBlockPlaced().getType())) {
            return;
        }

        sendMessageToPlayer(event.getPlayer(), "You can't place this block near PvP-disabled players [/pvp]", ChatColor.RED);

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        if (isAllowedToPlaceEntity(event.getBlock().getLocation(), event.getPlayer(), event.getEntity().getType())) {
            return;
        }

        sendMessageToPlayer(event.getPlayer(), "You can't place this entity near PvP-disabled players [/pvp]", ChatColor.RED);

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
        var radius = event.getAreaEffectCloud().getRadius();
        var potion = event.getAreaEffectCloud().getBasePotionType();

        if (potion == null) {
            return;
        }

        boolean hasHarmfulEffect = doesPotionHaveHarmfulEffect(potion.getPotionEffects());
        if (!hasHarmfulEffect) {
            return;
        }

        var buffer = 2; // blocks
        if (!isPvpDisabledPlayerInRadius(event.getAreaEffectCloud().getLocation(), radius + buffer, null)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        boolean hasHarmfulEffect = doesPotionHaveHarmfulEffect(event.getPotion().getEffects());
        if (!hasHarmfulEffect) {
            return;
        }

        var attacker = getAttackingPlayerFromOriginEntity(event.getEntity());
        if (attacker == null) {
            return;
        }

        if (!PacifistPreferenceService.isPvpEnabled(attacker)) {
            sendMessageToPlayer(attacker, "You can't throw splash potions near PvP-disabled players [/pvp]", ChatColor.RED);
            event.setCancelled(true);
            return;
        }

        var isAnyPvpDisabledPlayerAffected = false;
        for (var entity : event.getAffectedEntities()) {
            var player = getDamagedPlayerFromOriginEntity(entity);

            if (player == null) {
                continue;
            }

            if (!PacifistPreferenceService.isPvpEnabled(player)) {
                isAnyPvpDisabledPlayerAffected = true;
                break;
            }
        }

        sendMessageToPlayer(attacker, "You can't throw splash potions near PvP-disabled players [/pvp]", ChatColor.RED);
        event.setCancelled(isAnyPvpDisabledPlayerAffected);
    }

    @EventHandler(ignoreCancelled = false)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        var isPlayer = event.getEntity() instanceof Player;
        var isTamed = event.getEntity() instanceof Tameable tameable && tameable.isTamed();

        if (!isPlayer && !isTamed) {
            return;
        }

        // Determine the party doing the damaging
        Player attacker = getAttackingPlayerFromOriginEntity(event.getDamager());
        if (attacker == null) {
            return;
        }

        // Determine the party being damaged
        Player target = getDamagedPlayerFromOriginEntity(event.getEntity());
        if (target == null) {
            return;
        }

        var attackerPVPEnabled = PacifistPreferenceService.isPvpEnabled(attacker);
        var attackerIsPlayerEntity = event.getDamager() instanceof Player;  // because `attacker` is the player attacking 'by proxy'
        var targetPVPEnabled = PacifistPreferenceService.isPvpEnabled(target);

        // If the attacker is the same is the target, allow it.
        if (attacker.equals(target)) {
            return;
        }

        // If both of them have their PvP enabled, let the event pass.
        if (attackerPVPEnabled && targetPVPEnabled) {
            return;
        }

        if (attackerIsPlayerEntity) {
            String message = "";
            if (!attackerPVPEnabled) {
                message = "You can't damage %s because your PvP is disabled [/pvp]".formatted(target.getDisplayName());
            } else {
                message = "You can't damage %s because their PvP is disabled [/pvp]".formatted(target.getDisplayName());
            }

            sendMessageToPlayer(attacker, message, ChatColor.RED);
        }

        event.setCancelled(true);
    }

    /**
     * @param entity The entity from an EntityDamageByEntityEvent
     * @return The player doing the damaging or null of it's not a player
     */
    private @Nullable Player getAttackingPlayerFromOriginEntity(Entity entity) {
        return switch (entity) {
            // Direct player-to-player damage
            case Player player -> player;

            // Player-to-player damage through projectiles
            case Projectile projectile -> {
                ProjectileSource shooter = projectile.getShooter();
                yield shooter instanceof Player player ? player : null;
            }

            // Player-tamed pet damage
            case Tameable tameable -> {
                if (!tameable.isTamed()) {
                    yield null;
                }

                if (!(tameable.getOwner() instanceof Player)) {
                    yield null;
                }

                yield (Player) tameable.getOwner();
            }
            default -> null;
        };
    }

    private @Nullable Player getDamagedPlayerFromOriginEntity(Entity entity) {
        if (entity instanceof Tameable tameable) {
            if (!tameable.isTamed()) {
                return null;
            }

            if (!(tameable.getOwner() instanceof Player player)) {
                return null;
            }

            return player;
        }

        if (entity instanceof Player player) {
            return player;
        }

        return null;
    }

    private void sendMessageToPlayer(Player player, String message, ChatColor color) {
        var text = new TextComponent(message);
        text.setColor(color);
        sendTextToPlayer(player, text);
    }

    private void sendTextToPlayer(Player player, TextComponent text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, text);
    }
}