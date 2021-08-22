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

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.ModernIndustrialization;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseFileFilter;
import com.therandomlabs.curseapi.file.CurseFiles;
import com.therandomlabs.curseapi.file.CurseReleaseType;
import java.util.Optional;
import me.shedaniel.cloth.api.common.events.v1.PlayerJoinCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import okhttp3.HttpUrl;

public class VersionEvents {

    private static final int MI_PROJECT_ID = 405388;

    public static void init() {
        PlayerJoinCallback.EVENT.register((connection, player) -> {
            Optional<ModContainer> currentMod = FabricLoader.getInstance().getModContainer(ModernIndustrialization.MOD_ID);
            if (MIConfig.getConfig().newVersionMessage) {
                if (currentMod.isPresent()) {
                    ModContainer mod = currentMod.get();
                    Version currentVersion = mod.getMetadata().getVersion();
                    String version = currentVersion.getFriendlyString();

                    try {
                        final Optional<CurseFiles<CurseFile>> optionalFiles = CurseAPI.files(MI_PROJECT_ID);
                        if (optionalFiles.isPresent()) {
                            final CurseFiles<CurseFile> files = optionalFiles.get();
                            new CurseFileFilter().minimumStability(CurseReleaseType.BETA)
                                    .gameVersionStrings(MinecraftClient.getInstance().getGameVersion()).apply(files);

                            if (!files.isEmpty()) {
                                CurseFile lastVersion = files.first();

                                String lastVersionString = lastVersion.displayName().replaceFirst("Modern Industrialization v", "").strip();

                                if (!lastVersionString.equals(version)) {
                                    HttpUrl url = lastVersion.url();

                                    Style styleClick = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url.toString()))
                                            .withFormatting(Formatting.UNDERLINE).withFormatting(Formatting.GREEN).withHoverEvent(new HoverEvent(
                                                    HoverEvent.Action.SHOW_TEXT, new TranslatableText("text.modern_industrialization.click_url")));

                                    player.sendMessage(new TranslatableText("text.modern_industrialization.new_version", lastVersionString,
                                            new TranslatableText("text.modern_industrialization.curse_forge").setStyle(styleClick)), false);
                                }
                            }
                        }

                    } catch(Exception e) {
                        ModernIndustrialization.LOGGER.error(e.getMessage(), e);
                    }
                } else {
                    throw new IllegalStateException("Modern Industrialization is not loaded but loaded at the same time");
                }
            }
        });
    }
}
