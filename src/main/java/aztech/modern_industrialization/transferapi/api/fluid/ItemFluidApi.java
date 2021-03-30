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
package aztech.modern_industrialization.transferapi.api.fluid;

import aztech.modern_industrialization.transferapi.api.context.ContainerItemContext;
import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import aztech.modern_industrialization.transferapi.api.item.ItemPreconditions;
import aztech.modern_industrialization.transferapi.impl.fluid.EmptyItemsRegistry;
import aztech.modern_industrialization.transferapi.impl.fluid.FluidApiImpl;
import aztech.modern_industrialization.transferapi.impl.fluid.SimpleFluidContainingItem;
import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidPreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

// TODO: delegate to static FluidApiImpl functions so that other impl classes can be package private
public final class ItemFluidApi {
    public static final ItemApiLookup<Storage<Fluid>, ContainerItemContext> ITEM = ItemApiLookup.get(new Identifier("fabric:fluid_api"),
            Storage.asClass(), ContainerItemContext.class);

    /**
     * Register an item that contains a fluid and can be emptied of it entirely.
     *
     * <p>
     * Note: If a provider was already registered for the full item, this function
     * will do nothing.
     * 
     * @param fullItem  The item that contains the fluid.
     * @param fluid     The contained fluid. May not be empty.
     * @param amount    The amount of fluid in the full item. Must be positive.
     * @param emptyItem The emptied item.
     */
    public static void registerFullItem(Item fullItem, Fluid fluid, long amount, Item emptyItem) {
        registerFullItem(fullItem, fluid, amount, key -> ItemKey.of(emptyItem, key.copyTag()));
    }

    /**
     * Register an item that contains a fluid and can be emptied of it entirely.
     *
     * <p>
     * Note: If a provider was already registered for the full item, this function
     * will do nothing.
     * 
     * @param fullItem   The item that contains the fluid.
     * @param fluid      The contained fluid. May not be empty.
     * @param amount     The amount of fluid in the full item. Must be positive.
     * @param keyMapping A function mapping the key of the source item to that of
     *                   the target item.
     */
    public static void registerFullItem(Item fullItem, Fluid fluid, long amount, Function<ItemKey, ItemKey> keyMapping) {
        ItemPreconditions.notEmpty(fullItem);
        FluidPreconditions.notEmpty(fluid);
        Preconditions.checkArgument(amount > 0);

        ITEM.registerForItems((stack, ctx) -> new SimpleFluidContainingItem(ctx, ItemKey.of(stack), fluid, amount, keyMapping), fullItem);
    }

    /**
     * Register an item that is empty, and may be filled with some fluid entirely.
     */
    // TODO: document params and conflicts
    // TODO: pick parameter order, probably the same for both methods?
    public static void registerEmptyItem(Item emptyItem, Fluid fluid, long amount, Item fullItem) {
        registerEmptyItem(emptyItem, fluid, amount, key -> ItemKey.of(fullItem, key.copyTag()));
    }

    /**
     * Register an item that is empty, and may be filled with some fluid entirely.
     */
    // TODO: document params and conflicts
    // TODO: pick parameter order, probably the same for both methods?
    public static void registerEmptyItem(Item emptyItem, Fluid fluid, long amount, Function<ItemKey, ItemKey> keyMapping) {
        ItemPreconditions.notEmpty(emptyItem);
        FluidPreconditions.notEmpty(fluid);
        Preconditions.checkArgument(amount > 0);
        Objects.requireNonNull(keyMapping);

        EmptyItemsRegistry.registerEmptyItem(emptyItem, fluid, amount, keyMapping);
    }

    /**
     * Register full and empty variants of a fluid container item. Calls both
     * {@link #registerEmptyItem(Item, Fluid, long, Item) registerEmptyItem} and
     * {@link #registerFullItem}.
     * 
     * @param emptyItem The empty variant of the container.
     * @param fluid     The fluid.
     * @param amount    The amount.
     * @param fullItem  The full variant of the container.
     */
    public static void registerEmptyAndFullItems(Item emptyItem, Fluid fluid, long amount, Item fullItem) {
        registerEmptyItem(emptyItem, fluid, amount, fullItem);
        registerFullItem(fullItem, fluid, amount, emptyItem);
    }

    // TODO: potion handling

    private ItemFluidApi() {
    }

    static {
        FluidApiImpl.init();
    }
}
