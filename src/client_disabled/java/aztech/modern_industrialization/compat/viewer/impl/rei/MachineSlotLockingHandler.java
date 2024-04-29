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
package aztech.modern_industrialization.compat.viewer.impl.rei;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.machines.guicomponents.ReiSlotLockingClient;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.network.machines.ReiLockSlotsPacket;
import java.util.List;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

class MachineSlotLockingHandler implements TransferHandler {
    @Override
    public @NotNull Result handle(@NotNull Context context) {
        if (!(context.getDisplay() instanceof ViewerCategoryRei.ViewerDisplay<?>d && d.recipe instanceof RecipeHolder<?>holder
                && holder.value() instanceof MachineRecipe recipe))
            return Result.createNotApplicable();
        if (!(context.getMenu() instanceof MachineMenuClient handler))
            return Result.createNotApplicable();
        if (!canApply(handler, d.getCategoryIdentifier()))
            return Result.createNotApplicable();
        ReiSlotLockingClient slotLocking = handler.getComponent(ReiSlotLockingClient.class);
        if (slotLocking == null || !slotLocking.isLockingAllowed())
            return Result.createNotApplicable();
        if (context.isActuallyCrafting()) {
            new ReiLockSlotsPacket(handler.containerId, holder.id()).sendToServer();
        }
        return Result.createSuccessful().blocksFurtherHandling();
    }

    private boolean canApply(MachineMenuClient handler, CategoryIdentifier<?> category) {
        // Check if the block is in the worktables - it's a hack but it should work. :P
        String blockId = handler.guiParams.blockId;
        List<EntryIngredient> workstations = CategoryRegistry.getInstance().get(category).getWorkstations();
        for (EntryIngredient workstationEntries : workstations) {
            for (EntryStack<?> entry : workstationEntries) {
                Item item = entry.<ItemStack>cast().getValue().getItem();
                if (BuiltInRegistries.ITEM.getKey(item).equals(new MIIdentifier(blockId))) {
                    return true;
                }
            }
        }
        return false;
    }
}
