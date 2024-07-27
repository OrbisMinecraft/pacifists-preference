// Copyright © 2024. OrbisMC Contributors 
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist.listener;

import net.orbismc.pacifist.PacifistMessaging;
import net.orbismc.pacifist.PacifistService;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

// TODO: Players can push other players
public final class CombatEventListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Determine the party doing the damaging
        OfflinePlayer attacker = PacifistService.getAttackingPlayerFromOriginEntity(event.getDamager());
        if (attacker == null) {
            return;
        }

        // Determine the party being damaged
        OfflinePlayer target = PacifistService.getDamagedPlayerFromOriginEntity(event.getEntity());
        if (target == null) {
            return;
        }

        // If the attacker is the same is the target, allow it.
        if (attacker.equals(target)) {
            return;
        }

        // If both of them have their PvP enabled, let the event pass.
        if (PacifistService.isPvpEnabled(attacker, target)) {
            return;
        }

        PacifistMessaging.sendAttackDenialMessage(
                attacker,
                target,
                event.getDamager(),
                event.getEntity()
        );

        event.setCancelled(true);
    }
}