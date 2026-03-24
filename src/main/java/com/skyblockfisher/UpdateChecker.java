package com.skyblockfisher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class UpdateChecker {

    private static final String GITHUB_REPO = "Glitchyorsmth/SkyBlockFisher";
    private static final String API_URL = "https://api.github.com/repos/" + GITHUB_REPO + "/releases/latest";
    private static final String CURRENT_VERSION = FabricLoader.getInstance()
            .getModContainer("skyblockfisher")
            .map(c -> c.getMetadata().getVersion().getFriendlyString())
            .orElse("1.0.0");

    // State
    private static boolean checked = false;
    private static boolean updateAvailable = false;
    private static boolean downloading = false;
    private static boolean downloaded = false;
    private static String latestVersion = "";
    private static String downloadUrl = "";
    private static String changelog = "";

    public static boolean isUpdateAvailable() { return updateAvailable; }
    public static boolean isDownloading() { return downloading; }
    public static boolean isDownloaded() { return downloaded; }
    public static String getLatestVersion() { return latestVersion; }
    public static String getChangelog() { return changelog; }
    public static String getCurrentVersion() { return CURRENT_VERSION; }
    public static boolean hasChecked() { return checked; }

    /**
     * Check for updates on a background thread. Call once on mod init.
     */
    public static void checkAsync() {
        if (checked) return;
        new Thread(() -> {
            try {
                check();
            } catch (Exception e) {
                System.out.println("[SkyBlockFisher] Update check failed: " + e.getMessage());
            }
            checked = true;
        }, "SkyBlockFisher-UpdateCheck").start();
    }

    private static void check() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("User-Agent", "SkyBlockFisher/" + CURRENT_VERSION);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        if (conn.getResponseCode() != 200) return;

        String body;
        try (InputStream is = conn.getInputStream()) {
            body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        JsonObject release = JsonParser.parseString(body).getAsJsonObject();
        String tagName = release.get("tag_name").getAsString();
        // Strip leading 'v' if present
        latestVersion = tagName.startsWith("v") ? tagName.substring(1) : tagName;
        changelog = release.has("body") && !release.get("body").isJsonNull()
                ? release.get("body").getAsString() : "";

        // Compare versions
        if (isNewer(latestVersion, CURRENT_VERSION)) {
            updateAvailable = true;

            // Find the JAR asset
            JsonArray assets = release.getAsJsonArray("assets");
            if (assets != null) {
                for (JsonElement asset : assets) {
                    JsonObject a = asset.getAsJsonObject();
                    String name = a.get("name").getAsString();
                    if (name.endsWith(".jar") && !name.contains("sources") && !name.contains("dev")) {
                        downloadUrl = a.get("browser_download_url").getAsString();
                        break;
                    }
                }
            }

            // Notify player in chat when they join a world
            MinecraftClient.getInstance().execute(() -> notifyPlayer());
        }
    }

    /**
     * Compare two semver-ish strings. Returns true if 'newer' > 'current'.
     */
    private static boolean isNewer(String newer, String current) {
        try {
            int[] n = parseVersion(newer);
            int[] c = parseVersion(current);
            for (int i = 0; i < Math.max(n.length, c.length); i++) {
                int nv = i < n.length ? n[i] : 0;
                int cv = i < c.length ? c[i] : 0;
                if (nv > cv) return true;
                if (nv < cv) return false;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static int[] parseVersion(String v) {
        String[] parts = v.split("\\.");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].replaceAll("[^0-9]", ""));
        }
        return result;
    }

    /**
     * Send a chat message to the player about the available update.
     */
    private static void notifyPlayer() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return; // will retry via tick

        MutableText msg = Text.literal("[Fisher] ").formatted(Formatting.AQUA)
                .append(Text.literal("Update available! ").formatted(Formatting.GREEN))
                .append(Text.literal("v" + CURRENT_VERSION).formatted(Formatting.GRAY))
                .append(Text.literal(" → ").formatted(Formatting.DARK_GRAY))
                .append(Text.literal("v" + latestVersion).formatted(Formatting.YELLOW))
                .append(Text.literal(" | Open GUI (RShift) to update.").formatted(Formatting.GRAY));

        mc.player.sendMessage(msg, false);
    }

    // Will be called from tick to retry notification if player wasn't loaded yet
    private static boolean notified = false;
    public static void tickNotify() {
        if (updateAvailable && !notified) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                notifyPlayer();
                notified = true;
            }
        }
    }

    /**
     * On mod init, check if a previous update was staged (.update file exists).
     * If so, the shutdown hook from last session should have already swapped it.
     * Clean up any leftover .old files.
     */
    public static void applyPendingUpdate() {
        try {
            Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
            // Clean up .old files from previous update
            Files.list(modsDir)
                    .filter(p -> p.getFileName().toString().startsWith("SkyBlockFisher") && p.toString().endsWith(".old"))
                    .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} });

            // Check if .update file exists (shutdown hook didn't run — do swap now)
            Files.list(modsDir)
                    .filter(p -> p.getFileName().toString().startsWith("SkyBlockFisher") && p.toString().endsWith(".update"))
                    .forEach(p -> {
                        try {
                            String name = p.getFileName().toString().replace(".update", "");
                            Path target = p.resolveSibling(name);
                            Files.move(p, target, StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception ignored) {}
                    });
        } catch (Exception ignored) {}
    }

    /**
     * Download the new JAR as .update, register a shutdown hook to swap files
     * when the game closes. No file locking issues.
     */
    public static void downloadUpdate() {
        if (downloadUrl.isEmpty() || downloading || downloaded) return;
        downloading = true;

        new Thread(() -> {
            try {
                Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
                String fileName = "SkyBlockFisher-" + latestVersion + ".jar";
                Path staged = modsDir.resolve(fileName + ".update");

                // Download new JAR as .update file
                HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
                conn.setRequestProperty("User-Agent", "SkyBlockFisher/" + CURRENT_VERSION);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);

                try (InputStream is = conn.getInputStream()) {
                    Files.copy(is, staged, StandardCopyOption.REPLACE_EXISTING);
                }

                // Register shutdown hook to swap files when game closes
                Path currentJar = FabricLoader.getInstance()
                        .getModContainer("skyblockfisher")
                        .flatMap(c -> c.getOrigin().getPaths().stream().findFirst())
                        .orElse(null);

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        // Rename old JAR to .old
                        if (currentJar != null && Files.exists(currentJar)) {
                            Files.move(currentJar, currentJar.resolveSibling(currentJar.getFileName() + ".old"),
                                    StandardCopyOption.REPLACE_EXISTING);
                        }
                        // Rename .update to .jar
                        Path target = staged.resolveSibling(fileName);
                        Files.move(staged, target, StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception e) {
                        System.out.println("[SkyBlockFisher] Shutdown update swap failed: " + e.getMessage());
                    }
                }, "SkyBlockFisher-UpdateSwap"));

                downloaded = true;
                downloading = false;

                MinecraftClient.getInstance().execute(() -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc.player != null) {
                        mc.player.sendMessage(
                                Text.literal("[Fisher] ").formatted(Formatting.AQUA)
                                        .append(Text.literal("v" + latestVersion + " downloaded! It will be applied when you close Minecraft.").formatted(Formatting.GREEN)),
                                false
                        );
                    }
                });

            } catch (Exception e) {
                downloading = false;
                System.out.println("[SkyBlockFisher] Update download failed: " + e.getMessage());
                MinecraftClient.getInstance().execute(() -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc.player != null) {
                        mc.player.sendMessage(
                                Text.literal("[Fisher] ").formatted(Formatting.AQUA)
                                        .append(Text.literal("Update failed: " + e.getMessage()).formatted(Formatting.RED)),
                                false
                        );
                    }
                });
            }
        }, "SkyBlockFisher-UpdateDownload").start();
    }
}
