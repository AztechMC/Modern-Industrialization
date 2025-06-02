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

    public final ModConfigSpec.BooleanValue missingRecipeViewerMessage;
    public final ModConfigSpec.BooleanValue newVersionMessage;

    public final ModConfigSpec.IntValue armorHudYPosition;
    public final ModConfigSpec.BooleanValue barrelContentRendering;
    public final ModConfigSpec.BooleanValue hatchPlacementOverlay;
    public final ModConfigSpec.BooleanValue interMachineConnectedTextures;

    public final ModConfigSpec.BooleanValue fuelTooltips;
    public final ModConfigSpec.BooleanValue itemTagTooltips;
    public final ModConfigSpec.BooleanValue defaultOreGenTooltips;

    private MIClientConfig(MIConfigBuilder builder) {
        builder.pushSection("messages", "Messages");
        this.missingRecipeViewerMessage = builder.start("missingRecipeViewerMessage",
                "Missing recipe viewer message",
                "Enable login message when EMI, JEI and REI are all missing.")
                .define("missingRecipeViewerMessage", true);
        this.newVersionMessage = builder.start("newVersionMessage",
                "New version message",
                "Display when a new version is available")
                .define("newVersionMessage", true);
        builder.popSection();

        builder.pushSection("rendering", "Rendering");
        this.armorHudYPosition = builder.start("armorHudYPosition",
                "Armor HUD vertical position",
                "Space between the top of the screen and the jetpack/GraviChestPlate overlay text.")
                .defineInRange("armorHudYPosition", 4, 0, 10000);
        this.barrelContentRendering = builder.start("barrelContentRendering",
                "Barrel content rendering",
                "Enable rendering of barrel content: item icon, item amount, and item name.")
                .define("barrelContentRendering", true);
        this.hatchPlacementOverlay = builder.start("hatchPlacementOverlay",
                "Hatch placement overlay",
                "Show valid positions in multiblocks when holding a hatch.")
                .define("hatchPlacementOverlay", true);
        this.interMachineConnectedTextures = builder.start("interMachineConnectedTextures",
                "Inter-machine connected textures",
                "Enable connected textures between machines that have the same casing. (Requires a suitable resource pack)")
                .define("interMachineConnectedTextures", false);
        builder.popSection();

        builder.pushSection("tooltips", "Tooltips");
        this.fuelTooltips = builder.start("fuelTooltips",
                "Fuel tooltips",
                "Add the total EU value of fuel items to their tooltips.")
                .define("fuelTooltips", true);
        this.itemTagTooltips = builder.start("itemTagTooltips",
                "Item tag tooltips",
                "Add item tags to item tooltips.")
                .define("itemTagTooltips", !FMLEnvironment.production);
        this.defaultOreGenTooltips = builder.start("defaultOreGenTooltips",
                "Ore generation tooltips",
                "Enable the default ore generation tooltips.",
                "These tooltips show how each MI ore generates, based on the default ore generation settings in MI.",
                "Set this to false if you change the ore features in a datapack.")
                .define("defaultOreGenTooltips", true);
        builder.popSection();
    }
}
