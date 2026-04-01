# Styx's Infinity Lights

[![Version](https://img.shields.io/badge/version-1.0.7-orange.svg)](https://github.com/exocyt0sis/infinity-lights/releases/tag/v1.0.7)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-3C8527.svg)](https://www.minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.2.0+-f16436.svg)](https://files.minecraftforge.net/)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Release](https://img.shields.io/badge/status-release_ready-brightgreen.svg)](https://github.com/exocyt0sis/infinity-lights/releases/tag/v1.0.7)

Giving players an incentive to venture into the Nether and explore other means of lighting settlements, this mod replaces Mojang's coal based light source progression with glowstone based crafting while retaining vanilla block IDs for compatibility.

## Public Summary

Infinity Lights makes lighting progression harder by requiring glowstone for torches and lanterns, encouraging Nether exploration before mass lighting becomes available.

## Features

- Replaces vanilla recipe progression for `minecraft:torch` and `minecraft:lantern` to require glowstone.
- Adds depletion mechanics for active glowstone torches and lanterns (configurable).
- Adds depleted variants with matching placement/shape behavior.
- Adds optional recycling recipes for depleted lights (config-gated).
- Adds Forge metadata/logo integration and configurable console messages.

## Compatibility

- Minecraft: `1.20.1`
- Forge: `47.2.0+`
- Mod ID: `infinitylights`

## Default Config

Config file: `run/saves/<world>/serverconfig/infinitylights.toml` (singleplayer) or `<server>/world/serverconfig/infinitylights.toml` (dedicated server)

- `TorchDepletionRisk = 0`
- `LanternDepletionRisk = 0`
- `TorchRecyclable = false`
- `LanternRecyclable = false`
- `ConsoleMessages = false`

## Build Artifact

- `build/libs/infinitylights-1.20.1-forge-47.2.0-1.0.7.jar`
