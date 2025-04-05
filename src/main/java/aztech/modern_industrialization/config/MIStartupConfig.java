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

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class MIStartupConfig {
    public static final MIStartupConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    static {
        var builder = new MIConfigBuilder();
        INSTANCE = new MIStartupConfig(builder);
        SPEC = builder.build();
    }

    public boolean loadAe2Compat() {
        return enableAe2Integration.getAsBoolean() && ModList.get().isLoaded("ae2");
    }

    public final ModConfigSpec.BooleanValue enableBidirectionalEnergyCompat;
    public final ModConfigSpec.BooleanValue enableAe2Integration;
    public final ModConfigSpec.BooleanValue enableFtbQuestsIntegration;

    public final ModConfigSpec.BooleanValue datagenOnStartup;
    public final ModConfigSpec.BooleanValue loadRuntimeGeneratedResources;

    // These should ideally be moved to the server config one day.
    public final ModConfigSpec.BooleanValue enableDebugCommands;
    public final ModConfigSpec.IntValue maxDistillationTowerHeight;
    public final ModConfigSpec.BooleanValue removeIndustrialistTrades;

    private MIStartupConfig(MIConfigBuilder builder) {
        builder.pushSection("compat", "Mod Compatibility");
        this.enableBidirectionalEnergyCompat = builder.start("enableBidirectionalEnergyCompat",
                "Enable bidirectional energy compatibility",
                "Enable bi-directional energy compatibility with Forge Energy.",
                "We recommend leaving this to false unless the other mods have been balanced accordingly.")
                .gameRestart()
                .define("enableBidirectionalEnergyCompat", false);
        this.enableAe2Integration = builder.start("enableAe2Integration",
                "Enable AE2 integration",
                "Enable the Applied Energistics 2 integration, if present.")
                .gameRestart()
                .define("enableAe2Integration", true);
        this.enableFtbQuestsIntegration = builder.start("enableFtbQuestsIntegration",
                "Enable FTB Quests integration",
                "Enable the FTB Quests integration, if present.")
                .gameRestart()
                .define("enableFtbQuestsIntegration", true);
        builder.popSection();

        builder.pushSection("datagen", "Runtime Datagen");
        this.datagenOnStartup = builder.start("datagenOnStartup",
                "Datagen on startup",
                "Run MI runtime datagen on startup")
                .gameRestart()
                .define("datagenOnStartup", false);
        this.loadRuntimeGeneratedResources = builder.start("loadRuntimeGeneratedResources",
                "Load generated resources",
                "Additionally load resources in modern_industrialization/generated_resources")
                .gameRestart()
                .define("loadRuntimeGeneratedResources", false);
        builder.popSection();

        this.enableDebugCommands = builder.start("enableDebugCommands",
                "Enable debug commands",
                "Enable UNSUPPORTED and DANGEROUS debug commands")
                .define("enableDebugCommands", !FMLEnvironment.production);
        this.maxDistillationTowerHeight = builder.start("maxDistillationTowerHeight",
                "Max distillation tower height",
                "Maximum height of the Distillation Tower multiblock")
                .gameRestart()
                .defineInRange("maxDistillationTowerHeight", 9, 1, 32);
        this.removeIndustrialistTrades = builder.start("removeIndustrialistTrades",
                "Remove Industrialist trades",
                "Removes trades from the Industrialist villager.")
                .define("removeIndustrialistTrades", false);
    }
}
