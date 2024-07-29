// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist.listener;

import net.orbismc.pacifist.PacifistMessaging;
import net.orbismc.pacifist.PacifistService;
import net.orbismc.pacifist.PacifistsPreference;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class ProjectileEventListener implements Listener {
    private final PacifistsPreference plugin;

    public ProjectileEventListener(final @NotNull PacifistsPreference plugin) {
        this.plugin = plugin;
    }

    public static Collection<LivingEntity> applyAreaOfEffect(final @NotNull OfflinePlayer attacker, final @NotNull Collection<LivingEntity> affected) {
        for (var iterator = affected.iterator(); iterator.hasNext(); ) {
            var target = PacifistService.getDamagedPlayerFromOriginEntity(iterator.next());
            if (target == null) {
                continue;
            }

            if (target.equals(attacker)) {
                continue;
            }

            // If PvP is not enabled between the player who threw the potion and the player being affected by it,
            // remove the target player from the affected list.
            if (!PacifistService.isPvpEnabled(attacker, target)) {
                iterator.remove();
            }
        }

        return affected;
    }

    public boolean areEffectsBlocked(final @NotNull AreaEffectCloud areaEffectCloud) {
        var isHarmful = areEffectsBlocked(areaEffectCloud.getCustomEffects());

        var basePotionType = areaEffectCloud.getBasePotionType();
        if (basePotionType != null) {
            isHarmful = isHarmful || areEffectsBlocked(basePotionType.getPotionEffects());
        }

        return isHarmful;
    }

    public boolean areEffectsBlocked(final @NotNull ThrownPotion potion) {
        return areEffectsBlocked(potion.getEffects());
    }

    public boolean areEffectsBlocked(final @NotNull Collection<PotionEffect> effects) {
        if (plugin.config.blockPotionEffectRule.equalsIgnoreCase("all")) {
            return effects.stream().allMatch(effect -> plugin.config.blockedPotionEffects.contains(effect.getType()));
        }

        return effects.stream().anyMatch(effect -> plugin.config.blockedPotionEffects.contains(effect.getType()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(final @NotNull ProjectileLaunchEvent event) {
        // Known issues:
        //  * Does not cover projectiles launched from dispensers
        if (!(event.getEntity().getShooter() instanceof Entity shooter)) {
            return;
        }

        var attacker = PacifistService.getAttackingPlayerFromOriginEntity(shooter);
        if (attacker == null) {
            return;
        }

        PacifistService.setOwnerTag(event.getEntity(), attacker);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLingeringPotionSplash(final @NotNull LingeringPotionSplashEvent event) {
        var attacker = PacifistService.getAttackingPlayerFromOriginEntity(event.getEntity());
        if (attacker == null) {
            return;
        }

        PacifistService.setOwnerTag(event.getAreaEffectCloud(), attacker);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(final @NotNull PotionSplashEvent event) {
        // If there is no harmful effect in the potion, allow it.
        if (!areEffectsBlocked(event.getPotion())) {
            return;
        }

        var attacker = PacifistService.getAttackingPlayerFromOriginEntity(event.getEntity());
        if (attacker == null) {
            return;
        }

        var affected = applyAreaOfEffect(attacker, event.getAffectedEntities());
        if (affected.size() != event.getAffectedEntities().size()) {
            PacifistMessaging.sendGenericDenialMessage(
                    attacker,
                    event.getEntity(),
                    "can't damage PvP-disabled players using %s".formatted(event.getPotion().getName())
            );
        }

        // Set the effect intensity to 0 for unaffected players, effectively cancelling the effect.
        event.getAffectedEntities().stream().filter(e -> !affected.contains(e)).forEach(e -> event.setIntensity(e, 0));
    }

    @EventHandler(ignoreCancelled = true)
    public void onAreaEffectCloudApply(final @NotNull AreaEffectCloudApplyEvent event) {
        // If there is no harmful effect in the potion, allow it.
        if (!areEffectsBlocked(event.getEntity())) {
            return;
        }

        var attacker = PacifistService.getAttackingPlayerFromOriginEntity(event.getEntity());
        if (attacker == null) {
            return;
        }

        var sizeBefore = event.getAffectedEntities().size();
        applyAreaOfEffect(attacker, event.getAffectedEntities());

        if (sizeBefore != event.getAffectedEntities().size()) {
            PacifistMessaging.sendGenericDenialMessage(
                    attacker,
                    event.getEntity(),
                    "can't damage PvP-disabled players using %s".formatted(event.getEntity().getName())
            );
        }
    }
}
