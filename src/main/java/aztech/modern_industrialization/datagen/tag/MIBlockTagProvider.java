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

package aztech.modern_industrialization.datagen.tag;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.pipes.MIPipes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

public class MIBlockTagProvider extends BlockTagsProvider {
    public MIBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, MI.ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (BlockDefinition<?> definition : MIBlock.BLOCK_DEFINITIONS.values()) {
            for (var tag : definition.tags) {
                tag(tag).add(kb(definition.asBlock()));
            }
        }

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(kb(MIPipes.BLOCK_PIPE.get()));
        tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED).add(kb(MIPipes.BLOCK_PIPE.get()));

        for (var entry : TagsToGenerate.getTags().entrySet()) {
            boolean optional = TagsToGenerate.optionalTags.contains(entry.getKey());
            for (var item : entry.getValue()) {
                var itemKey = BuiltInRegistries.ITEM.getKey(item);
                var block = BuiltInRegistries.BLOCK.getOptional(itemKey);
                if (block.isEmpty()) {
                    continue;
                }
                var key = BlockTags.create(entry.getKey().location());
                if (optional) {
                    tag(key).addOptional(kb(block.get()));
                } else {
                    tag(key).add(kb(block.get()));
                }
            }
        }

        for (var entry : TagsToGenerate.tagToBeAddedToAnotherTag.entrySet()) {
            for (var tag : entry.getValue()) {
                // Skip item tag if it was already skipped above due to no item existing
                if (this.builders.containsKey(tag.location())) {
                    tag(key(entry.getKey().location())).addTag(key(tag.location()));
                }
            }
        }
    }

    // MC 26.2: TagAppender.add now takes ResourceKey instead of the block object.
    private static ResourceKey<Block> kb(Block block) {
        return block.builtInRegistryHolder().key();
    }

    private static TagKey<Block> key(Identifier id) {
        return TagKey.create(BuiltInRegistries.BLOCK.key(), id);
    }

    private static TagKey<Block> key(String id) {
        return key(Identifier.parse(id));
    }
}
