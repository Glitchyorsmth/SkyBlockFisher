package com.skyblockfisher;

import com.skyblockfisher.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class FishingHandler {

    private static final FishingHandler INSTANCE = new FishingHandler();
    public static FishingHandler getInstance() { return INSTANCE; }

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Random random = new Random();

    // --- State machine ---
    public enum Phase {
        IDLE, WAITING_FOR_BITE, REEL_DELAY, RECAST_DELAY, CAST_WAIT,
        FLAY_SWAP, FLAY_USE, FLAY_KILL_WAIT, FLAY_RETURN,
        STRIDER_SWAP, STRIDER_KILL, STRIDER_RETURN,
        MICRO_BREAK, SESSION_BREAK, WHISPER_PAUSE
    }
    private boolean enabled = false;
    private Phase phase = Phase.IDLE;
    private int delayTicks = 0;

    // --- Stats ---
    private int totalTicks = 0;
    private int catchCount = 0;
    private int missCount = 0;

    // --- Humanization state ---
    private int lastAfkTick = 0;
    private int nextCameraJitterTick = 0;
    private int nextSessionBreakTick = 0;
    private int catchesSinceBreak = 0;

    // Camera jitter target (smooth interpolation)
    private float jitterTargetYaw = 0;
    private float jitterTargetPitch = 0;
    private int jitterStepsTodo = 0;
    private float jitterYawPerStep = 0;
    private float jitterPitchPerStep = 0;

    // Sound log
    private final List<String> soundLog = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_SOUND_LOG = 20;

    // Whisper alert
    private String lastWhisperFrom = null;

    // Flaming Flay kill detection
    private boolean flayKillDetected = false;
    private int flaySpamCooldown = 0;

    // Strider fishing kill detection
    private boolean striderKillDetected = false;
    private int striderClickCooldown = 0;

    // Visual detection: track armor stand entity IDs we've already reacted to
    private final Set<Integer> seenArmorStandIds = new HashSet<>();

    // --- Public API ---
    public void toggle() {
        if (enabled) stop(); else start();
    }

    public void start() {
        if (mc.player == null) return;
        enabled = true;
        phase = Phase.RECAST_DELAY;
        delayTicks = gaussianRange(8, 20); // slight random start delay
        catchCount = 0;
        missCount = 0;
        totalTicks = 0;
        lastAfkTick = 0;
        catchesSinceBreak = 0;
        lastWhisperFrom = null;
        flayKillDetected = false;
        striderKillDetected = false;
        seenArmorStandIds.clear();
        scheduleNextJitter();
        scheduleNextSessionBreak();
        chat(Formatting.GREEN, "Started! Hold your fishing rod.");
    }

    public void stop() {
        enabled = false;
        phase = Phase.IDLE;
        setRightClickHeld(false); // always release right-click on stop
        setLeftClickHeld(false);  // always release left-click on stop
        chat(Formatting.RED, "Stopped. Catches: " + catchCount +
                (missCount > 0 ? " (missed " + missCount + ")" : ""));
    }

    public void resumeFromPause() {
        if (!enabled) return;
        if (phase == Phase.WHISPER_PAUSE) {
            lastWhisperFrom = null;
            phase = Phase.RECAST_DELAY;
            delayTicks = gaussianRange(20, 60); // human-like resume delay
            chat(Formatting.GREEN, "Resumed fishing.");
        }
    }

    public boolean isEnabled()        { return enabled; }
    public int getCatchCount()        { return catchCount; }
    public int getMissCount()         { return missCount; }
    public int getElapsedSeconds()    { return totalTicks / 20; }
    public List<String> getSoundLog() { return soundLog; }
    public String getWhisperFrom()    { return lastWhisperFrom; }
    public Phase getPhase()           { return phase; }

    public String getStatusText() {
        if (!enabled) return "Stopped";
        switch (phase) {
            case WAITING_FOR_BITE: return "Waiting for bite...";
            case REEL_DELAY:       return "Reeling in...";
            case FLAY_SWAP:        return "Swapping to Flaming Flay...";
            case FLAY_USE:         return "Using Flaming Flay...";
            case FLAY_KILL_WAIT:   return "Waiting for kill...";
            case FLAY_RETURN:      return "Swapping back to rod...";
            case STRIDER_SWAP:     return "Swapping to weapon...";
            case STRIDER_KILL:     return "Attacking (strider)...";
            case STRIDER_RETURN:   return "Swapping back to rod...";
            case RECAST_DELAY:     return "Recasting...";
            case CAST_WAIT:        return "Waiting for bobber...";
            case MICRO_BREAK:      return "Micro-break (" + (delayTicks / 20) + "s)";
            case SESSION_BREAK:    return "Session break (" + (delayTicks / 20) + "s)";
            case WHISPER_PAUSE:    return "PAUSED — whisper from " + lastWhisperFrom;
            default:               return "Idle";
        }
    }

    // ========================================
    //  SOUND DETECTION (called from mixin)
    // ========================================
    public void onSoundPacket(PlaySoundS2CPacket packet) {
        String soundPath;
        float volume;
        float pitch;

        try {
            String fullId = packet.getSound().value().id().toString();
            int colonIdx = fullId.indexOf(':');
            soundPath = colonIdx >= 0 ? fullId.substring(colonIdx + 1) : fullId;
            volume = packet.getVolume();
            pitch = packet.getPitch();
        } catch (Exception e) {
            System.out.println("[SkyBlockFisher] Sound packet extraction failed: " + e.getMessage());
            soundLog.add("ERROR: " + e.getClass().getSimpleName());
            if (soundLog.size() > MAX_SOUND_LOG) soundLog.remove(0);
            return;
        }

        // Always log sounds when enabled (regardless of detection mode)
        if (ModConfig.soundLogEnabled) {
            String entry = String.format("%s v=%.1f p=%.2f", soundPath, volume, pitch);
            soundLog.add(entry);
            if (soundLog.size() > MAX_SOUND_LOG) soundLog.remove(0);
        }

        // Detect kill during Flay wait (always active regardless of mode)
        if (enabled && phase == Phase.FLAY_KILL_WAIT) {
            if (soundPath.contains("entity.experience_orb") || soundPath.contains("entity.player.attack")
                    || soundPath.contains(".death") || soundPath.contains(".kill")) {
                flayKillDetected = true;
            }
        }

        // Detect kill during Strider kill (same sounds)
        if (enabled && phase == Phase.STRIDER_KILL) {
            if (soundPath.contains("entity.experience_orb") || soundPath.contains(".death")
                    || soundPath.contains(".kill")) {
                striderKillDetected = true;
            }
        }

        // Only trigger bite from sound if mode is AUDIO or BOTH
        String mode = ModConfig.detectionMode.toUpperCase();
        if (!mode.equals("AUDIO") && !mode.equals("BOTH")) return;

        if (!enabled || phase != Phase.WAITING_FOR_BITE) return;
        if (!soundPath.contains(ModConfig.targetSound)) return;

        // Proximity check
        if (mc.player != null) {
            double dx = packet.getX() - mc.player.getX();
            double dy = packet.getY() - mc.player.getY();
            double dz = packet.getZ() - mc.player.getZ();
            if (dx * dx + dy * dy + dz * dz > 256) return;
        }

        triggerBite("sound");
    }

    // ========================================
    //  VISUAL DETECTION (armor stand scan)
    // ========================================
    /**
     * Scans nearby armor stands for one with a custom name matching the
     * trigger text (default "!!!"). SkyBlock spawns an invisible armor
     * stand with a red "!!!" name tag when a fish bites.
     */
    private void scanForBiteArmorStand() {
        if (mc.player == null || mc.world == null) return;

        String mode = ModConfig.detectionMode.toUpperCase();
        if (!mode.equals("VISUAL") && !mode.equals("BOTH")) return;
        if (phase != Phase.WAITING_FOR_BITE) return;

        Box searchBox = mc.player.getBoundingBox().expand(10);
        List<ArmorStandEntity> stands = mc.world.getEntitiesByClass(
                ArmorStandEntity.class, searchBox, e -> e.hasCustomName());

        for (ArmorStandEntity stand : stands) {
            if (seenArmorStandIds.contains(stand.getId())) continue;

            Text name = stand.getCustomName();
            if (name == null) continue;

            String nameStr = name.getString();
            if (nameStr.contains(ModConfig.visualTriggerText)) {
                seenArmorStandIds.add(stand.getId());

                // Log it
                if (ModConfig.soundLogEnabled) {
                    String entry = "[VISUAL] ArmorStand: \"" + nameStr + "\"";
                    soundLog.add(entry);
                    if (soundLog.size() > MAX_SOUND_LOG) soundLog.remove(0);
                }

                triggerBite("visual");
                return; // only trigger once per tick
            }
        }
    }

    /**
     * Still called from TitleMixin — just for logging, not bite detection.
     */
    public void onTitleReceived(String titleText) {
        if (titleText == null || titleText.isEmpty()) return;
        if (ModConfig.soundLogEnabled) {
            String entry = "[TITLE] " + titleText;
            soundLog.add(entry);
            if (soundLog.size() > MAX_SOUND_LOG) soundLog.remove(0);
        }
    }

    // ========================================
    //  BITE TRIGGER (shared by audio + visual)
    // ========================================
    private void triggerBite(String source) {
        if (!isHoldingRod()) return;

        // Miss chance: only when Flaming Flay is disabled
        if (!ModConfig.flamingFlayEnabled && ModConfig.missChancePercent > 0
                && random.nextInt(100) < ModConfig.missChancePercent) {
            missCount++;
            chat(Formatting.GRAY, "Missed a bite (human sim)");
            return;
        }

        int reelDelay = humanizedDelay(ModConfig.reelDelayMinTicks, ModConfig.reelDelayMaxTicks);
        phase = Phase.REEL_DELAY;
        delayTicks = reelDelay;
        chat(Formatting.AQUA, "Bite detected! (" + source + ")");
    }

    // ========================================
    //  CHAT DETECTION (called from mixin)
    // ========================================
    public void onChatMessage(String message) {
        if (!enabled || !ModConfig.whisperPauseEnabled) return;

        // Hypixel whisper formats:
        // "From [RANK] Username: message"
        // "From Username: message"
        String lower = message.toLowerCase();
        if (lower.startsWith("from ") && message.contains(": ")) {
            // Extract sender name
            String afterFrom = message.substring(5);
            int colonIdx = afterFrom.indexOf(": ");
            if (colonIdx > 0) {
                String sender = afterFrom.substring(0, colonIdx).trim();
                // Strip rank prefix like [VIP] [MVP+] etc
                if (sender.contains("] ")) {
                    sender = sender.substring(sender.lastIndexOf("] ") + 2);
                }
                lastWhisperFrom = sender;
                phase = Phase.WHISPER_PAUSE;
                chat(Formatting.RED, "PAUSED — whisper from " + sender + "! Press F6 twice to resume, or open GUI.");
            }
        }

        // Also detect staff-related messages
        if (lower.contains("a]") && lower.contains("watchdog")) {
            // Could be a Watchdog notification — pause immediately
            phase = Phase.WHISPER_PAUSE;
            lastWhisperFrom = "WATCHDOG";
            chat(Formatting.DARK_RED, "PAUSED — Watchdog activity detected in chat!");
        }
    }

    // ========================================
    //  TICK HANDLER (called every client tick)
    // ========================================
    public void onClientTick() {
        if (mc.player == null || mc.world == null) return;
        if (!enabled) return;

        totalTicks++;

        // --- Failsafe timer ---
        if (totalTicks / 20 / 60 >= ModConfig.failsafeMinutes) {
            chat(Formatting.YELLOW, "Failsafe: auto-stopped after " + ModConfig.failsafeMinutes + " min.");
            stop();
            return;
        }

        // --- Camera jitter (runs during WAITING_FOR_BITE) ---
        if (ModConfig.cameraJitterEnabled && phase == Phase.WAITING_FOR_BITE) {
            tickCameraJitter();
        }

        // --- Visual detection: scan for "!!!" armor stand ---
        if (phase == Phase.WAITING_FOR_BITE) {
            scanForBiteArmorStand();
        }

        // --- Anti-AFK ---
        if (ModConfig.antiAfkEnabled && totalTicks - lastAfkTick > ModConfig.antiAfkIntervalSec * 20) {
            lastAfkTick = totalTicks;
            doAntiAfk();
        }

        // --- Session break check ---
        if (ModConfig.sessionBreaksEnabled && totalTicks >= nextSessionBreakTick
                && (phase == Phase.WAITING_FOR_BITE || phase == Phase.RECAST_DELAY)) {
            int breakTicks = uniformRange(ModConfig.sessionBreakMinSec * 20, ModConfig.sessionBreakMaxSec * 20);
            phase = Phase.SESSION_BREAK;
            delayTicks = breakTicks;
            scheduleNextSessionBreak();
            chat(Formatting.YELLOW, "Taking a break for ~" + (breakTicks / 20) + "s (session break)");
            return;
        }

        // --- State machine ---
        switch (phase) {
            case RECAST_DELAY:
                if (delayTicks-- <= 0) {
                    doRightClick();
                    phase = Phase.CAST_WAIT;
                    delayTicks = gaussianRange(35, 50); // variable bobber land time
                }
                break;

            case CAST_WAIT:
                if (delayTicks-- <= 0) {
                    phase = Phase.WAITING_FOR_BITE;
                    scheduleNextJitter();
                }
                break;

            case REEL_DELAY:
                if (delayTicks-- <= 0) {
                    doRightClick(); // reel in
                    catchCount++;
                    catchesSinceBreak++;

                    if (ModConfig.striderFishingEnabled) {
                        // Strider: swap to weapon and spam left-click
                        if (mc.player != null) {
                            mc.player.getInventory().setSelectedSlot(ModConfig.striderWeaponSlot - 1);
                        }
                        try { Thread.sleep(uniformRange(5, 15)); } catch (InterruptedException ignored) {}
                        doLeftClick(); // first hit immediately
                        striderKillDetected = false;
                        striderClickCooldown = 0;
                        phase = Phase.STRIDER_KILL;
                        delayTicks = ModConfig.striderKillWaitMax;
                    } else if (ModConfig.flamingFlayEnabled) {
                        // Flay: swap and hold right-click — instant
                        if (mc.player != null) {
                            mc.player.getInventory().setSelectedSlot(ModConfig.flamingFlaySlot - 1);
                        }
                        doRightClick();
                        setRightClickHeld(true);
                        phase = Phase.FLAY_KILL_WAIT;
                        delayTicks = gaussianRange(ModConfig.flamingFlayKillWaitMin, ModConfig.flamingFlayKillWaitMax);
                    } else {
                        goToPostCatch();
                    }
                }
                break;

            case FLAY_SWAP:
                if (delayTicks-- <= 0) {
                    // Swap to Flaming Flay hotbar slot (config is 1-9, inventory is 0-8)
                    if (mc.player != null) {
                        mc.player.getInventory().setSelectedSlot(ModConfig.flamingFlaySlot - 1);
                    }
                    phase = Phase.FLAY_USE;
                    delayTicks = uniformRange(0, 1); // attack immediately after swap
                }
                break;

            case FLAY_USE:
                if (delayTicks-- <= 0) {
                    // Hold right-click down to use the Flaming Flay
                    setRightClickHeld(true);
                    phase = Phase.FLAY_KILL_WAIT;
                    delayTicks = gaussianRange(ModConfig.flamingFlayKillWaitMin, ModConfig.flamingFlayKillWaitMax);
                }
                break;

            case FLAY_KILL_WAIT:
                if (flayKillDetected) {
                    // Kill detected — release and swap back immediately
                    setRightClickHeld(false);
                    flayKillDetected = false;
                    phase = Phase.FLAY_RETURN;
                    delayTicks = uniformRange(0, 1);
                } else if (delayTicks-- <= 0) {
                    // Max wait elapsed — release and swap back
                    setRightClickHeld(false);
                    phase = Phase.FLAY_RETURN;
                    delayTicks = uniformRange(0, 1);
                }
                // Right-click stays held the entire time
                break;

            case FLAY_RETURN:
                if (delayTicks-- <= 0) {
                    // Swap back to fishing rod slot
                    if (mc.player != null) {
                        mc.player.getInventory().setSelectedSlot(ModConfig.fishingRodSlot - 1);
                    }
                    goToPostCatch();
                }
                break;

            case STRIDER_KILL:
                if (striderKillDetected || delayTicks-- <= 0) {
                    // Kill done or max wait — swap back to rod
                    striderKillDetected = false;
                    phase = Phase.STRIDER_RETURN;
                    delayTicks = gaussianRange(3, 8);
                } else {
                    // Configurable CPS: convert to tick interval
                    if (striderClickCooldown-- <= 0) {
                        doLeftClick();
                        int cps = Math.max(1, Math.min(20, ModConfig.striderCps));
                        // 20 ticks/sec ÷ CPS = ticks per click
                        // Use randomized rounding to hit the target CPS on average
                        float ticksPerClick = 20.0f / cps;
                        int base = (int) ticksPerClick;
                        float frac = ticksPerClick - base;
                        striderClickCooldown = random.nextFloat() < frac ? base + 1 : base;
                        if (striderClickCooldown < 1) striderClickCooldown = 1;
                    }
                }
                break;

            case STRIDER_RETURN:
                if (delayTicks-- <= 0) {
                    if (mc.player != null) {
                        mc.player.getInventory().setSelectedSlot(ModConfig.fishingRodSlot - 1);
                    }
                    goToPostCatch();
                }
                break;

            case WAITING_FOR_BITE:
                // Safety: if bobber is gone, recast
                if (mc.player.fishHook == null && totalTicks % 60 == 0) {
                    chat(Formatting.YELLOW, "Bobber lost, recasting...");
                    phase = Phase.RECAST_DELAY;
                    delayTicks = gaussianRange(10, 30);
                }
                break;

            case MICRO_BREAK:
                if (delayTicks-- <= 0) {
                    phase = Phase.RECAST_DELAY;
                    delayTicks = humanizedDelay(ModConfig.recastDelayMinTicks, ModConfig.recastDelayMaxTicks);
                }
                // Occasional camera movement during break
                if (delayTicks % 40 == 0 && random.nextInt(3) == 0) {
                    nudgeCamera(0.8f);
                }
                break;

            case SESSION_BREAK:
                if (delayTicks-- <= 0) {
                    catchesSinceBreak = 0;
                    phase = Phase.RECAST_DELAY;
                    delayTicks = gaussianRange(15, 40);
                    chat(Formatting.GREEN, "Break over, resuming...");
                }
                // Look around naturally during break
                if (delayTicks % 80 == 0 && random.nextInt(2) == 0) {
                    nudgeCamera(3.0f);
                }
                break;

            case WHISPER_PAUSE:
                // Fully stopped — user must manually resume
                break;

            default:
                break;
        }
    }

    // ========================================
    //  HUMANIZED DELAY GENERATION
    // ========================================

    /**
     * Generates a delay using Gaussian distribution centered between min and max,
     * with fatigue added on top.
     */
    private int humanizedDelay(int minTicks, int maxTicks) {
        int base;
        if (ModConfig.gaussianDelays) {
            base = gaussianRange(minTicks, maxTicks);
        } else {
            base = uniformRange(minTicks, maxTicks);
        }

        // Add fatigue: slowly increasing delay over the session
        if (ModConfig.fatigueEnabled) {
            double minutesElapsed = totalTicks / 20.0 / 60.0;
            int fatigueExtra = (int) (minutesElapsed * ModConfig.fatigueRatePerMinute);
            fatigueExtra = Math.min(fatigueExtra, ModConfig.fatigueCapTicks);
            base += fatigueExtra;
        }

        return Math.max(1, base);
    }

    /**
     * Gaussian distribution clamped to [min, max].
     * Mean = (min+max)/2, stddev = (max-min)/4 so ~95% falls within range.
     */
    private int gaussianRange(int min, int max) {
        if (min >= max) return min;
        double mean = (min + max) / 2.0;
        double stddev = (max - min) / 4.0;
        double value = mean + random.nextGaussian() * stddev;
        return (int) Math.round(Math.max(min, Math.min(max, value)));
    }

    private int uniformRange(int min, int max) {
        if (min >= max) return min;
        return min + random.nextInt(max - min + 1);
    }

    // ========================================
    //  MICRO-BREAK LOGIC
    // ========================================

    private boolean shouldMicroBreak() {
        if (!ModConfig.microBreaksEnabled) return false;
        return random.nextInt(100) < ModConfig.microBreakChancePercent;
    }

    // ========================================
    //  SESSION BREAK SCHEDULING
    // ========================================

    private void scheduleNextSessionBreak() {
        int baseInterval = ModConfig.sessionBreakIntervalMin * 60 * 20; // ticks
        int variance = ModConfig.sessionBreakIntervalVarianceMin * 60 * 20;
        int jitter = variance > 0 ? random.nextInt(variance * 2) - variance : 0;
        nextSessionBreakTick = totalTicks + baseInterval + jitter;
    }

    // ========================================
    //  CAMERA JITTER (natural look movement)
    // ========================================

    private void scheduleNextJitter() {
        nextCameraJitterTick = totalTicks + uniformRange(
                ModConfig.cameraJitterIntervalMinTicks,
                ModConfig.cameraJitterIntervalMaxTicks
        );
    }

    private void tickCameraJitter() {
        // Process ongoing smooth jitter movement
        if (jitterStepsTodo > 0 && mc.player != null) {
            mc.player.setYaw(mc.player.getYaw() + jitterYawPerStep);
            mc.player.setPitch(mc.player.getPitch() + jitterPitchPerStep);
            jitterStepsTodo--;
            return;
        }

        // Schedule new jitter
        if (totalTicks >= nextCameraJitterTick) {
            nudgeCamera(ModConfig.cameraJitterMaxDegrees);
            scheduleNextJitter();
        }
    }

    /**
     * Smooth camera nudge over multiple ticks (looks natural, not snappy).
     */
    private void nudgeCamera(float maxDeg) {
        if (mc.player == null) return;
        float yawDelta = (random.nextFloat() - 0.5f) * 2f * maxDeg;
        float pitchDelta = (random.nextFloat() - 0.5f) * 2f * maxDeg * 0.4f; // less vertical
        int steps = 5 + random.nextInt(10); // spread over 5-14 ticks

        jitterYawPerStep = yawDelta / steps;
        jitterPitchPerStep = pitchDelta / steps;
        jitterStepsTodo = steps;
    }

    // ========================================
    //  POST-CATCH ROUTING
    // ========================================

    private void goToPostCatch() {
        seenArmorStandIds.clear(); // reset for next catch
        if (shouldMicroBreak()) {
            int breakLen = gaussianRange(ModConfig.microBreakMinTicks, ModConfig.microBreakMaxTicks);
            phase = Phase.MICRO_BREAK;
            delayTicks = breakLen;
            chat(Formatting.GRAY, "Pausing for " + (breakLen / 20) + "s...");
        } else {
            phase = Phase.RECAST_DELAY;
            delayTicks = humanizedDelay(ModConfig.recastDelayMinTicks, ModConfig.recastDelayMaxTicks);
        }
    }

    // ========================================
    //  ACTIONS
    // ========================================

    private boolean isHoldingRod() {
        ClientPlayerEntity player = mc.player;
        if (player == null) return false;
        ItemStack held = player.getMainHandStack();
        if (held.getItem() instanceof FishingRodItem) return true;
        held = player.getOffHandStack();
        return held.getItem() instanceof FishingRodItem;
    }

    private void doRightClick() {
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
    }

    private void setRightClickHeld(boolean held) {
        mc.options.useKey.setPressed(held);
    }

    private void doLeftClick() {
        ((MinecraftClientAccessor) mc).invokeDoAttack();
    }

    private void setLeftClickHeld(boolean held) {
        mc.options.attackKey.setPressed(held);
    }

    private void doAntiAfk() {
        // Sneak toggle + small camera nudge
        if (mc.player != null) {
            mc.player.setSneaking(true);
            nudgeCamera(1.5f);
        }
    }

    private void chat(Formatting color, String message) {
        if (mc.player != null) {
            mc.player.sendMessage(
                    Text.literal("[Fisher] ").formatted(color)
                            .append(Text.literal(message).formatted(Formatting.WHITE)),
                    false
            );
        }
    }
}
