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

package aztech.modern_industrialization;

import aztech.modern_industrialization.blocks.storage.ResourceStorage;
import aztech.modern_industrialization.items.SteamDrillFuel;
import aztech.modern_industrialization.pipes.item.SavedItemPipeConfig;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.MIExtraCodecs;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MIComponents {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MI.ID);

    public static final Supplier<DataComponentType<Boolean>> ACTIVATED = COMPONENTS.registerComponentType("activated",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    public static final Supplier<DataComponentType<BlockState>> CAMOUFLAGE = COMPONENTS.registerComponentType("camouflage",
            builder -> builder.persistent(BlockState.CODEC));
    public static final Supplier<DataComponentType<Long>> ENERGY = COMPONENTS.registerComponentType("energy",
            builder -> builder.persistent(MIExtraCodecs.NON_NEGATIVE_LONG));
    public static final Supplier<DataComponentType<Boolean>> LOW_SIGNAL = COMPONENTS.registerComponentType("low_signal",
            builder -> builder.persistent(Codec.BOOL));
    public static final Supplier<DataComponentType<Integer>> REMAINING_DISINTEGRATIONS = COMPONENTS.registerComponentType("remaining_disintegrations",
            builder -> builder.persistent(ExtraCodecs.POSITIVE_INT));
    public static final Supplier<DataComponentType<SavedItemPipeConfig>> SAVED_CONFIG = COMPONENTS.registerComponentType("saved_config",
            builder -> builder.persistent(SavedItemPipeConfig.CODEC));
    public static final Supplier<DataComponentType<Boolean>> SILK_TOUCH = COMPONENTS.registerComponentType("silk_touch",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    public static final Supplier<DataComponentType<SteamDrillFuel>> STEAM_DRILL_FUEL = COMPONENTS.registerComponentType("steam_drill_fuel",
            builder -> builder.persistent(SteamDrillFuel.CODEC));
    public static final Supplier<DataComponentType<Integer>> WATER = COMPONENTS.registerComponentType("water",
            builder -> builder.persistent(ExtraCodecs.POSITIVE_INT));

    public static final Supplier<DataComponentType<SimpleFluidContent>> FLUID_CONTENT = COMPONENTS.registerComponentType("fluid_content",
            builder -> builder.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC));
    public static final Supplier<DataComponentType<ResourceStorage<FluidVariant>>> FLUID_STORAGE = COMPONENTS.registerComponentType("fluid_storage",
            ResourceStorage.component(FluidVariant.CODEC, FluidVariant.STREAM_CODEC));
    public static final Supplier<DataComponentType<ResourceStorage<ItemVariant>>> ITEM_STORAGE = COMPONENTS.registerComponentType("item_storage",
            ResourceStorage.component(ItemVariant.CODEC, ItemVariant.STREAM_CODEC));

    public static void init(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }

    private MIComponents() {}
}
