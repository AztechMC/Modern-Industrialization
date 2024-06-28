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

import aztech.modern_industrialization.datagen.translation.EnglishTranslation;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;

@Config(name = MIConfig.NAME)
public class MIConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    public static final transient String NAME = "modern_industrialization";

    @EnglishTranslation(value = "Enable login message when EMI, JEI and REI are missing")
    public boolean enableNoEmiMessage = true;
    @EnglishTranslation(value = "Grant Guidebook at Spawn")
    public boolean spawnWithGuideBook = true;
    @EnglishTranslation(value = "Grant Guidebook at Respawn")
    public boolean respawnWithGuideBook = true;
    @EnglishTranslation(value = "Disable display of Fuel EU in tooltips")
    public boolean disableFuelTooltips = false;
    @EnglishTranslation(value = "Disable display of Item Tag in tooltips")
    public boolean disableItemTagTooltips = FMLEnvironment.production;
    @EnglishTranslation(value = "Display when a new version is available")
    public boolean newVersionMessage = true;
    @EnglishTranslation(value = "Show valid positions in multiblocks when holding a hatch")
    public boolean enableHatchPlacementOverlay = true;
    @EnglishTranslation(value = "Enable rendering of barrel content (item icon, item amount, and item name)")
    public boolean enableBarrelContentRendering = true;
    @ConfigEntry.Gui.RequiresRestart
    @EnglishTranslation(value = "Color Water and Lava (Restart needed)")
    public boolean colorWaterLava = true;
    @EnglishTranslation(value = "Enable UNSUPPORTED and DANGEROUS debug commands")
    public boolean enableDebugCommands = !FMLEnvironment.production;
    @ConfigEntry.Gui.RequiresRestart
    @EnglishTranslation(value = "Enable bi-directional energy compatibility with Tech Reborn Energy. We recommend leaving this to false unless the other mods have been balanced accordingly. (Restart needed)")
    public boolean enableBidirectionalEnergyCompat = false;
    @ConfigEntry.Gui.RequiresRestart
    @EnglishTranslation(value = "Enable the AE2 integration, if present (Restart needed)")
    public boolean enableAe2Integration = true;
    @ConfigEntry.Gui.RequiresRestart
    @EnglishTranslation(value = "Enable the FTB Quests integration, if present (Restart needed)")
    public boolean enableFtbQuestsIntegration = true;
    @EnglishTranslation(value = "Enable the default ore generation tooltips. Set this to false if you change the ore features in a datapack.")
    public boolean enableDefaultOreGenTooltips = true;
    @ConfigEntry.Gui.RequiresRestart
    @EnglishTranslation(value = "Run MI runtime datagen on startup (Restart needed)")
    public boolean datagenOnStartup = false;
    @ConfigEntry.Gui.RequiresRestart
    @EnglishTranslation(value = "Additionally load resources in modern_industrialization/generated_resources")
    public boolean loadRuntimeGeneratedResources = true;
    @ConfigEntry.Gui.RequiresRestart
    @EnglishTranslation(value = "Removes trades from the Industrialist villager (Restart needed)")
    public boolean removeIndustrialistTrades = false;
    @ConfigEntry.Gui.RequiresRestart
    @EnglishTranslation(value = "Maximum height of the Distillation Tower multiblock (Restart needed)")
    public int maxDistillationTowerHeight = 9;
    @EnglishTranslation(value = "Enable inter-machine connected textures. (Requires a suitable resource pack)")
    public boolean enableInterMachineConnectedTextures = false;

    @ConfigEntry.Gui.Excluded
    private transient volatile static MIConfig instance = null;

    public static synchronized MIConfig getConfig() {
        MIConfig config = instance;
        if (config == null) {
            synchronized (MIConfig.class) {
                config = instance;
                if (config == null) {
                    instance = config = AutoConfig.register(MIConfig.class, Toml4jConfigSerializer::new).getConfig();
                }
            }
        }
        return config;
    }

    public static boolean loadAe2Compat() {
        return getConfig().enableAe2Integration && ModList.get().isLoaded("ae2");
    }
}
