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

import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.bridge.SlotFluidHandler;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.bridge.SlotItemHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

public class MICapabilities {
    private static final List<Consumer<RegisterCapabilitiesEvent>> processors = new ArrayList<>();

    public static void init(RegisterCapabilitiesEvent event) {
        // Fluids
        for (var fluid : MIFluids.FLUID_DEFINITIONS.values()) {
            event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), fluid.getBucket());
        }

        // Delayed processors
        processors.forEach(c -> c.accept(event));

        // Misc
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MIRegistries.CREATIVE_BARREL_BE.get(), (be, side) -> new SlotItemHandler(be));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, MIRegistries.CREATIVE_TANK_BE.get(), (be, side) -> new SlotFluidHandler(be));
        event.registerBlockEntity(EnergyApi.SIDED, MIRegistries.CREATIVE_STORAGE_UNIT_BE.get(), (be, side) -> EnergyApi.CREATIVE);

        // Energy compat
        var allBlocks = StreamSupport.stream(BuiltInRegistries.BLOCK.spliterator(), false)
                .toArray(Block[]::new);
        var allItems = StreamSupport.stream(BuiltInRegistries.ITEM.spliterator(), false)
                .toArray(Item[]::new);
        EnergyApi.init(event, allBlocks, allItems);
    }

    public static void onEvent(Consumer<RegisterCapabilitiesEvent> consumer) {
        processors.add(consumer);
    }
}
