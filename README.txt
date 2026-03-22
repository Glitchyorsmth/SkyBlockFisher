SkyBlock Fisher - Auto-Fishing Mod (Fabric 1.21.10)
===================================================

INSTALL
-------
1. Download SkyBlockFisher-1.0.0.jar from the Releases page:
   https://github.com/Glitchyorsmth/SkyBlockFisher/releases

2. Download Fabric API for 1.21.10 from:
   https://modrinth.com/mod/fabric-api

3. Drop BOTH jars into your mods folder

4. Launch Minecraft with Fabric 1.21.10

That's it. No building, no Java, no terminal commands.

BUILD FROM SOURCE (optional)
----------------------------
Only if you want to modify the code yourself:
  1. Install Java 21 JDK (https://adoptium.net)
  2. Open a terminal in the SkyBlockFisher folder
  3. Run:  .\gradlew.bat build  (Windows)  or  ./gradlew build  (Mac/Linux)
  4. JAR is at: build/libs/SkyBlockFisher-1.0.0.jar

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
