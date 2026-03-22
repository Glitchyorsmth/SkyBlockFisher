# SkyBlock Fisher

Auto-fishing mod for Hypixel SkyBlock on Fabric 1.21.10.

Detects bites, reels in, kills mobs, and recasts — all with built-in humanization to look legit.

## Features

- **Auto-fishing** — casts, waits for bite, reels in, recasts automatically
- **Bite detection** — audio (ding sound), visual (armor stand "!!!"), or both
- **Flaming Flay** — auto-swaps to Flay and holds right-click to kill after catching
- **Strider Fishing** — auto-swaps to weapon and left-click spams at ~14 CPS to kill
- **Anti-detection** — Gaussian delays, fatigue simulation, camera jitter, micro-breaks, session breaks, occasional misses
- **Whisper pause** — auto-pauses and alerts you if someone DMs you
- **Watchdog alerts** — pauses if Watchdog activity is detected in chat
- **Sound log** — shows recent sounds to help identify the right bite sound
- **In-game GUI** — configure everything without leaving the game

## Install

1. Download `SkyBlockFisher-1.0.0.jar` from [Releases](https://github.com/Glitchyorsmth/SkyBlockFisher/releases)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.10
3. Drop both jars in your mods folder
4. Launch with Fabric 1.21.10

## Controls

| Key | Action |
|-----|--------|
| Right Shift | Open settings GUI |
| F6 | Toggle fishing on/off |
| ESC | Close GUI |

## Build from source

```
.\gradlew.bat build
```

JAR outputs to `build/libs/SkyBlockFisher-1.0.0.jar`

## Warning

Using macros violates Hypixel's rules and can result in a ban. Use at your own risk.
