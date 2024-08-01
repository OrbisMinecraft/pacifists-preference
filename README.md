# Pacifist's Preference — PvP-toggle for distinguished gentlemen

A basic, plugin for allowing players to toggle player-versus-player (*PvP*) on and off on demand. Exposes one command
`/pvp` to set your PvP state. If it's `off`, you can not participate in PvP combat.

## Features

This plugin handles the following player interactions:

| Group                 | Handled Actions                                                                                                                                                                              | Limitations                                                                                                                                                                                                                                         | Comment                                  |
|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------|
| Direct Attacks        | - Players directly hitting other players with their fists or melee weapons                                                                                                                   | —                                                                                                                                                                                                                                                   | —                                        |
| Projectiles           | - Players shooting other players with any projectile (arrows, snowballs)<br/>- Players throwing splash potions at other players<br/>- Players throwing lingering potions at other players    | - Cannot handle projectiles shot using dispensers                                                                                                                                                                                                   | Blocked potion effects can be configured |
| Area of Effect Clouds | - Area of effect clouds created using lingering potions                                                                                                                                      | - Cannot handle area effect clouds created using any other means than a lingering potion                                                                                                                                                            | —                                        |
| Pets                  | - Any animal tamed by a player attacking another player<br/>- Any animal bred by a player attacking another player                                                                           | - Cannot check pets created through blocks (e.g. iron golems)                                                                                                                                                                                       | —                                        |
| Dangerous Blocks      | - Placing dangerous blocks within a radius around other players (such as lava buckets)<br/>- Placing beds or respawn anchors in worlds where they explode within 30 blocks of another player | - Can only handle blocks directly placed by the player<br/>- Does not account for moving blocks (e.g. water) being placed just outside of the radius<br/>- Does not currently prevent the explosion of beds/respawn anchors in the wrong dimensions | Blocked block types can be configured    |
| Dangerous Entities    | - TNT and TNT minecarts ignited / placed by another player<br/>- End crystals placed / detonated by another player                                                                           | - Cannot account for these entities being placed using dispensers<br/>- Uses a best-effort system to figure out who caused damage to be dealt (either the player who spawned the entity or the player who caused it ignition/explosion)             | —                                        |


## Commands

The following commands are exposed by the plugin:

| Command               | Permission                   | Description                                                  |
|-----------------------|------------------------------|--------------------------------------------------------------|
| `/pvp`                | `pacifistspreference.use`    | Toggle between PvP states (on and off)                       |
| `/pvp on`             | `pacifistspreference.use`    | Enable PvP participation for the player running the command  |
| `/pvp off`            | `pacifistspreference.use`    | Disable PvP participation for the player running the command |
| `/pvp on PlayerName`  | `pacifistspreference.other`  | Enable PvP participation for another player                  |
| `/pvp off PlayerName` | `pacifistspreference.other`  | Disable PvP participation for another player                 |
| `/pvp-reload`         | `pacifistspreference.reload` | Reload the configuration of the plugin                       |
