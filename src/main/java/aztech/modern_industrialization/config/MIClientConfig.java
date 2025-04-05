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
package aztech.modern_industrialization.config;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class MIClientConfig {
    public static final MIClientConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    static {
        var builder = new MIConfigBuilder();
        INSTANCE = new MIClientConfig(builder);
        SPEC = builder.build();
    }

    public final ModConfigSpec.BooleanValue enableNoEmiMessage;
    public final ModConfigSpec.BooleanValue newVersionMessage;

    public final ModConfigSpec.IntValue armorHudYPosition;
    public final ModConfigSpec.BooleanValue enableBarrelContentRendering;
    public final ModConfigSpec.BooleanValue enableHatchPlacementOverlay;
    public final ModConfigSpec.BooleanValue enableInterMachineConnectedTextures;

    public final ModConfigSpec.BooleanValue disableFuelTooltips;
    public final ModConfigSpec.BooleanValue disableItemTagTooltips;
    public final ModConfigSpec.BooleanValue enableDefaultOreGenTooltips;

    private MIClientConfig(MIConfigBuilder builder) {
        builder.pushSection("messages", "Messages");
        this.enableNoEmiMessage = builder.start("enableNoEmiMessage",
                "Enable \"No EMI\" message",
                "Enable login message when EMI, JEI and REI are missing")
                .define("enableNoEmiMessage", true);
        this.newVersionMessage = builder.start("newVersionMessage",
                "New version message",
                "Display when a new version is available")
                .define("newVersionMessage", true);
        builder.popSection();

        builder.pushSection("rendering", "Rendering");
        this.armorHudYPosition = builder.start("armorHudYPosition",
                "Armor HUD vertical position",
                "Space between the top of the screen and the Jetpack/GraviChestPlate overlay text.")
                .defineInRange("armorHudYPosition", 4, 0, 10000);
        this.enableBarrelContentRendering = builder.start("enableBarrelContentRendering",
                "Enable barrel content rendering",
                "Enable rendering of barrel content (item icon, item amount, and item name)")
                .define("enableBarrelContentRendering", true);
        this.enableHatchPlacementOverlay = builder.start("enableHatchPlacementOverlay",
                "Enable hatch placement overlay",
                "Show valid positions in multiblocks when holding a hatch")
                .define("enableHatchPlacementOverlay", true);
        this.enableInterMachineConnectedTextures = builder.start("enableInterMachineConnectedTextures",
                "Enable inter-machine connected textures",
                "Enable inter-machine connected textures. (Requires a suitable resource pack)")
                .define("enableInterMachineConnectedTextures", false);
        builder.popSection();

        builder.pushSection("tooltips", "Tooltips");
        this.disableFuelTooltips = builder.start("disableFuelTooltips",
                "Disable fuel tooltips",
                "Disable display of Fuel EU in tooltips")
                .define("disableFuelTooltips", false);
        this.disableItemTagTooltips = builder.start("disableItemTagTooltips",
                "Disable item tag tooltips",
                "Disable display of Item Tag in tooltips")
                .define("disableItemTagTooltips", FMLEnvironment.production);
        this.enableDefaultOreGenTooltips = builder.start("enableDefaultOreGenTooltips",
                "Enable default ore generation tooltips",
                "Enable the default ore generation tooltips. Set this to false if you change the ore features in a datapack.")
                .define("enableDefaultOreGenTooltips", true);
        builder.popSection();
    }
}
