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
package aztech.modern_industrialization.datagen.loot;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.pipes.MIPipes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;

public class BlockLootTableProvider extends FabricBlockLootTableProvider {

    public BlockLootTableProvider(FabricDataOutput packOutput) {
        super(packOutput);
    }

    @Override
    public void generate() {
        for (BlockDefinition<?> blockDefinition : MIBlock.BLOCKS.values()) {
            if (blockDefinition.lootTableGenerator != null) {
                blockDefinition.lootTableGenerator.accept(blockDefinition.block, this);
            } else {
                excludeFromStrictValidation(blockDefinition.block);
            }
        }

        // Loot is dynamic
        excludeFromStrictValidation(MIPipes.BLOCK_PIPE);
        // No drops for these
        for (var fluid : MIFluids.FLUIDS.values()) {
            excludeFromStrictValidation(fluid.fluidBlock);
        }
    }
}
