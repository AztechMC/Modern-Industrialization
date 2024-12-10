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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.machines.models.MachineBakedModel;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MachineCasingsProvider extends ModelProvider<BlockModelBuilder> {
    public MachineCasingsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MI.ID, MachineBakedModel.CASING_FOLDER, BlockModelBuilder::new, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var casing : MachineCasings.registeredCasings.values()) {
            if (casing.imitatedBlock != null) {
                imitateBlock(casing, casing.imitatedBlock.get());
            }
        }

        imitateBlock(CableTier.LV.casing, MIBlock.BASIC_MACHINE_HULL.asBlock());
        imitateBlock(CableTier.MV.casing, MIBlock.ADVANCED_MACHINE_HULL.asBlock());
        imitateBlock(CableTier.HV.casing, MIBlock.TURBO_MACHINE_HULL.asBlock());
        imitateBlock(CableTier.EV.casing, MIBlock.HIGHLY_ADVANCED_MACHINE_HULL.asBlock());
        imitateBlock(CableTier.SUPERCONDUCTOR.casing, MIBlock.QUANTUM_MACHINE_HULL.asBlock());

        cubeBottomTop(MachineCasings.BRICKED_BRONZE, "block/casings/bricked_bronze", "block/fire_clay_bricks", "block/bronze_machine_casing");
        cubeBottomTop(MachineCasings.BRICKED_STEEL, "block/casings/bricked_steel", "block/fire_clay_bricks", "block/steel_machine_casing");
        cubeAll(MachineCasings.CONFIGURABLE_TANK, "block/casings/configurable_tank");
        cubeAll(MachineCasings.STEEL_CRATE, "block/casings/steel_crate");
    }

    private void imitateBlock(MachineCasing casing, Block block) {
        getBuilder(casing.key.toString())
                .customLoader((bmb, existingFileHelper) -> new UseBlockModelModelBuilder<>(block, bmb, existingFileHelper));
    }

    private void cubeBottomTop(MachineCasing casing, String side, String bottom, String top) {
        cubeBottomTop(casing.key.toString(), MI.id(side), MI.id(bottom), MI.id(top));
    }

    private void cubeAll(MachineCasing casing, String side) {
        cubeAll(casing.key.toString(), MI.id(side));
    }

    @Override
    public String getName() {
        return "Machine Casings";
    }
}
