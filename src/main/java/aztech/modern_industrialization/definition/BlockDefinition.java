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
package aztech.modern_industrialization.definition;

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.items.SortOrder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class BlockDefinition<T extends Block> extends Definition implements ItemLike {

    public final T block;
    public final ItemDefinition<BlockItem> blockItem;

    public final BiConsumer<Block, BlockModelGenerators> modelGenerator;
    public final BiConsumer<Block, BlockLootSubProvider> lootTableGenerator;
    public final List<TagKey<Block>> tags;

    private BiConsumer<Block, Item> onBlockRegistrationEvent;

    public BlockDefinition(String englishName, String id, T block,
            BiFunction<? super T, FabricItemSettings, BlockItem> blockItemCtor,
            BiConsumer<Block, BlockModelGenerators> modelGenerator,
            BiConsumer<Item, ItemModelGenerators> itemModelGenerator,
            BiConsumer<Block, BlockLootSubProvider> lootTableGenerator,
            List<TagKey<Block>> tags,
            SortOrder sortOrder) {

        super(englishName, id, false);
        this.block = block;
        this.blockItem = MIItem.item(
                englishName,
                id,
                s -> blockItemCtor.apply(block, s),
                itemModelGenerator,
                sortOrder);
        this.modelGenerator = modelGenerator;
        this.lootTableGenerator = lootTableGenerator;
        this.tags = tags;
    }

    public BlockDefinition<T> withBlockRegistrationEvent(BiConsumer<Block, Item> onBlockRegistrationEvent) {
        this.onBlockRegistrationEvent = onBlockRegistrationEvent;
        return this;
    }

    @Override
    public Item asItem() {
        if (blockItem != null) {
            return blockItem.asItem();
        } else {
            return null;
        }

    }

    public T asBlock() {
        return block;
    }

    @Override
    public String getTranslationKey() {
        return blockItem.getTranslationKey();
    }

    public void onRegister() {
        if (onBlockRegistrationEvent != null) {
            onBlockRegistrationEvent.accept(block, blockItem.asItem());
        }
    }

}
