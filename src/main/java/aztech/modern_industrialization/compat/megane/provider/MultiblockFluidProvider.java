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
package aztech.modern_industrialization.compat.megane.provider;

import aztech.modern_industrialization.compat.megane.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import java.util.List;
import lol.bai.megane.api.provider.FluidProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class MultiblockFluidProvider extends FluidProvider<MultiblockInventoryComponentHolder> {
    private List<ConfigurableFluidStack> inputs;
    private List<ConfigurableFluidStack> outputs;

    @Override
    public void setContext(Level world, BlockPos pos, Player player, MultiblockInventoryComponentHolder holder) {
        super.setContext(world, pos, player, holder);

        this.inputs = holder.getMultiblockInventoryComponent().getFluidInputs();
        this.outputs = holder.getMultiblockInventoryComponent().getFluidOutputs();
    }

    @Override
    public int getSlotCount() {
        return inputs.size() + outputs.size();
    }

    @Override
    public @Nullable Fluid getFluid(int slot) {
        return getFluidStack(slot).getResource().getFluid();
    }

    @Override
    public double getStored(int slot) {
        return droplets(getFluidStack(slot).getAmount());
    }

    @Override
    public double getMax(int slot) {
        return droplets(getFluidStack(slot).getCapacity());
    }

    private ConfigurableFluidStack getFluidStack(int slot) {
        return slot < inputs.size() ? inputs.get(slot) : outputs.get(slot - inputs.size());
    }
}
