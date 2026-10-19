<div align="center">

<img src="docs/logo.png" alt="" width="96">

# Prefab World

**Drop a map into a folder — every world you create from then on is generated from it.**<br>
Built for mapmakers, modpack authors and server owners. No command blocks, no datapacks.

[![Modrinth](https://img.shields.io/modrinth/dt/prefabworldmod?label=Modrinth&color=00AF5C)](https://modrinth.com/mod/prefabworldmod)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.3-62B47A)](#compatibility)
[![NeoForge](https://img.shields.io/badge/NeoForge-26.3.0.92-ED8D36)](#compatibility)
[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-lightgrey)](LICENSE)

**[中文](README.md) | [English](README_EN.md)**

### [⬇ Download on Modrinth](https://modrinth.com/mod/prefabworldmod)

</div>

## What problem it solves

Mapmakers and modpack authors keep running into the same chore: every test world, every player's fresh start, has to be filled with the same spawn area, the same starting island, the same tutorial lobby — built again from scratch.

Prefab World turns that into a single step: **prepare the map once, and every new world starts from it.**

## How it works

1. Install the mod (client and server side).
2. Put your template map — a normal world save folder — into the `prefab_saves` folder inside your instance directory.
3. Create a new world as usual.

The new world is generated with your template's terrain and structures. Want a different starting layout? Swap the template folder.

```text
.minecraft/
└── prefab_saves/
    ├── .active            # remembers which template is active
    └── your-template-map/
        ├── level.dat
        ├── region/
        └── ...
```

With a **single** template in the folder it is activated automatically; with several, pick one in the manager screen or with `/prefab use <name>`.

## Who it's for

| Role | What you use it for |
|------|--------------------|
| Mapmakers | A consistent starting area for every new playthrough — easier to test and ship |
| Modpack authors | Custom spawn, starter island or tutorial area in every new world |
| Server owners | A ready-made lobby, starter plot or event arena before you open the server |

## Features

- **Plug and play** — one save folder, no config to write
- **In-game manager** — add saves to the prefab list, activate or delete them with a click
- **Server commands** — `/prefab` does the same on dedicated servers
- **Selective copying** — player data, progress, world icon and `level.dat` are each toggleable
- **World rule overrides** — force the game mode and whether commands are allowed
- **Invisible to players** — the manager entry is hidden by default
- **No dependencies** — runs on its own

## Compatibility

| Version | Loader | Status |
|---------|--------|--------|
| 26.3 | NeoForge 26.3.0.92+ | ✅ Supported (current) |
| 1.20.1 | Forge | Legacy, no longer updated |
| 1.18.2 | Forge | Legacy, no longer updated |

> NeoForge only for now. Fabric is not planned; ask in Issues if you need it — enough requests and we'll add it.

## Installation

1. Install Minecraft 26.3 and NeoForge 26.3.0.92 or newer
2. Drop the `.jar` into your `mods` folder
3. Launch the game (requires Java 25)

## Manager screen

Set `showManageButton` to `true` in the config and a **Prefabs** button appears on the title screen, right below Multiplayer. It opens a list of your saves:

- Each row shows the world icon, name, last played time and save id
- **Regular saves** have an **Add** button — copies the save into the prefab folder
- **Prefabs** have **Use / Delete** buttons
- The active prefab is shown in green with an "ACTIVE" marker

## Commands

For admins (permission level 2):

| Command | Description |
|---------|-------------|
| `/prefab list` | List all prefabs and mark the active one |
| `/prefab use <name>` | Activate a prefab |
| `/prefab add <save>` | Copy a save from `saves/` into the prefab folder |
| `/prefab remove <name>` | Delete a prefab |

Arguments are tab-completed and support names with spaces, including Chinese characters.

## Configuration

The config lives in `config/prefabworldmod-local.toml`, or use **Mods → Prefab World → Config** in game.

| Option | Default | Description |
|--------|---------|-------------|
| `folderName` | `prefab_saves` | Folder that holds the prefab saves |
| `showManageButton` | `false` | Show the manager button on the title screen |
| `overwriteLevelDat` | `false` | Copy the template's `level.dat` (world name, time, …) |
| `copyPlayerData` | `false` | Copy `players/` (inventories, positions, ender chests) |
| `copyPlayerProgress` | `false` | Copy statistics and advancements |
| `copyIcon` | `true` | Copy the world icon |
| `defaultGameMode` | `KEEP` | Force a game mode: `KEEP` / `SURVIVAL` / `CREATIVE` / `ADVENTURE` / `SPECTATOR` |
| `allowCommands` | `KEEP` | Allow commands: `KEEP` / `ENABLED` / `DISABLED` |

Why these defaults: a prefab is a map, so players should start clean — player data and progress are not copied. The icon is copied so every new world shows the same preview image. Game mode and commands follow whatever was picked when creating the world.

## FAQ

**Q: My new world still has normal terrain.**

A: Check that the template folder sits directly inside `prefab_saves` in your instance directory, and that it is an extracted save folder, not a zip. Use `/prefab list` or the manager screen to confirm it is active.

**Q: Does it work on multiplayer servers?**

A: Yes. The server and every client need the same version installed.

**Q: Will it overwrite my existing worlds?**

A: No. It only affects worlds created after installation; existing worlds are untouched.

**Q: Can I use a template for some worlds and not others?**

A: Every new world uses the currently active template. Existing worlds are unaffected.

**Q: Why is there no Prefabs button on the title screen?**

A: So that regular players never see it — the entry is off by default. Turn on `showManageButton` in the config.

**Q: Do players inherit the map author's inventory?**

A: No, player data is not copied by default. Set `copyPlayerData` to `true` if you want it.

## Feedback

Open an [Issue](https://github.com/Stryde01/Prefabworld/issues). English or Chinese, both are fine. Include your game version and a crash log if you have one — it makes debugging much faster.

## License

All Rights Reserved — see [LICENSE](LICENSE). If you want to redistribute it or bundle it into your own pack, please open an Issue first.

---

<div align="center">
<sub>Made for mapmakers and modpack authors.</sub>
</div>
