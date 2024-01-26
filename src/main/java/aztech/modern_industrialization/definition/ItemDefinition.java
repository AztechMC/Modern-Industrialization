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

import aztech.modern_industrialization.items.SortOrder;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.registries.DeferredItem;

public class ItemDefinition<T extends Item> extends Definition implements ItemLike {

    private final DeferredItem<T> item;
    public final SortOrder sortOrder;

    public final BiConsumer<Item, ItemModelProvider> modelGenerator;
    private Consumer<? super T> onItemRegistrationEvent;

    public ItemDefinition(String englishName, DeferredItem<T> item,
            BiConsumer<Item, ItemModelProvider> modelGenerator, SortOrder sortOrder) {
        super(englishName, item.getId().getPath());
        this.item = item;
        this.modelGenerator = modelGenerator;
        this.onItemRegistrationEvent = null;
        this.sortOrder = sortOrder;
    }

    public ItemDefinition<T> withItemRegistrationEvent(Consumer<? super T> onItemRegistrationEvent) {
        this.onItemRegistrationEvent = onItemRegistrationEvent;
        return this;
    }

    public void onRegister() {
        if (this.onItemRegistrationEvent != null) {
            this.onItemRegistrationEvent.accept(item.get());
        }
    }

    public ItemStack stack() {
        return stack(1);
    }

    public ItemStack stack(int stackSize) {
        return new ItemStack(item.get(), stackSize);
    }

    public boolean is(ItemStack stack) {
        return stack.is(item);
    }

    @Override
    public T asItem() {
        return item.get();
    }

    @Override
    public String getTranslationKey() {
        return item.get().getDescriptionId();
    }

}
