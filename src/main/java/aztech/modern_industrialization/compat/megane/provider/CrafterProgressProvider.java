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

import aztech.modern_industrialization.compat.megane.holder.CrafterComponentHolder;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import com.google.common.primitives.Ints;
import lol.bai.megane.api.provider.ProgressProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CrafterProgressProvider extends ProgressProvider<CrafterComponentHolder> {
    private CrafterComponent crafter;
    private CrafterComponent.Inventory inventory;

    @Override
    public void setContext(Level world, BlockPos pos, Player player, CrafterComponentHolder holder) {
        super.setContext(world, pos, player, holder);

        this.crafter = holder.getCrafterComponent();
        this.inventory = holder.getCrafterComponent().getInventory();
    }

    @Override
    public int getInputSlotCount() {
        return inventory.getItemInputs().size();
    }

    @Override
    public int getOutputSlotCount() {
        return inventory.getItemOutputs().size();
    }

    @Override
    public @NotNull ItemStack getInputStack(int slot) {
        return toStack(inventory.getItemInputs().get(slot));
    }

    @Override
    public @NotNull ItemStack getOutputStack(int slot) {
        return toStack(inventory.getItemOutputs().get(slot));
    }

    @Override
    public int getPercentage() {
        return (int) (crafter.getProgress() * 100);
    }

    private static ItemStack toStack(ConfigurableItemStack stack) {
        return stack.getResource().toStack(Ints.saturatedCast(stack.getAmount()));
    }
}
