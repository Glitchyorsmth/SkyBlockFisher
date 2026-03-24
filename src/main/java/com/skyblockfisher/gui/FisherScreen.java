package com.skyblockfisher.gui;

import com.skyblockfisher.FishingHandler;
import com.skyblockfisher.ModConfig;
import com.skyblockfisher.UpdateChecker;
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
    private static final int SECTION_BG   = 0xFF111828;

    private final FishingHandler handler = FishingHandler.getInstance();

    private int panelX, panelY, panelW, panelH;

    // Tab state: 0 = Main, 1 = Anti-Detect, 2 = Killing
    private int activeTab = 0;

    // Dropdown section states for Anti-Detect tab
    private boolean sectionHumanOpen = true;
    private boolean sectionSeaCreatureOpen = false;
    private boolean sectionSafetyOpen = false;

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
    private ButtonWidget chatFeedbackBtn;
    private ButtonWidget soundLogBtn;

    // Anti-Detect: Humanization widgets
    private ButtonWidget sectionHumanBtn;
    private ButtonWidget gaussianBtn;
    private ButtonWidget fatigueBtn;
    private ButtonWidget microBreakBtn;
    private TextFieldWidget microBreakChanceField;
    private ButtonWidget sessionBreakBtn;
    private TextFieldWidget sessionIntervalField;
    private TextFieldWidget missChanceField;
    private ButtonWidget cameraJitterBtn;

    // Anti-Detect: Sea Creature widgets
    private ButtonWidget sectionSeaCreatureBtn;
    private ButtonWidget thunderBtn;
    private ButtonWidget jawbusBtn;
    private ButtonWidget scuttlerBtn;
    private ButtonWidget ragnarokBtn;

    // Anti-Detect: Safety widgets
    private ButtonWidget sectionSafetyBtn;
    private ButtonWidget whisperPauseBtn;
    private ButtonWidget deathPauseBtn;

    // Kill tab widgets
    private TextFieldWidget rodSlotField;
    private ButtonWidget flayToggleBtn;
    private TextFieldWidget flaySlotField;
    private TextFieldWidget flayKillMinField, flayKillMaxField;
    private ButtonWidget striderToggleBtn;
    private TextFieldWidget striderSlotField;
    private TextFieldWidget striderKillMaxField;
    private TextFieldWidget striderCpsField;

    // Tab buttons
    private ButtonWidget tabMainBtn;
    private ButtonWidget tabAntiDetectBtn;
    private ButtonWidget tabKillBtn;

    // Update widgets
    private ButtonWidget updateBtn;
    private boolean showChangelog = false;
    private boolean showSoundLog = false;

    public FisherScreen() {
        super(Text.literal("SkyBlock Fisher"));
    }

    @Override
    protected void init() {
        panelW = Math.min(340, width - 20);
        panelH = Math.min(460, height - 20);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        int lx = panelX + 15;
        int fw = panelW - 30;
        int thirdW = (fw - 12) / 3;

        // --- Tab buttons ---
        tabMainBtn = ButtonWidget.builder(Text.literal("Main"), btn -> switchTab(0))
                .dimensions(lx, panelY + 32, thirdW, 16).build();
        tabAntiDetectBtn = ButtonWidget.builder(Text.literal("Anti-Detect"), btn -> switchTab(1))
                .dimensions(lx + thirdW + 6, panelY + 32, thirdW, 16).build();
        tabKillBtn = ButtonWidget.builder(Text.literal("Killing"), btn -> switchTab(2))
                .dimensions(lx + (thirdW + 6) * 2, panelY + 32, thirdW, 16).build();
        addDrawableChild(tabMainBtn);
        addDrawableChild(tabAntiDetectBtn);
        addDrawableChild(tabKillBtn);

        // --- Update button ---
        // First click: show changelog. Second click: download. After download: toggle changelog.
        updateBtn = ButtonWidget.builder(getUpdateBtnText(), btn -> {
            if (UpdateChecker.isDownloaded()) {
                showChangelog = !showChangelog;
            } else if (UpdateChecker.isDownloading()) {
                // do nothing
            } else if (!showChangelog) {
                // First click — show changelog so user sees what's new
                showChangelog = true;
            } else {
                // Changelog already showing, second click — download
                UpdateChecker.downloadUpdate();
            }
            btn.setMessage(getUpdateBtnText());
        }).dimensions(panelX + panelW - 115, panelY + 4, 110, 16).build();
        updateBtn.visible = UpdateChecker.isUpdateAvailable();
        addDrawableChild(updateBtn);

        initMainTab(lx, fw);
        initAntiDetectTab(lx, fw);
        initKillTab(lx, fw);
        switchTab(activeTab);
    }

    // ======== MAIN TAB ========

    private void initMainTab(int lx, int fw) {
        int y = panelY + 62;

        toggleBtn = ButtonWidget.builder(getToggleText(), btn -> {
            handler.toggle();
            btn.setMessage(getToggleText());
        }).dimensions(lx, y, fw, 20).build();
        addDrawableChild(toggleBtn);

        resumeBtn = ButtonWidget.builder(
                Text.literal("RESUME").formatted(Formatting.GREEN, Formatting.BOLD),
                btn -> { handler.resumeFromPause(); btn.visible = false; }
        ).dimensions(lx, y, fw, 20).build();
        resumeBtn.visible = false;
        addDrawableChild(resumeBtn);
        y += 26;

        detectionModeBtn = ButtonWidget.builder(detectionModeText(), btn -> {
            String mode = ModConfig.detectionMode.toUpperCase();
            if (mode.equals("BOTH")) ModConfig.detectionMode = "AUDIO";
            else if (mode.equals("AUDIO")) ModConfig.detectionMode = "VISUAL";
            else ModConfig.detectionMode = "BOTH";
            btn.setMessage(detectionModeText());
            updateDetectionFieldVisibility();
        }).dimensions(lx, y, fw, 20).build();
        addDrawableChild(detectionModeBtn);
        y += 26;

        soundNameField = new TextFieldWidget(textRenderer, lx + 110, y, fw - 110, 16, Text.empty());
        soundNameField.setMaxLength(64);
        soundNameField.setText(ModConfig.targetSound);
        addDrawableChild(soundNameField);
        y += 22;

        visualTriggerField = new TextFieldWidget(textRenderer, lx + 110, y, fw - 110, 16, Text.empty());
        visualTriggerField.setMaxLength(32);
        visualTriggerField.setText(ModConfig.visualTriggerText);
        addDrawableChild(visualTriggerField);
        y += 26;

        reelMinField = makeField(lx + 140, y, 55, String.valueOf(ModConfig.reelDelayMinTicks));
        reelMaxField = makeField(lx + 215, y, 55, String.valueOf(ModConfig.reelDelayMaxTicks));
        addDrawableChild(reelMinField);
        addDrawableChild(reelMaxField);
        y += 24;

        recastMinField = makeField(lx + 140, y, 55, String.valueOf(ModConfig.recastDelayMinTicks));
        recastMaxField = makeField(lx + 215, y, 55, String.valueOf(ModConfig.recastDelayMaxTicks));
        addDrawableChild(recastMinField);
        addDrawableChild(recastMaxField);
        y += 24;

        failsafeField = makeField(lx + 140, y, 55, String.valueOf(ModConfig.failsafeMinutes));
        addDrawableChild(failsafeField);
        y += 28;

        antiAfkBtn = makeToggleBtn("Anti-AFK", ModConfig.antiAfkEnabled, lx, y, fw, btn -> {
            ModConfig.antiAfkEnabled = !ModConfig.antiAfkEnabled;
            btn.setMessage(toggleText("Anti-AFK", ModConfig.antiAfkEnabled));
        });
        addDrawableChild(antiAfkBtn);
        y += 24;

        chatFeedbackBtn = makeToggleBtn("Chat Feedback", ModConfig.chatFeedback, lx, y, fw, btn -> {
            ModConfig.chatFeedback = !ModConfig.chatFeedback;
            btn.setMessage(toggleText("Chat Feedback", ModConfig.chatFeedback));
        });
        addDrawableChild(chatFeedbackBtn);
        y += 24;

        soundLogBtn = ButtonWidget.builder(
                toggleText("Sound/Event Log", showSoundLog),
                btn -> { showSoundLog = !showSoundLog; btn.setMessage(toggleText("Sound/Event Log", showSoundLog)); }
        ).dimensions(lx, y, fw, 20).build();
        addDrawableChild(soundLogBtn);
    }

    // ======== ANTI-DETECT TAB (with collapsible sections) ========

    private void initAntiDetectTab(int lx, int fw) {
        // All widgets are created at y=0 initially; repositioned in layoutAntiDetect()
        int y = panelY + 62;

        // --- Section: Humanization ---
        sectionHumanBtn = ButtonWidget.builder(sectionTitle("Humanization", sectionHumanOpen), btn -> {
            sectionHumanOpen = !sectionHumanOpen;
            btn.setMessage(sectionTitle("Humanization", sectionHumanOpen));
            layoutAntiDetect();
        }).dimensions(lx, y, fw, 16).build();
        addDrawableChild(sectionHumanBtn);

        gaussianBtn = makeToggleBtn("Gaussian Delays", ModConfig.gaussianDelays, lx, y, fw, btn -> {
            ModConfig.gaussianDelays = !ModConfig.gaussianDelays;
            btn.setMessage(toggleText("Gaussian Delays", ModConfig.gaussianDelays));
        });
        addDrawableChild(gaussianBtn);

        fatigueBtn = makeToggleBtn("Fatigue Simulation", ModConfig.fatigueEnabled, lx, y, fw, btn -> {
            ModConfig.fatigueEnabled = !ModConfig.fatigueEnabled;
            btn.setMessage(toggleText("Fatigue Simulation", ModConfig.fatigueEnabled));
        });
        addDrawableChild(fatigueBtn);

        microBreakBtn = makeToggleBtn("Micro-Breaks", ModConfig.microBreaksEnabled, lx, y, fw, btn -> {
            ModConfig.microBreaksEnabled = !ModConfig.microBreaksEnabled;
            btn.setMessage(toggleText("Micro-Breaks", ModConfig.microBreaksEnabled));
        });
        addDrawableChild(microBreakBtn);

        microBreakChanceField = makeField(lx + 160, y, 40, String.valueOf(ModConfig.microBreakChancePercent));
        addDrawableChild(microBreakChanceField);

        sessionBreakBtn = makeToggleBtn("Session Breaks", ModConfig.sessionBreaksEnabled, lx, y, fw, btn -> {
            ModConfig.sessionBreaksEnabled = !ModConfig.sessionBreaksEnabled;
            btn.setMessage(toggleText("Session Breaks", ModConfig.sessionBreaksEnabled));
        });
        addDrawableChild(sessionBreakBtn);

        sessionIntervalField = makeField(lx + 160, y, 40, String.valueOf(ModConfig.sessionBreakIntervalMin));
        addDrawableChild(sessionIntervalField);

        missChanceField = makeField(lx + 160, y, 40, String.valueOf(ModConfig.missChancePercent));
        addDrawableChild(missChanceField);

        cameraJitterBtn = makeToggleBtn("Camera Jitter", ModConfig.cameraJitterEnabled, lx, y, fw, btn -> {
            ModConfig.cameraJitterEnabled = !ModConfig.cameraJitterEnabled;
            btn.setMessage(toggleText("Camera Jitter", ModConfig.cameraJitterEnabled));
        });
        addDrawableChild(cameraJitterBtn);

        // --- Section: Sea Creatures ---
        sectionSeaCreatureBtn = ButtonWidget.builder(sectionTitle("Sea Creatures", sectionSeaCreatureOpen), btn -> {
            sectionSeaCreatureOpen = !sectionSeaCreatureOpen;
            btn.setMessage(sectionTitle("Sea Creatures", sectionSeaCreatureOpen));
            layoutAntiDetect();
        }).dimensions(lx, y, fw, 16).build();
        addDrawableChild(sectionSeaCreatureBtn);

        int halfW = (fw - 6) / 2;
        thunderBtn = makeToggleBtn("Thunder", ModConfig.pauseOnThunder, lx, y, halfW, btn -> {
            ModConfig.pauseOnThunder = !ModConfig.pauseOnThunder;
            btn.setMessage(toggleText("Thunder", ModConfig.pauseOnThunder));
        });
        addDrawableChild(thunderBtn);

        jawbusBtn = makeToggleBtn("Lord Jawbus", ModConfig.pauseOnJawbus, lx + halfW + 6, y, halfW, btn -> {
            ModConfig.pauseOnJawbus = !ModConfig.pauseOnJawbus;
            btn.setMessage(toggleText("Lord Jawbus", ModConfig.pauseOnJawbus));
        });
        addDrawableChild(jawbusBtn);

        scuttlerBtn = makeToggleBtn("Fiery Scuttler", ModConfig.pauseOnScuttler, lx, y, halfW, btn -> {
            ModConfig.pauseOnScuttler = !ModConfig.pauseOnScuttler;
            btn.setMessage(toggleText("Fiery Scuttler", ModConfig.pauseOnScuttler));
        });
        addDrawableChild(scuttlerBtn);

        ragnarokBtn = makeToggleBtn("Ragnarok", ModConfig.pauseOnRagnarok, lx + halfW + 6, y, halfW, btn -> {
            ModConfig.pauseOnRagnarok = !ModConfig.pauseOnRagnarok;
            btn.setMessage(toggleText("Ragnarok", ModConfig.pauseOnRagnarok));
        });
        addDrawableChild(ragnarokBtn);

        // --- Section: Safety ---
        sectionSafetyBtn = ButtonWidget.builder(sectionTitle("Safety", sectionSafetyOpen), btn -> {
            sectionSafetyOpen = !sectionSafetyOpen;
            btn.setMessage(sectionTitle("Safety", sectionSafetyOpen));
            layoutAntiDetect();
        }).dimensions(lx, y, fw, 16).build();
        addDrawableChild(sectionSafetyBtn);

        whisperPauseBtn = makeToggleBtn("Pause on Whisper", ModConfig.whisperPauseEnabled, lx, y, fw, btn -> {
            ModConfig.whisperPauseEnabled = !ModConfig.whisperPauseEnabled;
            btn.setMessage(toggleText("Pause on Whisper", ModConfig.whisperPauseEnabled));
        });
        addDrawableChild(whisperPauseBtn);

        deathPauseBtn = makeToggleBtn("Pause on Death", ModConfig.deathPauseEnabled, lx, y, fw, btn -> {
            ModConfig.deathPauseEnabled = !ModConfig.deathPauseEnabled;
            btn.setMessage(toggleText("Pause on Death", ModConfig.deathPauseEnabled));
        });
        addDrawableChild(deathPauseBtn);
    }

    /**
     * Repositions all Anti-Detect widgets based on which sections are open/closed.
     */
    private void layoutAntiDetect() {
        int lx = panelX + 15;
        int fw = panelW - 30;
        int halfW = (fw - 6) / 2;
        int y = panelY + 62;

        // --- Humanization section header ---
        sectionHumanBtn.setPosition(lx, y);
        y += 20;

        boolean hOpen = sectionHumanOpen;
        gaussianBtn.visible = hOpen && activeTab == 1;
        fatigueBtn.visible = hOpen && activeTab == 1;
        microBreakBtn.visible = hOpen && activeTab == 1;
        microBreakChanceField.visible = hOpen && activeTab == 1;
        sessionBreakBtn.visible = hOpen && activeTab == 1;
        sessionIntervalField.visible = hOpen && activeTab == 1;
        missChanceField.visible = hOpen && activeTab == 1;
        cameraJitterBtn.visible = hOpen && activeTab == 1;

        if (hOpen) {
            gaussianBtn.setPosition(lx, y); y += 22;
            fatigueBtn.setPosition(lx, y); y += 22;
            microBreakBtn.setPosition(lx, y); y += 22;
            microBreakChanceField.setPosition(lx + 160, y); y += 22;
            sessionBreakBtn.setPosition(lx, y); y += 22;
            sessionIntervalField.setPosition(lx + 160, y); y += 22;
            missChanceField.setPosition(lx + 160, y); y += 22;
            cameraJitterBtn.setPosition(lx, y); y += 24;
        }

        // --- Sea Creatures section header ---
        sectionSeaCreatureBtn.setPosition(lx, y);
        y += 20;

        boolean scOpen = sectionSeaCreatureOpen;
        thunderBtn.visible = scOpen && activeTab == 1;
        jawbusBtn.visible = scOpen && activeTab == 1;
        scuttlerBtn.visible = scOpen && activeTab == 1;
        ragnarokBtn.visible = scOpen && activeTab == 1;

        if (scOpen) {
            thunderBtn.setPosition(lx, y);
            jawbusBtn.setPosition(lx + halfW + 6, y);
            y += 22;
            scuttlerBtn.setPosition(lx, y);
            ragnarokBtn.setPosition(lx + halfW + 6, y);
            y += 24;
        }

        // --- Safety section header ---
        sectionSafetyBtn.setPosition(lx, y);
        y += 20;

        boolean safeOpen = sectionSafetyOpen;
        whisperPauseBtn.visible = safeOpen && activeTab == 1;
        deathPauseBtn.visible = safeOpen && activeTab == 1;

        if (safeOpen) {
            whisperPauseBtn.setPosition(lx, y); y += 22;
            deathPauseBtn.setPosition(lx, y); y += 22;
        }
    }

    private Text sectionTitle(String name, boolean open) {
        String arrow = open ? "\u25BC " : "\u25B6 ";
        return Text.literal(arrow + name).formatted(Formatting.AQUA, Formatting.BOLD);
    }

    // ======== KILL TAB ========

    private void initKillTab(int lx, int fw) {
        int y = panelY + 62;

        rodSlotField = makeField(lx + 180, y, 30, String.valueOf(ModConfig.fishingRodSlot));
        addDrawableChild(rodSlotField);
        y += 28;

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

    // ======== TAB SWITCHING ========

    private void switchTab(int tab) {
        activeTab = tab;

        // Main tab
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
        chatFeedbackBtn.visible = main;
        soundLogBtn.visible = main;
        if (main) {
            updateDetectionFieldVisibility();
        } else {
            soundNameField.visible = false;
            visualTriggerField.visible = false;
        }

        // Anti-Detect tab — section headers always visible, contents depend on dropdown state
        boolean ad = tab == 1;
        sectionHumanBtn.visible = ad;
        sectionSeaCreatureBtn.visible = ad;
        sectionSafetyBtn.visible = ad;
        if (ad) {
            layoutAntiDetect();
        } else {
            // Hide all anti-detect content widgets
            gaussianBtn.visible = false;
            fatigueBtn.visible = false;
            microBreakBtn.visible = false;
            microBreakChanceField.visible = false;
            sessionBreakBtn.visible = false;
            sessionIntervalField.visible = false;
            missChanceField.visible = false;
            cameraJitterBtn.visible = false;
            thunderBtn.visible = false;
            jawbusBtn.visible = false;
            scuttlerBtn.visible = false;
            ragnarokBtn.visible = false;
            whisperPauseBtn.visible = false;
            deathPauseBtn.visible = false;
        }

        // Kill tab
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
            case "VISUAL": color = Formatting.LIGHT_PURPLE; label = "VISUAL (!!! armor stand)"; break;
            default:       color = Formatting.GREEN;  label = "BOTH (sound + visual)"; break;
        }
        return Text.literal("Detection: ").append(Text.literal(label).formatted(color));
    }

    // ======== RENDERING ========

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, BG_OVERLAY);
        drawBorderedRect(ctx, panelX, panelY, panelX + panelW, panelY + panelH, PANEL_BG, PANEL_BORDER);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("SkyBlock Fisher").formatted(Formatting.AQUA, Formatting.BOLD),
                width / 2, panelY + 8, CYAN);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Anti-Detect Edition").formatted(Formatting.GRAY),
                width / 2, panelY + 20, TEXT_DIM);

        // Tab underline
        int tabY = panelY + 48;
        int thirdW = (panelW - 42) / 3;
        int tabStartX = panelX + 15 + activeTab * (thirdW + 6);
        ctx.fill(tabStartX, tabY, tabStartX + thirdW, tabY + 2, CYAN);

        // Stats bar
        int sy = panelY + panelH - 50;
        ctx.fill(panelX + 15, sy, panelX + panelW - 15, sy + 22, SURFACE);

        int catches = handler.getCatchCount();
        int misses = handler.getMissCount();
        int secs = handler.getElapsedSeconds();

        ctx.drawTextWithShadow(textRenderer,
                String.format("Catches: %s%d %s(%d missed)", Formatting.GREEN, catches, Formatting.GRAY, misses),
                panelX + 22, sy + 7, TEXT_MAIN);
        ctx.drawTextWithShadow(textRenderer,
                String.format("%s%02d:%02d", Formatting.AQUA, secs / 60, secs % 60),
                panelX + panelW - 55, sy + 7, TEXT_MAIN);

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
            renderAntiDetectLabels(ctx, lx);
        } else {
            renderKillLabels(ctx, lx);
        }

        // Sound log panel
        if (showSoundLog && activeTab == 0) {
            renderSoundLog(ctx);
        }

        // Update button + changelog
        if (UpdateChecker.isUpdateAvailable()) {
            updateBtn.visible = true;
            updateBtn.setMessage(getUpdateBtnText());
            if (showChangelog) {
                renderChangelog(ctx);
            }
        }
    }

    private void renderMainLabels(DrawContext ctx, int lx) {
        int y = panelY + 62 + 26 + 26;
        String mode = ModConfig.detectionMode.toUpperCase();

        if (mode.equals("AUDIO") || mode.equals("BOTH")) {
            ctx.drawTextWithShadow(textRenderer, "Target Sound:", lx, y + 4, TEXT_DIM);
        }
        y += 22;
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

    private void renderAntiDetectLabels(DrawContext ctx, int lx) {
        // Labels are drawn next to fields based on current layout positions
        if (sectionHumanOpen) {
            // Draw labels next to the text fields
            ctx.drawTextWithShadow(textRenderer, "Break chance (%):", lx,
                    microBreakChanceField.getY() + 4, TEXT_DIM);
            ctx.drawTextWithShadow(textRenderer, "Break interval (min):", lx,
                    sessionIntervalField.getY() + 4, TEXT_DIM);
            ctx.drawTextWithShadow(textRenderer, "Miss chance (%):", lx,
                    missChanceField.getY() + 4, TEXT_DIM);
        }
    }

    private void renderKillLabels(DrawContext ctx, int lx) {
        int y = panelY + 62;

        ctx.drawTextWithShadow(textRenderer, "Fishing Rod Slot (1-9):", lx, y + 4, TEXT_DIM);
        y += 28;

        // Flaming Flay section
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("--- Flaming Flay ---").formatted(Formatting.GOLD).getString(),
                lx, y - 8, YELLOW);
        y += 22;
        ctx.drawTextWithShadow(textRenderer, "Flay Slot (1-9):", lx + 10, y + 4, TEXT_DIM);
        y += 22;
        ctx.drawTextWithShadow(textRenderer, "Kill Wait Min (ticks):", lx + 10, y + 4, TEXT_DIM);
        y += 22;
        ctx.drawTextWithShadow(textRenderer, "Kill Wait Max (ticks):", lx + 10, y + 4, TEXT_DIM);
        y += 32;

        // Strider section
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("--- Strider Kill ---").formatted(Formatting.RED).getString(),
                lx, y - 8, RED);
        y += 22;
        ctx.drawTextWithShadow(textRenderer, "Weapon Slot (1-9):", lx + 10, y + 4, TEXT_DIM);
        y += 22;
        ctx.drawTextWithShadow(textRenderer, "Kill Wait Max (ticks):", lx + 10, y + 4, TEXT_DIM);
        y += 22;
        ctx.drawTextWithShadow(textRenderer, "CPS (1-20):", lx + 10, y + 4, TEXT_DIM);
    }

    private void renderSoundLog(DrawContext ctx) {
        int logX = panelX;
        int logY = panelY + panelH + 4;
        int logH = 140;

        drawBorderedRect(ctx, logX, logY, logX + panelW, logY + logH, PANEL_BG, PANEL_BORDER);
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
                col = entry.contains(ModConfig.visualTriggerText) ? 0xFFff69b4 : 0xFFaa88cc;
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

    private void renderChangelog(DrawContext ctx) {
        int logX = panelX - panelW - 6;
        int logY = panelY;
        int logW = panelW;
        int logH = panelH;

        drawBorderedRect(ctx, logX, logY, logX + logW, logY + logH, PANEL_BG, PANEL_BORDER);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("Changelog v" + UpdateChecker.getLatestVersion()).formatted(Formatting.AQUA, Formatting.BOLD).getString(),
                logX + 8, logY + 8, CYAN);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("Current: v" + UpdateChecker.getCurrentVersion()).formatted(Formatting.GRAY).getString(),
                logX + 8, logY + 20, TEXT_DIM);

        String changelog = UpdateChecker.getChangelog();
        if (changelog.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, "No changelog available.", logX + 8, logY + 38, TEXT_DIM);
        } else {
            int ly = logY + 38;
            for (String line : changelog.split("\n")) {
                if (ly > logY + logH - 12) break;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) { ly += 6; continue; }
                int color = TEXT_MAIN;
                if (trimmed.startsWith("##") || trimmed.startsWith("**")) {
                    color = YELLOW;
                    trimmed = trimmed.replace("#", "").replace("*", "").trim();
                } else if (trimmed.startsWith("-") || trimmed.startsWith("*")) {
                    color = TEXT_MAIN;
                    trimmed = "  " + trimmed;
                }
                while (!trimmed.isEmpty() && ly < logY + logH - 12) {
                    int maxChars = (logW - 16) / 6;
                    if (trimmed.length() <= maxChars) {
                        ctx.drawTextWithShadow(textRenderer, trimmed, logX + 8, ly, color);
                        ly += 11;
                        break;
                    } else {
                        String part = trimmed.substring(0, maxChars);
                        int lastSpace = part.lastIndexOf(' ');
                        if (lastSpace > 0) part = trimmed.substring(0, lastSpace);
                        ctx.drawTextWithShadow(textRenderer, part, logX + 8, ly, color);
                        trimmed = trimmed.substring(part.length()).trim();
                        ly += 11;
                    }
                }
            }
        }
    }

    // ======== HELPERS ========

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

    private Text getUpdateBtnText() {
        if (UpdateChecker.isDownloaded()) {
            return showChangelog
                    ? Text.literal("Close Log").formatted(Formatting.GRAY)
                    : Text.literal("Closing = Updated").formatted(Formatting.GREEN, Formatting.BOLD);
        } else if (UpdateChecker.isDownloading()) {
            return Text.literal("Downloading...").formatted(Formatting.YELLOW);
        } else if (showChangelog) {
            return Text.literal("Install v" + UpdateChecker.getLatestVersion()).formatted(Formatting.GREEN, Formatting.BOLD);
        } else {
            return Text.literal("v" + UpdateChecker.getLatestVersion() + " available").formatted(Formatting.YELLOW);
        }
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
