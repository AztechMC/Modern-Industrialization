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
package aztech.modern_industrialization.datagen.model;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.materials.part.MIParts;
import com.google.gson.JsonParser;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class MachineCasingsProvider implements DataProvider {
    private final FabricDataOutput packOutput;

    public MachineCasingsProvider(FabricDataOutput packOutput) {
        this.packOutput = packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Path outputPath = packOutput.getOutputFolder();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        generate((casing, block) -> {
            var modelPath = "assets/%s/models/machine_casing/%s.json".formatted(packOutput.getModId(), casing.name);

            var json = JsonParser.parseString("""
                    {
                      "block": "%s"
                    }
                    """.formatted(BuiltInRegistries.BLOCK.getKey(block)));

            futures.add(DataProvider.saveStable(output, json, outputPath.resolve(modelPath)));
        });

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void generate(BiConsumer<MachineCasing, Block> imitateBlock) {
        imitateBlock.accept(CableTier.LV.casing, MIBlock.BASIC_MACHINE_HULL.asBlock());
        imitateBlock.accept(CableTier.MV.casing, MIBlock.ADVANCED_MACHINE_HULL.asBlock());
        imitateBlock.accept(CableTier.HV.casing, MIBlock.TURBO_MACHINE_HULL.asBlock());
        imitateBlock.accept(CableTier.EV.casing, MIBlock.HIGHLY_ADVANCED_MACHINE_HULL.asBlock());
        imitateBlock.accept(CableTier.SUPERCONDUCTOR.casing, MIBlock.QUANTUM_MACHINE_HULL.asBlock());

        imitateBlock.accept(MachineCasings.FIREBRICKS, MIBlock.BLOCK_FIRE_CLAY_BRICKS.asBlock());
        imitateBlock.accept(MachineCasings.BRICKS, Blocks.BRICKS);

        imitateBlock.accept(MachineCasings.BRONZE, MIMaterials.BRONZE.getPart(MIParts.MACHINE_CASING).asBlock());
        imitateBlock.accept(MachineCasings.BRONZE_PLATED_BRICKS, MIMaterials.BRONZE.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());
        imitateBlock.accept(MachineCasings.CLEAN_STAINLESS_STEEL, MIMaterials.STAINLESS_STEEL.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());
        imitateBlock.accept(MachineCasings.FROSTPROOF, MIMaterials.ALUMINUM.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());
        imitateBlock.accept(MachineCasings.HEATPROOF, MIMaterials.INVAR.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());
        imitateBlock.accept(MachineCasings.STAINLESS_STEEL_PIPE, MIMaterials.STAINLESS_STEEL.getPart(MIParts.MACHINE_CASING_PIPE).asBlock());
        imitateBlock.accept(MachineCasings.STEEL, MIMaterials.STEEL.getPart(MIParts.MACHINE_CASING).asBlock());
        imitateBlock.accept(MachineCasings.TITANIUM, MIMaterials.TITANIUM.getPart(MIParts.MACHINE_CASING).asBlock());
        imitateBlock.accept(MachineCasings.TITANIUM_PIPE, MIMaterials.TITANIUM.getPart(MIParts.MACHINE_CASING_PIPE).asBlock());
        imitateBlock.accept(MachineCasings.SOLID_TITANIUM, MIMaterials.TITANIUM.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());
        imitateBlock.accept(MachineCasings.NUCLEAR, MIMaterials.NUCLEAR_ALLOY.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());
        imitateBlock.accept(MachineCasings.PLASMA_HANDLING_IRIDIUM, MIMaterials.IRIDIUM.getPart(MIParts.MACHINE_CASING_SPECIAL).asBlock());

        for (var entry : MachineCasingImitations.imitationsToGenerate.entrySet()) {
            imitateBlock.accept(entry.getKey(), BuiltInRegistries.BLOCK.getOrThrow(ResourceKey.create(Registries.BLOCK, entry.getValue())));
        }
    }

    @Override
    public String getName() {
        return "Machine Casings";
    }
}
