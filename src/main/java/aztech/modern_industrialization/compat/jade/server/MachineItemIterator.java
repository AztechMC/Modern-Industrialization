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

import aztech.modern_industrialization.api.machine.component.ItemAccess;
import aztech.modern_industrialization.api.machine.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import snownee.jade.addon.universal.ItemIterator;

public class MachineItemIterator extends ItemIterator<MachineBlockEntity> {
    public MachineItemIterator() {
        super(MachineBlockEntity.class::cast, 0);
    }

    @Override
    public Stream<ItemStack> populate(MachineBlockEntity machine) {
        List<ItemStack> stacks = new ArrayList<>();
        if (machine instanceof MultiblockInventoryComponentHolder multiblock) {
            var component = multiblock.getMultiblockInventoryComponent();
            addStacks(stacks, component.getItemInputs());
            addStacks(stacks, component.getItemOutputs());
        } else {
            addStacks(stacks, machine.getInventory().getItemStacks());
        }
        return stacks.isEmpty() ? Stream.empty() : stacks.stream();
    }

    private static void addStacks(List<ItemStack> stacks, List<? extends ItemAccess> itemAccesses) {
        for (ItemAccess access : itemAccesses) {
            stacks.add(access.toStack());
        }
    }
}
