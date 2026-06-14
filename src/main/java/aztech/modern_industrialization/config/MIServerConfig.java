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

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MIServerConfig {
    public static final MIServerConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    static {
        var builder = new MIConfigBuilder();
        INSTANCE = new MIServerConfig(builder);
        SPEC = builder.build();
    }

    public final ModConfigSpec.IntValue forgeEnergyPerEu;
    public final ModConfigSpec.IntValue baseItemPipeTransfer;
    public final ModConfigSpec.BooleanValue spawnWithGuideBook;
    public final ModConfigSpec.BooleanValue respawnWithGuideBook;

    public final ModConfigSpec.BooleanValue compostableToPlantOil;
    public final ModConfigSpec.BooleanValue stonecutterToCuttingMachine;

    private MIServerConfig(MIConfigBuilder builder) {
        this.forgeEnergyPerEu = builder.start("forgeEnergyPerEu",
                "FE per EU",
                "How many Forge Energy units a single EU from MI is worth.")
                .defineInRange("forgeEnergyPerEu", 10, 1, 1000);
        this.baseItemPipeTransfer = builder.start("baseItemPipeTransfer",
                "Base item pipe transfer",
                "Base amount of items transferred by item pipes every 3 seconds.")
                .defineInRange("baseItemPipeTransfer", 16, 1, 1024);
        this.spawnWithGuideBook = builder.start("spawnWithGuideBook",
                "Spawn with guidebook",
                "Grant guidebook the first time a player joins the server.")
                .define("spawnWithGuideBook", true);
        this.respawnWithGuideBook = builder.start("respawnWithGuideBook",
                "Respawn with guidebook",
                "Grant guidebook when a player respawns after death.")
                .define("respawnWithGuideBook", true);

        builder.pushSection("recipes", "Recipes");
        this.compostableToPlantOil = builder.start("compostableToPlantOil",
                "Compostable plant oil recipes",
                "Generate plant oil recipes in the centrifuge for all compostable items.")
                .define("compostableToPlantOil", true);
        this.stonecutterToCuttingMachine = builder.start("stonecutterToCuttingMachine",
                "Stonecutter cutting machine recipes",
                "Generate cutting machine recipes for all stonecutter recipes.")
                .define("stonecutterToCuttingMachine", true);
        builder.popSection();
    }
}
