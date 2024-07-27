// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class PacifistMessaging {
    public static void sendAttackDenialMessage(Player attacker, Player target, Entity attackerProxy, Entity targetProxy) {
        var message = "%s can't attack %s because %s have PvP disabled [/pvp]".formatted(
                getAttackerDescriptor(attackerProxy),
                getTargetDescriptor(targetProxy),
                getReasonDescriptor(attacker, target)
        );

        var text = new TextComponent(message);
        text.setColor(ChatColor.RED);
        text.setBold(true);

        attacker.spigot().sendMessage(ChatMessageType.ACTION_BAR, text);
    }

    public static void sendGenericDenialMessage(@NotNull Player attacker, Entity attackerProxy, String msg) {
        var message = "%s %s [/pvp]".formatted(
                getAttackerDescriptor(attackerProxy),
                msg
        );

        var text = new TextComponent(message);
        text.setColor(ChatColor.RED);
        text.setBold(true);

        attacker.spigot().sendMessage(ChatMessageType.ACTION_BAR, text);
    }

    @Contract(pure = true)
    private static @NotNull String getAttackerDescriptor(Entity attacker) {
        return switch (attacker) {
            case Tameable e -> "Your pet";
            default -> "You";
        };
    }

    private static @NotNull String getTargetDescriptor(Entity target) {
        return switch (target) {
            case Tameable e -> {
                var owner = e.getOwner().getName();
                yield owner.endsWith("s") ? owner + "' pet" : owner + "'s pet";
            }
            default -> target.getName();
        };
    }

    private static @NotNull String getReasonDescriptor(Player attacker, Player target) {
        var attackerPvpEnabled = PacifistService.isPvpEnabled(attacker);
        var targetPvpEnabled = PacifistService.isPvpEnabled(target);

        if (!attackerPvpEnabled && !targetPvpEnabled) {
            return "you both";
        } else if (attackerPvpEnabled) {
            return "they";
        } else {
            return "you";
        }
    }


}
