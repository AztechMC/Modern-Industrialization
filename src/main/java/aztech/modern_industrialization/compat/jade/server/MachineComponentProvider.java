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
package aztech.modern_industrialization.compat.jade.server;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.api.machine.component.FluidAccess;
import aztech.modern_industrialization.api.machine.component.ItemAccess;
import aztech.modern_industrialization.api.machine.holder.CrafterComponentHolder;
import aztech.modern_industrialization.api.machine.holder.EnergyComponentHolder;
import aztech.modern_industrialization.api.machine.holder.EnergyListComponentHolder;
import aztech.modern_industrialization.api.machine.holder.FluidStorageComponentHolder;
import aztech.modern_industrialization.api.machine.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.EnergyView;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;

public abstract sealed class MachineComponentProvider<S, C>
        implements IServerExtensionProvider<MachineBlockEntity, S>, IClientExtensionProvider<S, C> {
    @Override
    public ResourceLocation getUid() {
        return MI.id("machine");
    }

    public static final class Energy extends MachineComponentProvider<CompoundTag, EnergyView> {
        @Override
        public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor, MachineBlockEntity machine) {
            if (machine instanceof EnergyListComponentHolder holder) {
                var components = holder.getEnergyComponents();

                if (!components.isEmpty()) {
                    long stored = 0;
                    long capacity = 0;

                    for (var component : components) {
                        stored += component.getEu();
                        capacity += component.getCapacity();
                    }

                    // TODO: set unit to EU once supported by the Jade API
                    return List.of(new ViewGroup<>(List.of(EnergyView.of(stored, capacity))));
                }
            } else if (machine instanceof EnergyComponentHolder holder) {
                var component = holder.getEnergyComponent();
                return List.of(new ViewGroup<>(List.of(EnergyView.of(component.getEu(), component.getCapacity()))));
            }

            return List.of();
        }

        @Override
        public List<ClientViewGroup<EnergyView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> list) {
            // TODO: fix spacing between EU and the rest (another day)
            return ClientViewGroup.map(list, tag -> EnergyView.read(tag, " EU"), null);
        }
    }

    public static final class Fluids extends MachineComponentProvider<CompoundTag, FluidView> {
        @Override
        public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor, MachineBlockEntity machine) {
            if (machine instanceof MultiblockInventoryComponentHolder holder) {
                var component = holder.getMultiblockInventoryComponent();
                var inputs = component.getFluidInputs();
                var outputs = component.getFluidOutputs();

                if (!inputs.isEmpty() || !outputs.isEmpty()) {
                    var fluidData = new ViewGroup<CompoundTag>(new ArrayList<>());
                    addFluids(fluidData, inputs);
                    addFluids(fluidData, outputs);
                    return List.of(fluidData);
                } else {
                    return List.of();
                }
            } else if (machine instanceof FluidStorageComponentHolder holder) {
                var component = holder.getFluidStorageComponent();

                if (component != null) {
                    var fluid = component.getVariant();
                    var fluidData = new ViewGroup<CompoundTag>(new ArrayList<>());
                    fluidData.views.add(FluidView.writeDefault(MIJadeCommonPlugin.fluidStack(fluid, component.getAmount()), component.getCapacity()));
                    return List.of(fluidData);
                } else {
                    return List.of();
                }
            } else {
                var stacks = machine.getInventory().getFluidStacks();
                var fluidData = new ViewGroup<CompoundTag>(new ArrayList<>());
                addFluids(fluidData, stacks);
                return List.of(fluidData);
            }
        }

        private void addFluids(ViewGroup<CompoundTag> data, List<? extends FluidAccess> stacks) {
            for (var stack : stacks) {
                data.views.add(FluidView.writeDefault(MIJadeCommonPlugin.fluidStack(stack.getVariant(), stack.getAmount()), stack.getCapacity()));
            }
        }

        @Override
        public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> list) {
            return ClientViewGroup.map(list, FluidView::readDefault, null);
        }
    }

    public static final class Items extends MachineComponentProvider<ItemStack, ItemView> {
        @Override
        public List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor, MachineBlockEntity machine) {
            if (machine instanceof MultiblockInventoryComponentHolder holder) {
                var component = holder.getMultiblockInventoryComponent();
                var inputs = component.getItemInputs();
                var outputs = component.getItemOutputs();

                if (!inputs.isEmpty() || !outputs.isEmpty()) {
                    var itemData = new ViewGroup<ItemStack>(new ArrayList<>());
                    addItems(itemData, inputs);
                    addItems(itemData, outputs);
                    return List.of(itemData);
                } else {
                    return List.of();
                }
            } else {
                var itemData = new ViewGroup<ItemStack>(new ArrayList<>());
                addItems(itemData, machine.getInventory().getItemStacks());
                return List.of(itemData);
            }
        }

        private void addItems(ViewGroup<ItemStack> data, List<? extends ItemAccess> stacks) {
            for (var stack : stacks) {
                data.views.add(stack.toStack());
            }
        }

        @Override
        public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> list) {
            return ClientViewGroup.map(list, ItemView::new, null);
        }
    }

    // TODO: probably needs to be rewritten to be standalone for a better display
    public static final class Progress extends MachineComponentProvider<CompoundTag, ProgressView> {
        @Override
        public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor, MachineBlockEntity machine) {
            if (machine instanceof CrafterComponentHolder holder) {
                var component = holder.getCrafterComponent();
                var progress = component.getProgress();

                if (progress > 0.0f) {
                    var inventory = holder.getCrafterComponent().getInventory();
                    var progressData = new ViewGroup<CompoundTag>(new ArrayList<>());

                    // TODO potentially add inventory information if the Jade API ever supports it
//                    for (var stack : inventory.getItemInputs()) {
//                        progressData.input(stack.toStack());
//                    }
//
//                    for (var stack : inventory.getItemOutputs()) {
//                        progressData.output(stack.toStack());
//                    }

                    progressData.views.add(ProgressView.create(progress));
                    return List.of(progressData);
                }
            }
            return List.of();
        }

        @Override
        public List<ClientViewGroup<ProgressView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> list) {
            return ClientViewGroup.map(list, ProgressView::read, null);
        }
    }
}
