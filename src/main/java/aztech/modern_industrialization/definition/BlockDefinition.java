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
import aztech.modern_industrialization.datagen.loot.MIBlockLoot;
import aztech.modern_industrialization.datagen.model.BaseModelProvider;
import aztech.modern_industrialization.items.SortOrder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.Nullable;

public class BlockDefinition<T extends Block> extends Definition implements ItemLike, Supplier<T> {

    private final DeferredBlock<T> block;
    public final ItemDefinition<BlockItem> blockItem;

    public final BiConsumer<Block, BaseModelProvider> modelGenerator;
    @Nullable
    public final MIBlockLoot blockLoot;
    public final List<TagKey<Block>> tags;

    private BiConsumer<Block, Item> onBlockRegistrationEvent;

    public BlockDefinition(String englishName, DeferredBlock<T> block,
            BiFunction<? super T, Item.Properties, BlockItem> blockItemCtor,
            BiConsumer<Block, BaseModelProvider> modelGenerator,
            BiConsumer<Item, ItemModelProvider> itemModelGenerator,
            MIBlockLoot blockLoot,
            List<TagKey<Block>> tags,
            SortOrder sortOrder) {

        super(englishName, block.getId().getPath(), false);
        this.block = block;
        this.blockItem = MIItem.item(
                englishName,
                path(),
                s -> blockItemCtor.apply(block.get(), s),
                itemModelGenerator,
                sortOrder);
        this.modelGenerator = modelGenerator;
        this.blockLoot = blockLoot;
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
        return block.get();
    }

    @Override
    public String getTranslationKey() {
        return blockItem.getTranslationKey();
    }

    public void onRegister() {
        if (onBlockRegistrationEvent != null) {
            onBlockRegistrationEvent.accept(block.get(), blockItem.asItem());
        }
    }

    @Override
    public T get() {
        return asBlock();
    }
}
