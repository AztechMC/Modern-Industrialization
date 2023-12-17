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
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.definition.ItemDefinition;
import aztech.modern_industrialization.pipes.MIPipes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.world.level.block.Blocks;

public class ModelProvider extends FabricModelProvider {

    public ModelProvider(FabricDataOutput packOutput) {
        super(packOutput);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        for (BlockDefinition<?> blockDefinition : MIBlock.BLOCKS.values()) {
            if (blockDefinition.modelGenerator != null) {
                blockDefinition.modelGenerator.accept(blockDefinition.asBlock(), blockStateModelGenerator);
            }
        }

        for (FluidDefinition fluidDefinition : MIFluids.FLUIDS.values()) {
            blockStateModelGenerator.createNonTemplateModelBlock(fluidDefinition.fluidBlock, Blocks.AIR);
        }

        blockStateModelGenerator.createNonTemplateModelBlock(MIPipes.BLOCK_PIPE);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        for (ItemDefinition<?> itemDefinition : MIItem.ITEMS.values()) {
            if (itemDefinition.modelGenerator != null) {
                itemDefinition.modelGenerator.accept(itemDefinition.asItem(), itemModelGenerator);
            }
        }
    }

}
