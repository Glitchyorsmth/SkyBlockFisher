package com.skyblockfisher.gui;

import com.skyblockfisher.FishingHandler;
import com.skyblockfisher.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class FisherScreen extends Screen {

    // Colors (ARGB)
    private static final int BG_OVERLAY   = 0xDD000000;
    private static final int PANEL_BG     = 0xFF1a1a2e;
    private static final int PANEL_BORDER = 0xFF0f3460;
    private static final int SURFACE      = 0xFF16213e;
    private static final int CYAN         = 0xFF00d2ff;
    private static final int GREEN        = 0xFF00e676;
    private static final int RED          = 0xFFff1744;
    private static final int YELLOW       = 0xFFffd600;
    private static final int TEXT_MAIN    = 0xFFe0e0e0;
    private static final int TEXT_DIM     = 0xFF667788;
    private static final int ACCENT_DIM   = 0xFF0097b2;

    private final FishingHandler handler = FishingHandler.getInstance();

    private int panelX, panelY;
    private static final int PANEL_W = 340;
    private static final int PANEL_H = 460;

    // Tab state
    private int activeTab = 0; // 0 = Main, 1 = Humanization, 2 = Flay

    // Main tab widgets
    private ButtonWidget toggleBtn;
    private ButtonWidget resumeBtn;
    private ButtonWidget detectionModeBtn;
    private TextFieldWidget soundNameField;
    private TextFieldWidget visualTriggerField;
    private TextFieldWidget reelMinField, reelMaxField;
    private TextFieldWidget recastMinField, recastMaxField;
    private TextFieldWidget failsafeField;
    private ButtonWidget antiAfkBtn;
    private ButtonWidget soundLogBtn;

    // Humanization tab widgets
    private ButtonWidget gaussianBtn;
    private ButtonWidget fatigueBtn;
    private ButtonWidget microBreakBtn;
    private TextFieldWidget microBreakChanceField;
    private ButtonWidget sessionBreakBtn;
    private TextFieldWidget sessionIntervalField;
    private TextFieldWidget missChanceField;
    private ButtonWidget cameraJitterBtn;
    private ButtonWidget whisperPauseBtn;

    // Kill tab widgets — shared
    private TextFieldWidget rodSlotField;

    // Kill tab widgets — Flaming Flay subsection
    private ButtonWidget flayToggleBtn;
    private TextFieldWidget flaySlotField;
    private TextFieldWidget flayKillMinField, flayKillMaxField;

    // Kill tab widgets — Strider subsection
    private ButtonWidget striderToggleBtn;
    private TextFieldWidget striderSlotField;
    private TextFieldWidget striderKillMaxField;
    private TextFieldWidget striderCpsField;

    // Tab buttons
    private ButtonWidget tabMainBtn;
    private ButtonWidget tabHumanBtn;
    private ButtonWidget tabKillBtn;

    private boolean showSoundLog = false;

    public FisherScreen() {
        super(Text.literal("SkyBlock Fisher"));
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;

        int lx = panelX + 15;
        int fw = PANEL_W - 30;
        int thirdW = (fw - 12) / 3;

        // --- Tab buttons ---
        tabMainBtn = ButtonWidget.builder(Text.literal("Main"), btn -> switchTab(0))
                .dimensions(lx, panelY + 32, thirdW, 16).build();
        tabHumanBtn = ButtonWidget.builder(Text.literal("Anti-Detect"), btn -> switchTab(1))
                .dimensions(lx + thirdW + 6, panelY + 32, thirdW, 16).build();
        tabKillBtn = ButtonWidget.builder(Text.literal("Killing"), btn -> switchTab(2))
                .dimensions(lx + (thirdW + 6) * 2, panelY + 32, thirdW, 16).build();
        addDrawableChild(tabMainBtn);
        addDrawableChild(tabHumanBtn);
        addDrawableChild(tabKillBtn);

        initMainTab(lx, fw);
        initHumanTab(lx, fw);
        initKillTab(lx, fw);
        switchTab(activeTab);
    }

    private void initMainTab(int lx, int fw) {
        int y = panelY + 62;

        // Toggle
        toggleBtn = ButtonWidget.builder(getToggleText(), btn -> {
            handler.toggle();
            btn.setMessage(getToggleText());
        }).dimensions(lx, y, fw, 20).build();
        addDrawableChild(toggleBtn);

        // Resume (only visible when whisper-paused)
        resumeBtn = ButtonWidget.builder(
                Text.literal("RESUME").formatted(Formatting.GREEN, Formatting.BOLD),
                btn -> {
                    handler.resumeFromPause();
                    btn.visible = false;
                }
        ).dimensions(lx, y, fw, 20).build();
        resumeBtn.visible = false;
        addDrawableChild(resumeBtn);
        y += 26;

        // Detection mode (cycles: BOTH -> AUDIO -> VISUAL -> BOTH)
        detectionModeBtn = ButtonWidget.builder(detectionModeText(), btn -> {
            String mode = ModConfig.detectionMode.toUpperCase();
            if (mode.equals("BOTH")) ModConfig.detectionMode = "AUDIO";
            else if (mode.equals("AUDIO")) ModConfig.detectionMode = "VISUAL";
            else ModConfig.detectionMode = "BOTH";
            btn.setMessage(detectionModeText());
            // Show/hide relevant fields
            updateDetectionFieldVisibility();
        }).dimensions(lx, y, fw, 20).build();
        addDrawableChild(detectionModeBtn);
        y += 26;

        // Sound name (audio detection)
        soundNameField = new TextFieldWidget(textRenderer, lx + 110, y, fw - 110, 16, Text.empty());
        soundNameField.setMaxLength(64);
        soundNameField.setText(ModConfig.targetSound);
        addDrawableChild(soundNameField);
        y += 22;

        // Visual trigger text
        visualTriggerField = new TextFieldWidget(textRenderer, lx + 110, y, fw - 110, 16, Text.empty());
        visualTriggerField.setMaxLength(32);
        visualTriggerField.setText(ModConfig.visualTriggerText);
        addDrawableChild(visualTriggerField);
        y += 26;

        // Reel delay
        reelMinField = makeField(lx + 140, y, 55, String.valueOf(ModConfig.reelDelayMinTicks));
        reelMaxField = makeField(lx + 215, y, 55, String.valueOf(ModConfig.reelDelayMaxTicks));
        addDrawableChild(reelMinField);
        addDrawableChild(reelMaxField);
        y += 24;

        // Recast delay
        recastMinField = makeField(lx + 140, y, 55, String.valueOf(ModConfig.recastDelayMinTicks));
        recastMaxField = makeField(lx + 215, y, 55, String.valueOf(ModConfig.recastDelayMaxTicks));
        addDrawableChild(recastMinField);
        addDrawableChild(recastMaxField);
        y += 24;

        // Failsafe
        failsafeField = makeField(lx + 140, y, 55, String.valueOf(ModConfig.failsafeMinutes));
        addDrawableChild(failsafeField);
        y += 28;

        // Anti-AFK
        antiAfkBtn = makeToggleBtn("Anti-AFK", ModConfig.antiAfkEnabled, lx, y, fw, btn -> {
            ModConfig.antiAfkEnabled = !ModConfig.antiAfkEnabled;
            btn.setMessage(toggleText("Anti-AFK", ModConfig.antiAfkEnabled));
        });
        addDrawableChild(antiAfkBtn);
        y += 24;

        // Sound log
        soundLogBtn = ButtonWidget.builder(
                toggleText("Sound/Event Log", showSoundLog),
                btn -> {
                    showSoundLog = !showSoundLog;
                    btn.setMessage(toggleText("Sound/Event Log", showSoundLog));
                }
        ).dimensions(lx, y, fw, 20).build();
        addDrawableChild(soundLogBtn);
    }

    private void updateDetectionFieldVisibility() {
        if (activeTab != 0) return;
        String mode = ModConfig.detectionMode.toUpperCase();
        soundNameField.visible = mode.equals("AUDIO") || mode.equals("BOTH");
        visualTriggerField.visible = mode.equals("VISUAL") || mode.equals("BOTH");
    }

    private Text detectionModeText() {
        String mode = ModConfig.detectionMode.toUpperCase();
        Formatting color;
        String label;
        switch (mode) {
            case "AUDIO":  color = Formatting.GOLD;  label = "AUDIO (ding sound)"; break;
            case "VISUAL": color = Formatting.LIGHT_PURPLE; label = "VISUAL (!!! title)"; break;
            default:       color = Formatting.GREEN;  label = "BOTH (sound + visual)"; break;
        }
        return Text.literal("Detection: ").append(Text.literal(label).formatted(color));
    }

    private void initHumanTab(int lx, int fw) {
        int y = panelY + 62;

        // Gaussian delays
        gaussianBtn = makeToggleBtn("Gaussian Delays (bell curve)", ModConfig.gaussianDelays, lx, y, fw, btn -> {
            ModConfig.gaussianDelays = !ModConfig.gaussianDelays;
            btn.setMessage(toggleText("Gaussian Delays (bell curve)", ModConfig.gaussianDelays));
        });
        addDrawableChild(gaussianBtn);
        y += 24;

        // Fatigue
        fatigueBtn = makeToggleBtn("Fatigue Simulation", ModConfig.fatigueEnabled, lx, y, fw, btn -> {
            ModConfig.fatigueEnabled = !ModConfig.fatigueEnabled;
            btn.setMessage(toggleText("Fatigue Simulation", ModConfig.fatigueEnabled));
        });
        addDrawableChild(fatigueBtn);
        y += 30;

        // Micro-breaks
        microBreakBtn = makeToggleBtn("Micro-Breaks", ModConfig.microBreaksEnabled, lx, y, fw, btn -> {
            ModConfig.microBreaksEnabled = !ModConfig.microBreaksEnabled;
            btn.setMessage(toggleText("Micro-Breaks", ModConfig.microBreaksEnabled));
        });
        addDrawableChild(microBreakBtn);
        y += 22;

        // Micro-break chance
        microBreakChanceField = makeField(lx + 160, y, 40, String.valueOf(ModConfig.microBreakChancePercent));
        addDrawableChild(microBreakChanceField);
        y += 28;

        // Session breaks
        sessionBreakBtn = makeToggleBtn("Session Breaks", ModConfig.sessionBreaksEnabled, lx, y, fw, btn -> {
            ModConfig.sessionBreaksEnabled = !ModConfig.sessionBreaksEnabled;
            btn.setMessage(toggleText("Session Breaks", ModConfig.sessionBreaksEnabled));
        });
        addDrawableChild(sessionBreakBtn);
        y += 22;

        // Session break interval
        sessionIntervalField = makeField(lx + 160, y, 40, String.valueOf(ModConfig.sessionBreakIntervalMin));
        addDrawableChild(sessionIntervalField);
        y += 28;

        // Miss chance
        missChanceField = makeField(lx + 160, y, 40, String.valueOf(ModConfig.missChancePercent));
        addDrawableChild(missChanceField);
        y += 28;

        // Camera jitter
        cameraJitterBtn = makeToggleBtn("Camera Jitter", ModConfig.cameraJitterEnabled, lx, y, fw, btn -> {
            ModConfig.cameraJitterEnabled = !ModConfig.cameraJitterEnabled;
            btn.setMessage(toggleText("Camera Jitter", ModConfig.cameraJitterEnabled));
        });
        addDrawableChild(cameraJitterBtn);
        y += 24;

        // Whisper pause
        whisperPauseBtn = makeToggleBtn("Pause on Whisper", ModConfig.whisperPauseEnabled, lx, y, fw, btn -> {
            ModConfig.whisperPauseEnabled = !ModConfig.whisperPauseEnabled;
            btn.setMessage(toggleText("Pause on Whisper", ModConfig.whisperPauseEnabled));
        });
        addDrawableChild(whisperPauseBtn);
    }

    private void initKillTab(int lx, int fw) {
        int y = panelY + 62;

        // --- Shared: Rod slot ---
        rodSlotField = makeField(lx + 180, y, 30, String.valueOf(ModConfig.fishingRodSlot));
        addDrawableChild(rodSlotField);
        y += 28;

        // --- Flaming Flay subsection ---
        flayToggleBtn = makeToggleBtn("Flaming Flay", ModConfig.flamingFlayEnabled, lx, y, fw, btn -> {
            ModConfig.flamingFlayEnabled = !ModConfig.flamingFlayEnabled;
            if (ModConfig.flamingFlayEnabled) ModConfig.striderFishingEnabled = false;
            btn.setMessage(toggleText("Flaming Flay", ModConfig.flamingFlayEnabled));
            striderToggleBtn.setMessage(toggleText("Strider Kill", ModConfig.striderFishingEnabled));
        });
        addDrawableChild(flayToggleBtn);
        y += 22;

        flaySlotField = makeField(lx + 180, y, 30, String.valueOf(ModConfig.flamingFlaySlot));
        addDrawableChild(flaySlotField);
        y += 22;

        flayKillMinField = makeField(lx + 180, y, 40, String.valueOf(ModConfig.flamingFlayKillWaitMin));
        addDrawableChild(flayKillMinField);
        y += 22;
        flayKillMaxField = makeField(lx + 180, y, 40, String.valueOf(ModConfig.flamingFlayKillWaitMax));
        addDrawableChild(flayKillMaxField);
        y += 32;

        // --- Strider subsection ---
        striderToggleBtn = makeToggleBtn("Strider Kill", ModConfig.striderFishingEnabled, lx, y, fw, btn -> {
            ModConfig.striderFishingEnabled = !ModConfig.striderFishingEnabled;
            if (ModConfig.striderFishingEnabled) ModConfig.flamingFlayEnabled = false;
            btn.setMessage(toggleText("Strider Kill", ModConfig.striderFishingEnabled));
            flayToggleBtn.setMessage(toggleText("Flaming Flay", ModConfig.flamingFlayEnabled));
        });
        addDrawableChild(striderToggleBtn);
        y += 22;

        striderSlotField = makeField(lx + 180, y, 30, String.valueOf(ModConfig.striderWeaponSlot));
        addDrawableChild(striderSlotField);
        y += 22;

        striderKillMaxField = makeField(lx + 180, y, 40, String.valueOf(ModConfig.striderKillWaitMax));
        addDrawableChild(striderKillMaxField);
        y += 22;

        striderCpsField = makeField(lx + 180, y, 40, String.valueOf(ModConfig.striderCps));
        addDrawableChild(striderCpsField);
    }

    private void switchTab(int tab) {
        activeTab = tab;
        // Main tab widgets
        boolean main = tab == 0;
        toggleBtn.visible = main && handler.getPhase() != FishingHandler.Phase.WHISPER_PAUSE;
        resumeBtn.visible = main && handler.getPhase() == FishingHandler.Phase.WHISPER_PAUSE;
        detectionModeBtn.visible = main;
        reelMinField.visible = main;
        reelMaxField.visible = main;
        recastMinField.visible = main;
        recastMaxField.visible = main;
        failsafeField.visible = main;
        antiAfkBtn.visible = main;
        soundLogBtn.visible = main;
        if (main) {
            updateDetectionFieldVisibility();
        } else {
            soundNameField.visible = false;
            visualTriggerField.visible = false;
        }

        // Human tab widgets
        boolean human = tab == 1;
        gaussianBtn.visible = human;
        fatigueBtn.visible = human;
        microBreakBtn.visible = human;
        microBreakChanceField.visible = human;
        sessionBreakBtn.visible = human;
        sessionIntervalField.visible = human;
        missChanceField.visible = human;
        cameraJitterBtn.visible = human;
        whisperPauseBtn.visible = human;

        // Kill tab widgets
        boolean kill = tab == 2;
        rodSlotField.visible = kill;
        flayToggleBtn.visible = kill;
        flaySlotField.visible = kill;
        flayKillMinField.visible = kill;
        flayKillMaxField.visible = kill;
        striderToggleBtn.visible = kill;
        striderSlotField.visible = kill;
        striderKillMaxField.visible = kill;
        striderCpsField.visible = kill;
    }

    // ---- Rendering ----

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, BG_OVERLAY);
        drawBorderedRect(ctx, panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, PANEL_BG, PANEL_BORDER);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("SkyBlock Fisher").formatted(Formatting.AQUA, Formatting.BOLD),
                width / 2, panelY + 8, CYAN);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Anti-Detect Edition").formatted(Formatting.GRAY),
                width / 2, panelY + 20, TEXT_DIM);

        // Tab underline
        int tabY = panelY + 48;
        int thirdW = (PANEL_W - 42) / 3;
        int tabStartX = panelX + 15 + activeTab * (thirdW + 6);
        ctx.fill(tabStartX, tabY, tabStartX + thirdW, tabY + 2, CYAN);

        // Stats bar
        int sy = panelY + PANEL_H - 50;
        ctx.fill(panelX + 15, sy, panelX + PANEL_W - 15, sy + 22, SURFACE);

        int catches = handler.getCatchCount();
        int misses = handler.getMissCount();
        int secs = handler.getElapsedSeconds();

        ctx.drawTextWithShadow(textRenderer,
                String.format("Catches: %s%d %s(%d missed)", Formatting.GREEN, catches, Formatting.GRAY, misses),
                panelX + 22, sy + 7, TEXT_MAIN);
        ctx.drawTextWithShadow(textRenderer,
                String.format("%s%02d:%02d", Formatting.AQUA, secs / 60, secs % 60),
                panelX + PANEL_W - 55, sy + 7, TEXT_MAIN);

        // Status line
        String status = handler.getStatusText();
        int statusColor = handler.isEnabled() ? (handler.getPhase() == FishingHandler.Phase.WHISPER_PAUSE ? RED : GREEN) : TEXT_DIM;
        ctx.fill(panelX + 15, sy + 24, panelX + 15 + 4, sy + 24 + 10, statusColor);
        ctx.drawTextWithShadow(textRenderer, status, panelX + 24, sy + 25, TEXT_MAIN);

        // Update toggle/resume visibility
        if (activeTab == 0) {
            boolean whisperPaused = handler.getPhase() == FishingHandler.Phase.WHISPER_PAUSE;
            toggleBtn.visible = !whisperPaused;
            resumeBtn.visible = whisperPaused;
        }

        // Widgets
        super.render(ctx, mouseX, mouseY, delta);

        // Tab-specific labels
        int lx = panelX + 15;
        if (activeTab == 0) {
            renderMainLabels(ctx, lx);
        } else if (activeTab == 1) {
            renderHumanLabels(ctx, lx);
        } else {
            renderKillLabels(ctx, lx);
        }

        // Hotkey hint
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("RShift: GUI  |  F6: Toggle  |  ESC: Close").formatted(Formatting.DARK_GRAY),
                width / 2, panelY + PANEL_H - 14, TEXT_DIM);

        // Sound log panel
        if (showSoundLog && activeTab == 0) {
            renderSoundLog(ctx);
        }
    }

    private void renderMainLabels(DrawContext ctx, int lx) {
        int y = panelY + 62 + 26 + 26; // after toggle + detection mode buttons
        String mode = ModConfig.detectionMode.toUpperCase();

        // Sound name label (only if AUDIO or BOTH)
        if (mode.equals("AUDIO") || mode.equals("BOTH")) {
            ctx.drawTextWithShadow(textRenderer, "Target Sound:", lx, y + 4, TEXT_DIM);
        }
        y += 22;

        // Visual trigger label (only if VISUAL or BOTH)
        if (mode.equals("VISUAL") || mode.equals("BOTH")) {
            ctx.drawTextWithShadow(textRenderer, "Visual Text:", lx, y + 4, TEXT_DIM);
        }
        y += 26;

        ctx.drawTextWithShadow(textRenderer, "Reel Delay (ticks):", lx, y + 4, TEXT_DIM);
        ctx.drawTextWithShadow(textRenderer, "-", lx + 200, y + 4, TEXT_DIM);
        y += 24;
        ctx.drawTextWithShadow(textRenderer, "Recast Delay (ticks):", lx, y + 4, TEXT_DIM);
        ctx.drawTextWithShadow(textRenderer, "-", lx + 200, y + 4, TEXT_DIM);
        y += 24;
        ctx.drawTextWithShadow(textRenderer, "Failsafe (minutes):", lx, y + 4, TEXT_DIM);
    }

    private void renderHumanLabels(DrawContext ctx, int lx) {
        int y = panelY + 62;

        // Section header
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("These features make your fishing look human").formatted(Formatting.DARK_GRAY).getString(),
                lx, panelY + 52, TEXT_DIM);

        y += 54; // after gaussian + fatigue buttons
        // Skip to micro-break chance label
        y += 22;
        ctx.drawTextWithShadow(textRenderer, "Break chance (%):", lx, y + 4, TEXT_DIM);

        y += 28; // session breaks button
        y += 22;
        ctx.drawTextWithShadow(textRenderer, "Break interval (min):", lx, y + 4, TEXT_DIM);

        y += 28;
        ctx.drawTextWithShadow(textRenderer, "Miss chance (%):", lx, y + 4, TEXT_DIM);
    }

    private void renderKillLabels(DrawContext ctx, int lx) {
        int y = panelY + 62;

        // Shared rod slot
        ctx.drawTextWithShadow(textRenderer, "Fishing Rod Slot (1-9):", lx, y + 4, TEXT_DIM);
        y += 28;

        // --- Flaming Flay section header ---
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("--- Flaming Flay ---").formatted(Formatting.GOLD).getString(),
                lx, y - 8, YELLOW);
        y += 22; // after toggle

        ctx.drawTextWithShadow(textRenderer, "Flay Slot (1-9):", lx + 10, y + 4, TEXT_DIM);
        y += 22;
        ctx.drawTextWithShadow(textRenderer, "Kill Wait Min (ticks):", lx + 10, y + 4, TEXT_DIM);
        y += 22;
        ctx.drawTextWithShadow(textRenderer, "Kill Wait Max (ticks):", lx + 10, y + 4, TEXT_DIM);
        y += 32;

        // --- Strider section header ---
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("--- Strider Kill ---").formatted(Formatting.RED).getString(),
                lx, y - 8, RED);
        y += 22; // after toggle

        ctx.drawTextWithShadow(textRenderer, "Weapon Slot (1-9):", lx + 10, y + 4, TEXT_DIM);
        y += 22;
        ctx.drawTextWithShadow(textRenderer, "Kill Wait Max (ticks):", lx + 10, y + 4, TEXT_DIM);
        y += 22;
        ctx.drawTextWithShadow(textRenderer, "CPS (1-20):", lx + 10, y + 4, TEXT_DIM);
    }

    private void renderSoundLog(DrawContext ctx) {
        int logX = panelX;
        int logY = panelY + PANEL_H + 4;
        int logH = 140;

        drawBorderedRect(ctx, logX, logY, logX + PANEL_W, logY + logH, PANEL_BG, PANEL_BORDER);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("Event Log:").formatted(Formatting.AQUA).getString(),
                logX + 8, logY + 4, CYAN);

        List<String> log = new ArrayList<>(handler.getSoundLog());
        int ly = logY + 16;
        int maxLines = (logH - 20) / 11;
        int start = Math.max(0, log.size() - maxLines);
        for (int i = start; i < log.size(); i++) {
            String entry = log.get(i);
            int col;
            if (entry.startsWith("[TITLE]")) {
                col = entry.contains(ModConfig.visualTriggerText) ? 0xFFff69b4 : 0xFFaa88cc; // pink if match
            } else if (entry.contains(ModConfig.targetSound)) {
                col = GREEN;
            } else {
                col = TEXT_DIM;
            }
            ctx.drawTextWithShadow(textRenderer, entry, logX + 8, ly, col);
            ly += 11;
            if (ly > logY + logH - 6) break;
        }
    }

    // ---- Helpers ----

    private void drawBorderedRect(DrawContext ctx, int x1, int y1, int x2, int y2, int fill, int border) {
        ctx.fill(x1, y1, x2, y2, border);
        ctx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, fill);
    }

    private Text getToggleText() {
        return handler.isEnabled()
                ? Text.literal("STOP").formatted(Formatting.RED, Formatting.BOLD)
                : Text.literal("START").formatted(Formatting.GREEN, Formatting.BOLD);
    }

    private Text toggleText(String label, boolean on) {
        return Text.literal(label + ": ")
                .append(on ? Text.literal("ON").formatted(Formatting.GREEN)
                           : Text.literal("OFF").formatted(Formatting.RED));
    }

    private ButtonWidget makeToggleBtn(String label, boolean initial, int x, int y, int w,
                                        ButtonWidget.PressAction action) {
        return ButtonWidget.builder(toggleText(label, initial), action)
                .dimensions(x, y, w, 20).build();
    }

    private TextFieldWidget makeField(int x, int y, int w, String value) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, w, 16, Text.empty());
        field.setMaxLength(8);
        field.setText(value);
        return field;
    }

    @Override
    public void close() {
        applySettings();
        super.close();
    }

    private void applySettings() {
        ModConfig.targetSound = soundNameField.getText().trim();
        ModConfig.visualTriggerText = visualTriggerField.getText().trim();
        ModConfig.reelDelayMinTicks = parseIntSafe(reelMinField.getText(), 6);
        ModConfig.reelDelayMaxTicks = parseIntSafe(reelMaxField.getText(), 14);
        ModConfig.recastDelayMinTicks = parseIntSafe(recastMinField.getText(), 16);
        ModConfig.recastDelayMaxTicks = parseIntSafe(recastMaxField.getText(), 30);
        ModConfig.failsafeMinutes = parseIntSafe(failsafeField.getText(), 60);
        ModConfig.microBreakChancePercent = parseIntSafe(microBreakChanceField.getText(), 12);
        ModConfig.sessionBreakIntervalMin = parseIntSafe(sessionIntervalField.getText(), 25);
        ModConfig.missChancePercent = parseIntSafe(missChanceField.getText(), 4);
        ModConfig.fishingRodSlot = Math.max(1, Math.min(9, parseIntSafe(rodSlotField.getText(), 1)));
        ModConfig.flamingFlaySlot = Math.max(1, Math.min(9, parseIntSafe(flaySlotField.getText(), 2)));
        ModConfig.flamingFlayKillWaitMin = parseIntSafe(flayKillMinField.getText(), 20);
        ModConfig.flamingFlayKillWaitMax = parseIntSafe(flayKillMaxField.getText(), 40);
        ModConfig.striderWeaponSlot = Math.max(1, Math.min(9, parseIntSafe(striderSlotField.getText(), 3)));
        ModConfig.striderKillWaitMax = parseIntSafe(striderKillMaxField.getText(), 100);
        ModConfig.striderCps = Math.max(1, Math.min(20, parseIntSafe(striderCpsField.getText(), 14)));
        ModConfig.save();
    }

    private int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return fallback; }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
