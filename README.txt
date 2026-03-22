SkyBlock Fisher - Auto-Fishing Mod (Fabric 1.21.10)
==================================================

REQUIREMENTS
------------
- Java 21 (JDK)
- Minecraft 1.21.1 with Fabric Loader installed

BUILD
-----
1. Open a terminal in the SkyBlockFisher folder
2. Run:
       gradlew build
   (On first run it will download Minecraft + mappings — this takes a few minutes)
3. The built JAR is at: build/libs/SkyBlockFisher-1.0.0.jar
4. Copy that JAR into your .minecraft/mods/ folder
   (Make sure Fabric API is also in your mods folder)

If you don't have a Gradle wrapper yet:
       gradle wrapper --gradle-version 8.8
   then use gradlew.

INSTALL
-------
You need TWO jars in .minecraft/mods/:
  1. SkyBlockFisher-1.0.0.jar (this mod)
  2. fabric-api-*.jar (download from https://modrinth.com/mod/fabric-api)

IN-GAME USAGE
-------------
  Right Shift  -  Open the settings GUI
  F6           -  Toggle fishing on/off
  ESC          -  Close GUI

HOW IT WORKS
------------
1. Hold your fishing rod and stand at your fishing spot
2. Press F6 or open GUI and click START
3. The mod auto-casts, then listens for the "ding" sound from the server
4. When the ding is detected it reels in after a random delay, then recasts
5. Repeat until you stop it or the failsafe timer runs out

SETTINGS (in GUI)
-----------------
- Target Sound:      Sound path to detect (default: block.note_block.pling)
- Reel Delay:        Tick range before reeling in (20 ticks = 1 second)
- Recast Delay:      Tick range before recasting after reel
- Failsafe:          Auto-stop after this many minutes
- Anti-AFK:          Prevents AFK kick
- Sound Log:         Shows recent server sounds — use this to identify the
                     correct sound name if the default doesn't match

FINDING THE RIGHT SOUND
------------------------
If "block.note_block.pling" isn't the ding you hear:
1. Open the GUI (Right Shift)
2. Enable Sound Log
3. Cast your rod manually and wait for a bite
4. Look at the sound log — the ding will appear highlighted if it matches,
   otherwise find the new entry that appeared when you heard the ding
5. Copy that sound name into the Target Sound field

CONFIG FILE: .minecraft/config/skyblockfisher.json

WARNING: Using macros violates Hypixel's rules and can result in a ban.
