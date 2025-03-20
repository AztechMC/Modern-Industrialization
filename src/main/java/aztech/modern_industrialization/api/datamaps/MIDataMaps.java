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
package aztech.modern_industrialization.api.datamaps;

import aztech.modern_industrialization.MI;
import java.util.ArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;

public final class MIDataMaps {
    public static final DataMapType<Fluid, FluidFuel> FLUID_FUELS = DataMapType
            .builder(
                    MI.id("fluid_fuels"), Registries.FLUID, FluidFuel.CODEC)
            .synced(FluidFuel.CODEC, true)
            .build();

    /**
     * Items that can be added to item pipes, to increase the maximum amount of items moved at once.
     */
    public static final DataMapType<Item, ItemPipeUpgrade> ITEM_PIPE_UPGRADES = DataMapType
            .builder(
                    MI.id("item_pipe_upgrades"), Registries.ITEM, ItemPipeUpgrade.CODEC)
            .synced(ItemPipeUpgrade.CODEC, true)
            .build();

    /**
     * Items that can be added to electric machines, to increase the maximum EU/t they can support.
     */
    public static final DataMapType<Item, MachineUpgrade> MACHINE_UPGRADES = DataMapType
            .builder(
                    MI.id("machine_upgrades"), Registries.ITEM, MachineUpgrade.CODEC)
            .synced(MachineUpgrade.CODEC, true)
            .build();

    /**
     * Allows attaching a tooltip to arbitrary items.
     */
    public static final AdvancedDataMapType<Item, ItemTooltip, DataMapValueRemover.Default<ItemTooltip, Item>> ITEM_TOOLTIPS = AdvancedDataMapType
            .builder(
                    MI.id("item_tooltips"), Registries.ITEM, ItemTooltip.CODEC)
            .synced(ItemTooltip.CODEC, true)
            .remover(DataMapValueRemover.Default.codec())
            .merger((registry, first, firstValue, second, secondValue) -> {
                var components = new ArrayList<>(firstValue.components());
                components.addAll(secondValue.components());
                return new ItemTooltip(components);
            })
            .build();

    private MIDataMaps() {
    }
}
