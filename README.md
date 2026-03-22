# SkyBlock Fisher

Auto-fishing mod for Hypixel SkyBlock on Fabric 1.21.10.

Detects bites, reels in, kills mobs, and recasts — with built-in humanization to avoid detection.

> **You should always be at your PC while using this.** The mod does not auto-aim at your fishing spot or handle repositioning. On death, it pauses and waits for you to take over. If you get actively checked by staff, you need to be there to respond. This is a semi-AFK tool, not a fully unattended bot.

## Features

### Auto-Fishing & Bite Detection
- **Auto-fishing** — casts, waits for bite, reels in, recasts automatically
- **Bite detection** — audio (ding sound), visual (armor stand "!!!"), or both
- **Configurable delays** — reel and recast timing with min/max ranges
- **Sound/event log** — shows recent sounds to help identify the right bite sound
- **Chat feedback** — toggle notifications for bite detection and rod swaps

### Kill Modes
- **Flaming Flay** — auto-swaps to Flay and holds right-click to kill after catching
- **Strider Fishing** — auto-swaps to weapon and left-click spams at configurable CPS
- **Configurable hotbar slots** — set which slots hold your rod, Flay, and weapon

### Anti-Detection & Humanization
- **Gaussian delays** — bell-curve distributed timing instead of uniform random
- **Fatigue simulation** — reaction times gradually slow over the session
- **Camera jitter** — small random look movements while waiting
- **Micro-breaks** — short random pauses between catches
- **Session breaks** — longer periodic pauses to simulate real play
- **Occasional misses** — randomly skips a bite to look human

### Safety & Alerts
- **Whisper pause** — auto-pauses and alerts you if someone DMs you
- **Death pause** — pauses on `☠ You` in chat so you don't AFK after dying
- **Sea creature pause** — individual toggles for dangerous spawns:
  - Thunder | Lord Jawbus
  - Fiery Scuttler | Ragnarok
- **Watchdog alerts** — pauses if Watchdog activity is detected in chat

### General
- **In-game GUI** — configure everything with collapsible sections
- **Auto-updater** — checks GitHub for new releases and updates in-game
- **Config persistence** — settings saved to `skyblockfisher.json`

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for 1.21.10
2. Download `SkyBlockFisher-1.1.0.jar` from [Releases](https://github.com/Glitchyorsmth/SkyBlockFisher/releases)
3. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.10
4. Drop both JARs in your `.minecraft/mods/` folder
5. Launch Minecraft with Fabric 1.21.10

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

JAR outputs to `build/libs/SkyBlockFisher-1.1.0.jar`

## Warning

Using macros violates Hypixel's rules and can result in a ban. Use at your own risk. Always be present at your PC — this mod is not designed for unattended use.
