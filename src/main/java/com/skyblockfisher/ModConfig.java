package com.skyblockfisher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("skyblockfisher.json");

    // --- Detection mode: "AUDIO", "VISUAL", "BOTH" ---
    public static String detectionMode = "BOTH";

    // --- Sound (for AUDIO / BOTH mode) ---
    public static String targetSound = "block.note_block.pling";
    public static boolean soundLogEnabled = true;

    // --- Visual (for VISUAL / BOTH mode) ---
    public static String visualTriggerText = "!!!";

    // --- Base delays (ticks, 20 = 1 second) ---
    public static int reelDelayMinTicks = 6;     // ~0.3s
    public static int reelDelayMaxTicks = 14;    // ~0.7s
    public static int recastDelayMinTicks = 16;   // ~0.8s
    public static int recastDelayMaxTicks = 30;   // ~1.5s

    // --- Humanization ---
    // Gaussian: delays use bell-curve distribution centered between min/max
    public static boolean gaussianDelays = true;

    // Fatigue: reaction times slowly increase over the session
    public static boolean fatigueEnabled = true;
    public static double fatigueRatePerMinute = 0.5;  // extra ticks added per minute
    public static int fatigueCapTicks = 20;            // max extra ticks from fatigue

    // Micro-breaks: short pauses every few catches
    public static boolean microBreaksEnabled = true;
    public static int microBreakChancePercent = 12;    // % chance after each catch
    public static int microBreakMinTicks = 40;         // 2s
    public static int microBreakMaxTicks = 300;        // 15s

    // Session breaks: longer pauses at intervals
    public static boolean sessionBreaksEnabled = true;
    public static int sessionBreakIntervalMin = 25;    // every ~25 min
    public static int sessionBreakIntervalVarianceMin = 8; // +/- 8 min
    public static int sessionBreakMinSec = 60;         // 1 min break
    public static int sessionBreakMaxSec = 180;        // 3 min break

    // Miss chance: occasionally fail to reel in
    public static int missChancePercent = 4;           // ~4% of bites missed

    // Camera jitter: small random look movements while waiting
    public static boolean cameraJitterEnabled = true;
    public static int cameraJitterIntervalMinTicks = 100;  // 5s
    public static int cameraJitterIntervalMaxTicks = 600;  // 30s
    public static float cameraJitterMaxDegrees = 2.5f;     // subtle

    // --- Flaming Flay ---
    public static boolean flamingFlayEnabled = false;
    public static int fishingRodSlot = 1;              // hotbar slot 1-9
    public static int flamingFlaySlot = 2;             // hotbar slot 1-9
    public static int flamingFlayKillWaitMin = 40;     // ~2s
    public static int flamingFlayKillWaitMax = 80;     // ~4s (max wait — exits early on kill sound)

    // --- Safety ---
    public static boolean antiAfkEnabled = true;
    public static int antiAfkIntervalSec = 240;
    public static int failsafeMinutes = 60;

    // Whisper/DM detection: pause if someone messages you
    public static boolean whisperPauseEnabled = true;

    // --- Persistence ---
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                ConfigData data = GSON.fromJson(json, ConfigData.class);
                if (data != null) data.apply();
            } catch (IOException e) {
                System.out.println("[SkyBlockFisher] Failed to load config: " + e.getMessage());
            }
        } else {
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(new ConfigData()));
        } catch (IOException e) {
            System.out.println("[SkyBlockFisher] Failed to save config: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private static class ConfigData {
        String detectionMode = ModConfig.detectionMode;
        String targetSound = ModConfig.targetSound;
        boolean soundLogEnabled = ModConfig.soundLogEnabled;
        String visualTriggerText = ModConfig.visualTriggerText;
        int reelDelayMinTicks = ModConfig.reelDelayMinTicks;
        int reelDelayMaxTicks = ModConfig.reelDelayMaxTicks;
        int recastDelayMinTicks = ModConfig.recastDelayMinTicks;
        int recastDelayMaxTicks = ModConfig.recastDelayMaxTicks;
        boolean gaussianDelays = ModConfig.gaussianDelays;
        boolean fatigueEnabled = ModConfig.fatigueEnabled;
        double fatigueRatePerMinute = ModConfig.fatigueRatePerMinute;
        int fatigueCapTicks = ModConfig.fatigueCapTicks;
        boolean microBreaksEnabled = ModConfig.microBreaksEnabled;
        int microBreakChancePercent = ModConfig.microBreakChancePercent;
        int microBreakMinTicks = ModConfig.microBreakMinTicks;
        int microBreakMaxTicks = ModConfig.microBreakMaxTicks;
        boolean sessionBreaksEnabled = ModConfig.sessionBreaksEnabled;
        int sessionBreakIntervalMin = ModConfig.sessionBreakIntervalMin;
        int sessionBreakIntervalVarianceMin = ModConfig.sessionBreakIntervalVarianceMin;
        int sessionBreakMinSec = ModConfig.sessionBreakMinSec;
        int sessionBreakMaxSec = ModConfig.sessionBreakMaxSec;
        int missChancePercent = ModConfig.missChancePercent;
        boolean cameraJitterEnabled = ModConfig.cameraJitterEnabled;
        int cameraJitterIntervalMinTicks = ModConfig.cameraJitterIntervalMinTicks;
        int cameraJitterIntervalMaxTicks = ModConfig.cameraJitterIntervalMaxTicks;
        float cameraJitterMaxDegrees = ModConfig.cameraJitterMaxDegrees;
        boolean flamingFlayEnabled = ModConfig.flamingFlayEnabled;
        int fishingRodSlot = ModConfig.fishingRodSlot;
        int flamingFlaySlot = ModConfig.flamingFlaySlot;
        int flamingFlayKillWaitMin = ModConfig.flamingFlayKillWaitMin;
        int flamingFlayKillWaitMax = ModConfig.flamingFlayKillWaitMax;
        boolean antiAfkEnabled = ModConfig.antiAfkEnabled;
        int antiAfkIntervalSec = ModConfig.antiAfkIntervalSec;
        int failsafeMinutes = ModConfig.failsafeMinutes;
        boolean whisperPauseEnabled = ModConfig.whisperPauseEnabled;

        void apply() {
            ModConfig.detectionMode = detectionMode;
            ModConfig.targetSound = targetSound;
            ModConfig.soundLogEnabled = soundLogEnabled;
            ModConfig.visualTriggerText = visualTriggerText;
            ModConfig.reelDelayMinTicks = reelDelayMinTicks;
            ModConfig.reelDelayMaxTicks = reelDelayMaxTicks;
            ModConfig.recastDelayMinTicks = recastDelayMinTicks;
            ModConfig.recastDelayMaxTicks = recastDelayMaxTicks;
            ModConfig.gaussianDelays = gaussianDelays;
            ModConfig.fatigueEnabled = fatigueEnabled;
            ModConfig.fatigueRatePerMinute = fatigueRatePerMinute;
            ModConfig.fatigueCapTicks = fatigueCapTicks;
            ModConfig.microBreaksEnabled = microBreaksEnabled;
            ModConfig.microBreakChancePercent = microBreakChancePercent;
            ModConfig.microBreakMinTicks = microBreakMinTicks;
            ModConfig.microBreakMaxTicks = microBreakMaxTicks;
            ModConfig.sessionBreaksEnabled = sessionBreaksEnabled;
            ModConfig.sessionBreakIntervalMin = sessionBreakIntervalMin;
            ModConfig.sessionBreakIntervalVarianceMin = sessionBreakIntervalVarianceMin;
            ModConfig.sessionBreakMinSec = sessionBreakMinSec;
            ModConfig.sessionBreakMaxSec = sessionBreakMaxSec;
            ModConfig.missChancePercent = missChancePercent;
            ModConfig.cameraJitterEnabled = cameraJitterEnabled;
            ModConfig.cameraJitterIntervalMinTicks = cameraJitterIntervalMinTicks;
            ModConfig.cameraJitterIntervalMaxTicks = cameraJitterIntervalMaxTicks;
            ModConfig.cameraJitterMaxDegrees = cameraJitterMaxDegrees;
            ModConfig.flamingFlayEnabled = flamingFlayEnabled;
            ModConfig.fishingRodSlot = fishingRodSlot;
            ModConfig.flamingFlaySlot = flamingFlaySlot;
            ModConfig.flamingFlayKillWaitMin = flamingFlayKillWaitMin;
            ModConfig.flamingFlayKillWaitMax = flamingFlayKillWaitMax;
            ModConfig.antiAfkEnabled = antiAfkEnabled;
            ModConfig.antiAfkIntervalSec = antiAfkIntervalSec;
            ModConfig.failsafeMinutes = failsafeMinutes;
            ModConfig.whisperPauseEnabled = whisperPauseEnabled;
        }
    }
}
