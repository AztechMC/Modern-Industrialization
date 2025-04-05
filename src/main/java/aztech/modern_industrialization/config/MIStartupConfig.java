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
        return ae2Integration.getAsBoolean() && ModList.get().isLoaded("ae2");
    }

    public final ModConfigSpec.BooleanValue bidirectionalEnergyCompat;
    public final ModConfigSpec.BooleanValue ae2Integration;
    public final ModConfigSpec.BooleanValue ftbQuestsIntegration;

    public final ModConfigSpec.BooleanValue datagenOnStartup;
    public final ModConfigSpec.BooleanValue loadRuntimeGeneratedResources;

    // These should ideally be moved to the server config one day.
    public final ModConfigSpec.BooleanValue debugCommands;
    public final ModConfigSpec.IntValue maxDistillationTowerHeight;
    public final ModConfigSpec.BooleanValue defaultIndustrialistTrades;

    private MIStartupConfig(MIConfigBuilder builder) {
        builder.pushSection("compat", "Mod Compatibility");
        this.bidirectionalEnergyCompat = builder.start("bidirectionalEnergyCompat",
                "Bidirectional energy compatibility",
                "Enable bidirectional energy compatibility with NeoForge's energy system.",
                "We recommend leaving this to false unless the other mods have been balanced accordingly.")
                .gameRestart()
                .define("bidirectionalEnergyCompat", false);
        this.ae2Integration = builder.start("ae2Integration",
                "AE2 integration",
                "Enable the Applied Energistics 2 integration, if present.")
                .gameRestart()
                .define("ae2Integration", true);
        this.ftbQuestsIntegration = builder.start("ftbQuestsIntegration",
                "FTB Quests integration",
                "Enable the FTB Quests integration, if present.")
                .gameRestart()
                .define("ftbQuestsIntegration", true);
        builder.popSection();

        builder.pushSection("datagen", "Runtime Datagen");
        this.datagenOnStartup = builder.start("datagenOnStartup",
                "Datagen on startup",
                "Run MI runtime datagen on startup.")
                .gameRestart()
                .define("datagenOnStartup", false);
        this.loadRuntimeGeneratedResources = builder.start("loadRuntimeGeneratedResources",
                "Load generated resources",
                "Additionally load resources in modern_industrialization/generated_resources.")
                .gameRestart()
                .define("loadRuntimeGeneratedResources", false);
        builder.popSection();

        this.debugCommands = builder.start("debugCommands",
                "Debug commands",
                "Enable UNSUPPORTED and DANGEROUS debug commands.")
                .define("debugCommands", !FMLEnvironment.production);
        this.maxDistillationTowerHeight = builder.start("maxDistillationTowerHeight",
                "Max distillation tower height",
                "Maximum height of the distillation tower multiblock.")
                .gameRestart()
                .defineInRange("maxDistillationTowerHeight", 9, 1, 32);
        this.defaultIndustrialistTrades = builder.start("defaultIndustrialistTrades",
                "Default Industrialist trades",
                "Enable the default trades from the Industrialist villager provided by MI.",
                "Disable this to provide your own set of trades.")
                .define("defaultIndustrialistTrades", true);
    }
}
