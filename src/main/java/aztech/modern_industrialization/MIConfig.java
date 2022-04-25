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

    @ConfigEntry.Gui.RequiresRestart
    public boolean generateOres = true;

    @OreConfigEntry
    public List<String> blacklistedOres = Collections.emptyList();

    public boolean spawnWithGuideBook = true;
    public boolean respawnWithGuideBook = true;
    public boolean disableFuelTooltips = false;
    public boolean disableItemTagTooltips = false;
    public boolean newVersionMessage = true;
    public boolean enableHatchPlacementOverlay = true;
    public boolean colorWaterLava = true;

    public boolean enableDebugCommands = false;

    @ConfigEntry.Gui.RequiresRestart
    public boolean enableAe2Integration = true;

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
