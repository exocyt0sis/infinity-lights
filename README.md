# Styx's Infinity Lights

[![Version](https://img.shields.io/badge/version-2.1.1-orange.svg)](https://github.com/exocyt0sis/infinity-lights/releases/tag/v2.1.1)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-3C8527.svg)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.226-43853d.svg)](https://neoforged.net/)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Release](https://img.shields.io/badge/status-release_2.1.1-brightgreen.svg)](https://github.com/exocyt0sis/infinity-lights/releases/tag/v2.1.1)

Giving players an incentive to venture into the Nether and explore other means of lighting settlements, this mod replaces Mojang's coal based light source progression with glowstone based crafting while retaining vanilla block IDs for compatibility.

Styx's Infinity Lights for NeoForge 1.21.1 makes permanent lighting a glowstone-tier upgrade instead of the default early-game path.

This NeoForge 1.21.1 port splits lighting into two families: permanent glowstone lights from the mod itself, and ordinary vanilla lights that eventually burn out.

Vanilla coal-based torches, lanterns and campfires can still be crafted and used, but they now burn out over time. Permanent light instead comes from the mod's own glowstone-based items.

## Public Summary

Infinity Lights keeps vanilla block IDs for compatibility while moving permanent lighting up to glowstone-tier progression. In the NeoForge 1.21.1 line, vanilla torches, lanterns, candles, jack o'lanterns and campfires remain available, but temporary light now burns out over time while the mod's own glowstone lights fill the permanent role.

## Compatibility

- Minecraft: `1.21.1`
- NeoForge: `21.1.226`
- Java: `21`
- Mod ID: `infinitylights`
- Current version: `2.1.1`

## 2.1.1 Note

Version 2.1.1 adds `infinitylights:burning_campfire` to `minecraft:campfires` so datapack and mod integrations that key off the vanilla campfire block tag continue to recognize Infinity Lights campfires.

This is a tag-only compatibility adjustment. It does not change Infinity Lights burnout timing, smoldering, light emission, rain reactions, particles, sounds, or campfire state transitions.

## Branches

- `master` is the current and officially supported NeoForge 1.21.1 branch.
- `legacy/forge-1.20.1` is a historical Forge 1.20.1 branch kept for reference and older installs.
- The legacy Forge branch is no longer actively maintained and should not be treated as the current release line.

## Features

- `minecraft:torch` uses vanilla coal/charcoal crafting again, but burns out after a configurable number of in-game days.
- `minecraft:lantern` uses vanilla torch progression again, but also burns out after a configurable number of in-game days.
- `minecraft:candle` and all vanilla candle colors burn out after a configurable number of in-game days and can be smoldered by rain.
- `minecraft:jack_o_lantern` burns out after a configurable number of in-game days and becomes a `minecraft:carved_pumpkin`.
- `minecraft:campfire` burns out after a configurable number of in-game days, disappears in a brief ash-like puff, and can leave `supplementaries:ash` behind when Supplementaries is installed.
- `infinitylights:glowstone_torch` is crafted from `minecraft:glowstone_dust` and `minecraft:stick` and acts as a permanent glowstone torch.
- `infinitylights:glowstone_lantern` is crafted from `infinitylights:glowstone_torch` and `minecraft:iron_nugget` and acts as a permanent glowstone lantern.
- `infinitylights:glowstone_jack_o_lantern` is crafted from `minecraft:carved_pumpkin` and `infinitylights:glowstone_torch`, and can eventually deplete back into a `minecraft:carved_pumpkin`.
- Rain can smolder vanilla torches, candles and campfires, reducing their light output and emitting restrained dark smoke particles when enabled in config.

## Added Items And Blocks

- `infinitylights:glowstone_torch`
- `infinitylights:glowstone_lantern`
- `infinitylights:glowstone_jack_o_lantern`
- `infinitylights:depleted_glowstone_torch`
- `infinitylights:depleted_torch`
- `infinitylights:depleted_lantern`

## Config

The mod registers a common NeoForge config.

The generated TOML starts with general settings so the most global toggle is visible immediately, then groups glowstone torch, glowstone lantern and glowstone jack o'lantern settings first, followed by the vanilla torch, candle, lantern, jack o'lantern and campfire sections.

Expected config path in development runs:
- `runs/client/config/infinitylights.toml`

Expected config path in a normal Minecraft instance:
- `.minecraft/config/infinitylights.toml`

Key settings:

- `GlowstoneTorchRecyclable`
- `GlowstoneTorchDepletionRisk`
- `GlowstoneLanternDepletionRisk`
- `GlowstoneJackoLanternDepletionRisk`
- `TorchBurnoutTime`
- `TorchSmolderedByRain`
- `CandleBurnoutTime`
- `CandleSmolderedByRain`
- `LanternBurnoutTime`
- `LanternRecyclable`
- `JackoLanternBurnoutTime`
- `CampfireBurnoutTime`
- `CampfireSmolderedByRain`
- `ConsoleMessages`

## Build Artifact

- Preferred release command: `gradlew.bat releaseJar`
- `build/libs/infinitylights-1.21.1-neoforge-2.1.1.jar`
