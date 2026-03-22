SkyBlock Fisher - Auto-Fishing Mod (Fabric 1.21.10)
===================================================

REQUIREMENTS
------------
- Java 21 (JDK) — download from https://adoptium.net
- Minecraft 1.21.10 with Fabric Loader installed

BUILD
-----
1. Open a terminal in the SkyBlockFisher folder
2. Run:
       .\gradlew.bat build        (Windows)
       ./gradlew build            (Mac/Linux)

   Everything is included — no need to install Gradle.
   First run downloads Minecraft + mappings and takes a few minutes.

3. The built JAR is at: build/libs/SkyBlockFisher-1.0.0.jar

INSTALL
-------
Drop TWO jars into your mods folder:
  1. SkyBlockFisher-1.0.0.jar  (from build/libs/)
  2. Fabric API                (download from https://modrinth.com/mod/fabric-api)

IN-GAME USAGE
-------------
  Right Shift  —  Open the settings GUI
  F6           —  Toggle fishing on/off
  ESC          —  Close GUI

HOW IT WORKS
------------
1. Hold your fishing rod and stand at your fishing spot
2. Press F6 or open GUI and click START
3. The mod detects bites via sound (ding) and/or visual (armor stand with !!!)
4. When a bite is detected it reels in, optionally kills with Flaming Flay, then recasts
5. All delays are randomized with Gaussian distribution to look human

DETECTION MODES
---------------
- AUDIO:   Listens for the target sound (default: block.note_block.pling)
- VISUAL:  Scans for armor stands named "!!!" near you
- BOTH:    Either one triggers the reel-in

FLAMING FLAY
------------
Enable in GUI to auto-kill after reeling in:
  - Set your fishing rod hotbar slot (1-9)
  - Set your Flaming Flay hotbar slot (1-9)
  - On bite: reels in → swaps to Flay → holds right-click to kill → swaps back → recasts

ANTI-DETECTION
--------------
Built-in humanization to avoid pattern detection:
  - Gaussian-distributed delays (not uniform random)
  - Fatigue simulation (reaction times slow over time)
  - Camera jitter (small random look movements)
  - Session breaks (periodic pauses)
  - Occasional missed catches
  - Whisper detection (pauses and alerts you)

FINDING THE RIGHT SOUND
------------------------
If the default sound doesn't match your ding:
1. Open the GUI (Right Shift)
2. Enable Sound Log
3. Cast manually and wait for a bite
4. Check the log for the sound that appeared when you heard the ding
5. Copy that sound name into the Target Sound field

CONFIG FILE: .minecraft/config/skyblockfisher.json

WARNING: Using macros violates Hypixel's rules and can result in a ban.
