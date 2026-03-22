# Changelog

All notable changes to SkyBlock Fisher will be documented here.

---

## v1.1.0
- Added in-game auto-updater (checks GitHub releases, one-click update from GUI)
- Added changelog panel in GUI — see what changed before updating
- Added Strider Kill mode (left-click spam with configurable CPS 1-20)
- Renamed Flay tab to "Killing" with Flaming Flay and Strider subsections
- Flaming Flay now holds right-click instead of spamming
- Faster Flay swap timing (5-15ms micro-delay)
- Flay kill detection via sound (XP orb, death, attack sounds) — returns to rod early on kill
- Visual bite detection via armor stand "!!!" entity scanning
- Detection mode toggle: Audio, Visual, or Both
- Only one kill mode can be active at a time (Flay and Strider are mutually exclusive)

## v1.0.0
- Initial release
- Auto-fishing with sound-based bite detection (note.pling)
- Flaming Flay auto-kill on catch
- Anti-Watchdog humanization: gaussian delays, fatigue simulation, camera jitter
- Micro-breaks and session breaks with configurable intervals
- Configurable miss chance for realism
- Whisper pause — auto-pauses and alerts when someone messages you
- In-game GUI with tabs (Main, Anti-Detect, Killing)
- Keybinds: Right Shift (GUI), F6 (toggle)
- Failsafe auto-stop timer
