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
import aztech.modern_industrialization.definition.BlockDefinition;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class BlockLootTableProvider extends BlockLootSubProvider {
    public BlockLootTableProvider() {
        super(Set.of(), FeatureFlags.VANILLA_SET);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return MIBlock.BLOCK_DEFINITIONS.values()
                .stream()
                .filter(x -> x.blockLoot != null)
                .<Block>map(BlockDefinition::asBlock)
                .toList();
    }

    @Override
    public void generate() {
        for (BlockDefinition<?> blockDefinition : MIBlock.BLOCK_DEFINITIONS.values()) {
            if (blockDefinition.blockLoot == null) {
                continue;
            }

            var block = blockDefinition.asBlock();
            if (blockDefinition.blockLoot instanceof MIBlockLoot.DropSelf) {
                dropSelf(block);
            } else if (blockDefinition.blockLoot instanceof MIBlockLoot.Ore ore) {
                add(block, createOreDrop(block, BuiltInRegistries.ITEM.get(new ResourceLocation(ore.loot()))));
            }
        }
    }
}
