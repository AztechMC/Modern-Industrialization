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
package aztech.modern_industrialization;

import aztech.modern_industrialization.compat.modmenu.OreConfigEntry;
import aztech.modern_industrialization.datagen.translation.EnglishTranslation;
import java.util.Collections;
import java.util.List;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;

@Config(name = MIConfig.NAME)
public class MIConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    public static final transient String NAME = "modern_industrialization";

    @OreConfigEntry
    public List<String> blacklistedOres = Collections.emptyList();

    @EnglishTranslation(value = "Grant Guidebook at Spawn")
    public boolean spawnWithGuideBook = true;
    @EnglishTranslation(value = "Grant Guidebook at Respawn")
    public boolean respawnWithGuideBook = true;
    @EnglishTranslation(value = "Disable display of Fuel EU in tooltips")
    public boolean disableFuelTooltips = false;
    @EnglishTranslation(value = "Disable display of Item Tag in tooltips")
    public boolean disableItemTagTooltips = false;
    @EnglishTranslation(value = "Display when a new version is available")
    public boolean newVersionMessage = true;
    @EnglishTranslation(value = "Show valid positions in multiblocks when holding a hatch")
    public boolean enableHatchPlacementOverlay = true;
    @EnglishTranslation(value = "Color Water and Lava")
    public boolean colorWaterLava = true;
    @EnglishTranslation(value = "Enable UNSUPPORTED and DANGEROUS debug commands")
    public boolean enableDebugCommands = false;
    @ConfigEntry.Gui.RequiresRestart
    @EnglishTranslation(value = "Enable the AE2 integration, if present (Restart needed)")
    public boolean enableAe2Integration = true;
    @ConfigEntry.Gui.RequiresRestart
    @EnglishTranslation(value = "Ore Generation Enabled (Restart needed)")
    public boolean generateOres = true;

    @ConfigEntry.Gui.Excluded
    private transient static boolean registered = false;

    public static synchronized MIConfig getConfig() {
        if (!registered) {
            AutoConfig.register(MIConfig.class, Toml4jConfigSerializer::new);
            registered = true;
        }

        return AutoConfig.getConfigHolder(MIConfig.class).getConfig();
    }
}
