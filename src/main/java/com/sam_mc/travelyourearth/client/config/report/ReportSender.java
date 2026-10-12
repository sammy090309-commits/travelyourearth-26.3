package com.sam_mc.travelyourearth.client.config.report;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sam_mc.travelyourearth.TravelYourEarth;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Everything the in-game report needs that isn't drawing:
 * - the info the game already knows (mod / NeoForge / Minecraft versions, other mods, singleplayer or server)
 * - uploading latest.log to mclo.gs (only if the player ticks the box)
 * - building the GitHub link with every field already filled in
 *
 * Why a link and not sending it directly: creating the issue from the mod would need a secret
 * GitHub key inside the .jar, and anyone could take it out. With the link, the browser opens with
 * everything filled and the player only presses "Create".
 * GitHub fills issue-form TEXT fields (input / textarea) from the link: ?<field id>=<text>
 */
public final class ReportSender {

    public static final String ISSUES_URL = "https://github.com/sammy090309-commits/travelyourearth-26.3/issues/new";
    public static final String BUG_TEMPLATE = "bug_report.yml";
    public static final String FEEDBACK_TEMPLATE = "feedback.yml";

    private static final String MCLOGS_API = "https://api.mclo.gs/1/log";
    /** Browsers and GitHub don't like links much longer than this. */
    private static final int MAX_URL_LENGTH = 7800;
    /** Each text is cut to this length (the description can be cut more if the link gets too long). */
    private static final int MAX_FIELD_LENGTH = 1500;
    /** mclo.gs accepts big logs, but we only send the end (where the error usually is). */
    private static final int MAX_LOG_CHARS = 2_000_000;

    private ReportSender() {}

    // ---------------------------------------------------------------------
    // What the game already knows
    // ---------------------------------------------------------------------

    private static String versionOf(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("?");
    }

    public static String modVersion() {
        return versionOf(TravelYourEarth.MODID);
    }

    public static String neoForgeVersion() {
        return versionOf("neoforge");
    }

    public static String minecraftVersion() {
        return versionOf("minecraft");
    }

    /** Every other mod: "Name (id) version", one per line. */
    public static String otherMods() {
        String mods = ModList.get().getMods().stream()
                .filter(m -> !m.getModId().equals(TravelYourEarth.MODID)
                        && !m.getModId().equals("minecraft")
                        && !m.getModId().equals("neoforge"))
                .map(ReportSender::describe)
                .sorted()
                .collect(Collectors.joining("\n"));
        return mods.isEmpty() ? "none" : mods;
    }

    private static String describe(IModInfo mod) {
        return mod.getDisplayName() + " (" + mod.getModId() + ") " + mod.getVersion();
    }

    /** 0 = singleplayer, 1 = multiplayer server (what the player is doing right now; main menu -> singleplayer). */
    public static int detectSide() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.hasSingleplayerServer() && minecraft.getCurrentServer() != null) return 1;
        return 0;
    }

    // ---------------------------------------------------------------------
    // Log upload (mclo.gs)
    // ---------------------------------------------------------------------

    /**
     * Uploads logs/latest.log to mclo.gs and gives back its link (or null if it failed).
     * mclo.gs hides IP addresses by itself. Runs in the background: the game doesn't freeze.
     */
    public static CompletableFuture<String> uploadLog() {
        try {
            Path log = FMLPaths.GAMEDIR.get().resolve("logs").resolve("latest.log");
            String content = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
            if (content.length() > MAX_LOG_CHARS) {
                content = content.substring(content.length() - MAX_LOG_CHARS);
            }
            HttpRequest request = HttpRequest.newBuilder(URI.create(MCLOGS_API))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("content=" + URLEncoder.encode(content, StandardCharsets.UTF_8)))
                    .build();
            return HttpClient.newHttpClient()
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        return json.has("success") && json.get("success").getAsBoolean()
                                ? json.get("url").getAsString() : null;
                    })
                    .exceptionally(error -> {
                        TravelYourEarth.LOGGER.warn("Couldn't upload the log to mclo.gs", error);
                        return null;
                    });
        } catch (Exception error) {
            TravelYourEarth.LOGGER.warn("Couldn't read latest.log", error);
            return CompletableFuture.completedFuture(null);
        }
    }

    // ---------------------------------------------------------------------
    // GitHub link
    // ---------------------------------------------------------------------

    /** "★★★★☆ (4/5)" */
    public static String stars(int rating) {
        return "★".repeat(rating) + "☆".repeat(5 - rating) + " (" + rating + "/5)";
    }

    /**
     * The "new issue" link of a template, with the title and every field already written.
     * @param fields field id (as in the .yml) -> text. Order = which ones get cut first if it's too long (the last ones).
     */
    public static URI issueLink(String template, String title, Map<String, String> fields) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("template", template);
        params.put("title", title);
        fields.forEach((id, text) -> {
            if (text != null && !text.isBlank()) {
                params.put(id, cut(text.strip(), MAX_FIELD_LENGTH));
            }
        });

        // Too long for a link? Cut the longest texts a bit until it fits
        String url = build(params);
        while (url.length() > MAX_URL_LENGTH) {
            String longest = params.entrySet().stream()
                    .filter(e -> !e.getKey().equals("template") && !e.getKey().equals("title"))
                    .max((a, b) -> Integer.compare(a.getValue().length(), b.getValue().length()))
                    .map(Map.Entry::getKey).orElse(null);
            if (longest == null) break;
            String text = params.get(longest);
            params.put(longest, cut(text, Math.max(50, text.length() * 3 / 4)));
            url = build(params);
        }
        return URI.create(url);
    }

    private static String build(Map<String, String> params) {
        return ISSUES_URL + "?" + params.entrySet().stream()
                .map(e -> e.getKey() + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static String encode(String text) {
        // URLEncoder writes spaces as "+": GitHub wants %20
        return URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String cut(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}