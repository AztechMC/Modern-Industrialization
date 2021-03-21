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
package aztech.modern_industrialization.compat.rei.machines;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.MachinePackets;
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.components.sync.ReiSlotLocking;
import java.util.List;
import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

public class SlotLockingHandler implements AutoTransferHandler {
    private final RecipeHelper recipeHelper;

    public SlotLockingHandler(RecipeHelper recipeHelper) {
        this.recipeHelper = recipeHelper;
    }

    @Override
    public @NotNull Result handle(@NotNull Context context) {
        if (!(context.getRecipe() instanceof MachineRecipeDisplay))
            return Result.createNotApplicable();
        if (!(context.getContainer() instanceof MachineScreenHandlers.Client))
            return Result.createNotApplicable();
        MachineRecipeDisplay display = (MachineRecipeDisplay) context.getRecipe();
        MachineScreenHandlers.Client handler = (MachineScreenHandlers.Client) context.getContainer();
        if (!canApply(handler, display))
            return Result.createNotApplicable();
        ReiSlotLocking.Client slotLocking = handler.getComponent(ReiSlotLocking.Client.class);
        if (slotLocking == null || !slotLocking.isLockingAllowed())
            return Result.createNotApplicable();
        if (context.isActuallyCrafting()) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(handler.syncId);
            buf.writeIdentifier(display.recipe.getId());
            ClientPlayNetworking.send(MachinePackets.C2S.REI_LOCK_SLOTS, buf);
        }
        return Result.createSuccessful().blocksFurtherHandling();
    }

    private boolean canApply(MachineScreenHandlers.Client handler, MachineRecipeDisplay display) {
        // Check if the block is in the worktables - it's a hack but it should work. :P
        String blockId = handler.guiParams.blockId;
        List<List<EntryStack>> workstations = recipeHelper.getWorkingStations(display.getRecipeCategory());
        for (List<EntryStack> workstationEntries : workstations) {
            for (EntryStack entry : workstationEntries) {
                Item item = entry.getItem();
                if (Registry.ITEM.getId(item).equals(new MIIdentifier(blockId))) {
                    return true;
                }
            }
        }
        return false;
    }
}
