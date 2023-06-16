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
package aztech.modern_industrialization.compat.waila.server;

import aztech.modern_industrialization.compat.waila.holder.CrafterComponentHolder;
import aztech.modern_industrialization.compat.waila.holder.EnergyComponentHolder;
import aztech.modern_industrialization.compat.waila.holder.EnergyListComponentHolder;
import aztech.modern_industrialization.compat.waila.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.EnergyComponent;
import com.google.common.primitives.Ints;
import java.util.List;
import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.data.EnergyData;
import mcp.mobius.waila.api.data.FluidData;
import mcp.mobius.waila.api.data.ItemData;
import mcp.mobius.waila.api.data.ProgressData;
import net.minecraft.world.item.ItemStack;

public class MachineComponentProvider implements IDataProvider<MachineBlockEntity> {

    @Override
    public void appendData(IDataWriter data, IServerAccessor<MachineBlockEntity> accessor, IPluginConfig config) {
        var machine = accessor.getTarget();

        data.add(EnergyData.class, res -> {
            if (machine instanceof EnergyListComponentHolder holder) {
                var components = holder.getEnergyComponents();

                if (!components.isEmpty()) {
                    long stored = 0;
                    long capacity = 0;

                    for (EnergyComponent component : components) {
                        stored += component.getEu();
                        capacity += component.getCapacity();
                    }

                    res.add(EnergyData.of(stored, capacity));
                }
            } else if (machine instanceof EnergyComponentHolder holder) {
                var component = holder.getEnergyComponent();
                res.add(EnergyData.of(component.getEu(), component.getCapacity()));
            }

            res.block();
        });

        data.add(FluidData.class, res -> {
            if (machine instanceof MultiblockInventoryComponentHolder holder) {
                var component = holder.getMultiblockInventoryComponent();
                var inputs = component.getFluidInputs();
                var outputs = component.getFluidOutputs();

                if (!inputs.isEmpty() || !outputs.isEmpty()) {
                    var fluidData = FluidData.of(inputs.size() + outputs.size());
                    addFluids(fluidData, inputs);
                    addFluids(fluidData, outputs);
                    res.add(fluidData);
                }
            } else {
                var stacks = machine.getInventory().getFluidStacks();
                var fluidData = FluidData.of(stacks.size());
                addFluids(fluidData, stacks);
                res.add(fluidData);
            }

            res.block();
        });

        data.add(ItemData.class, res -> {
            if (machine instanceof MultiblockInventoryComponentHolder holder) {
                var component = holder.getMultiblockInventoryComponent();
                var inputs = component.getItemInputs();
                var outputs = component.getItemOutputs();

                if (!inputs.isEmpty() || !outputs.isEmpty()) {
                    var itemData = ItemData.of(config);
                    addItems(itemData, inputs);
                    addItems(itemData, outputs);
                    res.add(itemData);
                }
            } else {
                var itemData = ItemData.of(config);
                addItems(itemData, machine.getInventory().getItemStacks());
                res.add(itemData);
            }

            res.block();
        });

        data.add(ProgressData.class, res -> {
            if (machine instanceof CrafterComponentHolder holder) {
                var component = holder.getCrafterComponent();
                var progress = component.getProgress();

                if (progress > 0.0f) {
                    var inventory = component.getInventory();
                    var progressData = ProgressData.ratio(progress);

                    for (ConfigurableItemStack stack : inventory.getItemInputs()) {
                        progressData.input(toStack(stack));
                    }

                    for (ConfigurableItemStack stack : inventory.getItemOutputs()) {
                        progressData.output(toStack(stack));
                    }

                    res.add(progressData);
                }
            }

            res.block();
        });
    }

    private void addFluids(FluidData data, List<ConfigurableFluidStack> stacks) {
        for (ConfigurableFluidStack stack : stacks) {
            data.add(stack.getResource().getFluid(), stack.getResource().getNbt(), stack.getAmount(), stack.getCapacity());
        }
    }

    private void addItems(ItemData data, List<ConfigurableItemStack> stacks) {
        data.ensureSpace(stacks.size());

        for (ConfigurableItemStack stack : stacks) {
            data.add(toStack(stack));
        }
    }

    private ItemStack toStack(ConfigurableItemStack stack) {
        return stack.getResource().toStack(Ints.saturatedCast(stack.getAmount()));
    }

}
