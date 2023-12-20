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
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.definition.ItemDefinition;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MIModelProvider extends BlockStateProvider {

    public MIModelProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MI.ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (BlockDefinition<?> blockDefinition : MIBlock.BLOCK_DEFINITIONS.values()) {
            if (blockDefinition.modelGenerator != null) {
                blockDefinition.modelGenerator.accept(blockDefinition.asBlock(), this);
            }
        }

        for (FluidDefinition fluidDefinition : MIFluids.FLUID_DEFINITIONS.values()) {
            existingModel(fluidDefinition.asFluidBlock(), Blocks.AIR);
        }

        // TODO NEO
//        blockStateModelGenerator.createNonTemplateModelBlock(MIPipes.BLOCK_PIPE);

        // Item models as well...
        for (ItemDefinition<?> itemDefinition : MIItem.ITEM_DEFINITIONS.values()) {
            if (itemDefinition.modelGenerator != null) {
                itemDefinition.modelGenerator.accept(itemDefinition.asItem(), itemModels());
            }
        }
    }

    public void existingModel(Block block) {
        existingModel(block, block);
    }

    public void existingModel(Block block, Block targetBlock) {
        simpleBlock(block, models().getExistingFile(blockTexture(targetBlock)));
    }
}
