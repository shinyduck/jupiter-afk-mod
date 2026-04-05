# Jupiter AFK Mod

A lightweight server-side Fabric mod that automatically detects and manages AFK players.

## Features

- **Auto AFK detection** — players are marked AFK after a configurable period of inactivity
- **Tab list indicator** — AFK players show a `[AFK]` prefix in the tab list
- **Chat broadcasts** — server is notified when a player goes AFK or returns
- **Hunger freeze** — AFK players do not lose hunger
- **Damage freeze** — AFK players cannot take damage
- **Sleep bypass** — AFK players are excluded from the sleep percentage calculation so they don't block the night skip
- **`/afk` command** — players can manually toggle their AFK status

## Requirements

- Minecraft `26.1`
- [Fabric Loader](https://fabricmc.net/) `>=0.18.6`
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java `25`
- **Server-side only** — does not need to be installed on the client

## Installation

1. Download the latest `.jar` from the [Releases](https://github.com/shinyduck/jupiter-afk-mod/releases) page
2. Drop it into your server's `mods/` folder
3. Restart the server
4. A config file will be generated at `config/jupiter_afk.json`

## Configuration

The config file is located at `config/jupiter_afk.json` in your server folder.

| Option | Default | Description |
|---|---|---|
| `timeoutSeconds` | `300` | Seconds of inactivity before a player is marked AFK |
| `resetOnMovement` | `true` | Moving resets the AFK timer |
| `resetOnLook` | `true` | Looking around resets the AFK timer |
| `resetOnChat` | `false` | Chatting resets the AFK timer |
| `resetOnBlockInteractions` | `true` | Right-clicking blocks resets the AFK timer |
| `enableChatMessages` | `true` | Broadcast messages when AFK state changes |
| `wentAfkMessage` | `§e%player% is now AFK` | Message shown when a player goes AFK |
| `returnedMessage` | `§e%player% is no longer AFK` | Message shown when a player returns |
| `afkPlayerName` | `§7[AFK] §r%player%` | Tab list name format for AFK players |
| `freezeHunger` | `true` | Prevent hunger drain while AFK |
| `freezeDamage` | `true` | Prevent damage while AFK |
| `bypassSleep` | `true` | Exclude AFK players from sleep percentage |

Use `%player%` as a placeholder for the player's name in message and name fields.  
Supports `§` colour codes.

## Commands

| Command | Description |
|---|---|
| `/afk` | Toggle your own AFK status |

## License

[MIT](LICENSE)
