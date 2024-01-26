/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.misc.version;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIText;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

public class VersionEvents {

    private static final String url = "https://api.cfwidget.com/minecraft/mc-mods/modern-industrialization";
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    private static final String alphaPostfix = "alpha";

    private record Version(String name, String url, Date date) implements Comparable<Version> {
        @Override
        public int compareTo(@NotNull VersionEvents.Version o) {
            return o.date.compareTo(date);
        }
    }

    private static Version fetchVersion(boolean isIncludeAlphaVersion) throws Exception {
        String mcVersion = ModList.get().getModContainerById("minecraft").get().getModInfo().getVersion().toString();

        URLConnection connection;
        connection = new URL(url).openConnection();
        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            PriorityQueue<Version> queue = new PriorityQueue<>();

            String response = scanner.useDelimiter("\\A").next();
            JsonObject jo = (JsonObject) JsonParser.parseString(response);

            for (JsonElement file : jo.getAsJsonArray("files")) {
                JsonObject fileAsJsonObject = file.getAsJsonObject();

                boolean matchesCurrentMcVersion = false;
                for (JsonElement version : fileAsJsonObject.get("versions").getAsJsonArray()) {
                    if (version.getAsString().equals(mcVersion)) {
                        matchesCurrentMcVersion = true;
                    }
                }
                if (!matchesCurrentMcVersion) {
                    continue;
                }

                String name = fileAsJsonObject.get("display").getAsString();
                String url = fileAsJsonObject.get("url").getAsString();
                String type = fileAsJsonObject.get("type").getAsString();
                String date = fileAsJsonObject.get("uploaded_at").getAsString();

                if (isIncludeAlphaVersion || !type.equals(alphaPostfix)) {
                    queue.add(new Version(name, url, format.parse(date)));
                }
            }

            if (!queue.isEmpty()) {
                return queue.poll();
            }

        }
        return null;
    }

    public static void startVersionCheck(ModContainer miContainer, LocalPlayer player) {
        new Thread(() -> {
            try {
                if (MIConfig.getConfig().newVersionMessage) {
                    String currentVersion = miContainer.getModInfo().getVersion().toString();
                    Version lastVersion = fetchVersion(currentVersion.contains(alphaPostfix));

                    if (lastVersion != null) {
                        String lastVersionString = lastVersion.name.replaceFirst("Modern Industrialization v", "").strip();

                        if (!lastVersionString.equals(currentVersion)) {
                            String url = lastVersion.url;

                            Style styleClick = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                                    .applyFormat(ChatFormatting.UNDERLINE).applyFormat(ChatFormatting.GREEN).withHoverEvent(new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT, MIText.ClickUrl.text()));

                            Minecraft.getInstance().execute(() -> {
                                if (Minecraft.getInstance().player == player) {
                                    player.displayClientMessage(
                                            MIText.NewVersion.text(lastVersionString,
                                                    MIText.CurseForge.text().setStyle(styleClick)),
                                            false);
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                MI.LOGGER.error("Failed to get release information from Curseforge.", e);
            }
        }, "Modern Industrialization Update Checker").start();
    }

    public static void init(ModContainer miContainer) {
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, event -> {
            startVersionCheck(miContainer, event.getPlayer());
        });
    }
}
