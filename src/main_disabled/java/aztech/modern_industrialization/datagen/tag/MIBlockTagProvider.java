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

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.pipes.MIPipes;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class MIBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public MIBlockTagProvider(FabricDataOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(packOutput, registriesFuture);
    }

    @Override
    protected FabricTagBuilder tag(TagKey<Block> tag) {
        return getOrCreateTagBuilder(tag);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (BlockDefinition<?> definition : MIBlock.BLOCKS.values()) {
            for (var tag : definition.tags) {
                tag(tag).add(definition.asBlock());
            }
        }

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(MIPipes.BLOCK_PIPE);
        tag(ConventionalBlockTags.MOVEMENT_RESTRICTED).add(MIPipes.BLOCK_PIPE);
        // Have no idea why there is such a tag but go add it
        tag(ConventionalBlockTags.QUARTZ_ORES).add(BuiltInRegistries.BLOCK.get(new MIIdentifier("quartz_ore")));

        // Why is this not in Carrier? :(
        tag(TagKey.create(Registries.BLOCK, new ResourceLocation("carrier", "blacklist"))).addTag(ConventionalBlockTags.MOVEMENT_RESTRICTED);
    }
}
