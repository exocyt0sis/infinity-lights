# Styx's Infinity Lights

Styx's Infinity Lights for NeoForge 1.21.1 makes permanent lighting a glowstone-tier upgrade instead of the default early-game path.

This codebase was ported from Forge to NeoForge 1.21.1 and now splits lighting into two families: permanent glowstone lights from the mod itself, and ordinary vanilla lights that eventually burn out.

Vanilla coal-based torches, lanterns and campfires can still be crafted and used, but they now burn out over time. Permanent light instead comes from the mod's own glowstone items.

## Compatibility

- Minecraft: `1.21.1`
- NeoForge: `21.1.226`
- Java: `21`
- Mod ID: `infinitylights`
- Current version: `2.1`

## Core Gameplay

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

The mod now registers a common NeoForge config.

The generated TOML starts with general settings so the most global toggle is visible immediately, then groups glowstone torch, glowstone lantern and glowstone Jack o'Lantern settings first, followed by the vanilla torch, candle, lantern, Jack o'Lantern and campfire sections.

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
- `build/libs/infinitylights-1.21.1-neoforge-2.1.jar`
