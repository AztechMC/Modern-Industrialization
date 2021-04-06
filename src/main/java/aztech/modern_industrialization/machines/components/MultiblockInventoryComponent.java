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
package aztech.modern_industrialization.machines.components;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public class MultiblockInventoryComponent implements CrafterComponent.Inventory {
    private final List<ConfigurableItemStack> itemInputs = new ArrayList<>();
    private final List<ConfigurableItemStack> itemOutputs = new ArrayList<>();
    private final List<ConfigurableFluidStack> fluidInputs = new ArrayList<>();
    private final List<ConfigurableFluidStack> fluidOutputs = new ArrayList<>();

    public void rebuild(ShapeMatcher shapeMatcher) {
        // Sort the hatches by height for the distillation tower
        List<HatchBlockEntity> sortedHatches = new ArrayList<>(shapeMatcher.getMatchedHatches());
        sortedHatches.sort(Comparator.comparing(h -> h.getPos().getY()));
        // Accumulate the slots
        rebuildList(sortedHatches, itemInputs, HatchBlockEntity::appendItemInputs);
        rebuildList(sortedHatches, itemOutputs, HatchBlockEntity::appendItemOutputs);
        rebuildList(sortedHatches, fluidInputs, HatchBlockEntity::appendFluidInputs);
        rebuildList(sortedHatches, fluidOutputs, HatchBlockEntity::appendFluidOutputs);
    }

    private <Stack> void rebuildList(List<HatchBlockEntity> sortedHatches, List<Stack> stacks, BiConsumer<HatchBlockEntity, List<Stack>> appender) {
        stacks.clear();
        // Add all hatch slots
        for (HatchBlockEntity hatch : sortedHatches) {
            appender.accept(hatch, stacks);
        }
    }

    @Override
    public List<ConfigurableItemStack> getItemInputs() {
        return itemInputs;
    }

    @Override
    public List<ConfigurableItemStack> getItemOutputs() {
        return itemOutputs;
    }

    @Override
    public List<ConfigurableFluidStack> getFluidInputs() {
        return fluidInputs;
    }

    @Override
    public List<ConfigurableFluidStack> getFluidOutputs() {
        return fluidOutputs;
    }
}
