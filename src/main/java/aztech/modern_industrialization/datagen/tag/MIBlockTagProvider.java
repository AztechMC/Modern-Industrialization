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
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jspecify.annotations.Nullable;

public class MIBlockTagProvider extends BlockTagsProvider {
    public MIBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MI.ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (BlockDefinition<?> definition : MIBlock.BLOCK_DEFINITIONS.values()) {
            for (var tag : definition.tags) {
                tag(tag).add(definition.asBlock());
            }
        }

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(MIPipes.BLOCK_PIPE.get());
        tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED).add(MIPipes.BLOCK_PIPE.get());

        for (var entry : TagsToGenerate.tagToItemMap.entrySet()) {
            boolean optional = TagsToGenerate.optionalTags.contains(entry.getKey());
            var items = entry.getValue().stream()
                    .map(ItemLike::asItem)
                    .sorted(Comparator.comparing(BuiltInRegistries.ITEM::getKey))
                    .toList();
            for (var item : items) {
                var itemKey = BuiltInRegistries.ITEM.getKey(item);
                var block = BuiltInRegistries.BLOCK.getOptional(itemKey);
                if (block.isEmpty()) {
                    continue;
                }
                var key = BlockTags.create(entry.getKey().location());
                if (optional) {
                    tag(key).addOptional(itemKey);
                } else {
                    tag(key).add(block.get());
                }
            }
        }

        for (var entry : TagsToGenerate.tagToBeAddedToAnotherTag.entrySet()) {
            var tagId = ResourceLocation.parse(entry.getKey());
            for (var tag : entry.getValue()) {
                if (this.builders.containsKey(ResourceLocation.parse(tag))) {
                    tag(key(tagId)).addTag(key(tag));
                }
            }
        }
    }

    private static TagKey<Block> key(ResourceLocation id) {
        return TagKey.create(BuiltInRegistries.BLOCK.key(), id);
    }

    private static TagKey<Block> key(String id) {
        return key(ResourceLocation.parse(id));
    }
}
