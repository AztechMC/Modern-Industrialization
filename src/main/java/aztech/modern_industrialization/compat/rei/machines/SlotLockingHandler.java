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
import aztech.modern_industrialization.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.machines.guicomponents.ReiSlotLocking;
import java.util.List;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotLockingHandler implements TransferHandler {
    @Override
    public @NotNull Result handle(@NotNull Context context) {
        if (!(context.getDisplay() instanceof MachineRecipeDisplay))
            return Result.createNotApplicable();
        if (!(context.getMenu() instanceof MachineMenuClient))
            return Result.createNotApplicable();
        MachineRecipeDisplay display = (MachineRecipeDisplay) context.getDisplay();
        MachineMenuClient handler = (MachineMenuClient) context.getMenu();
        if (!canApply(handler, display))
            return Result.createNotApplicable();
        ReiSlotLocking.Client slotLocking = handler.getComponent(ReiSlotLocking.Client.class);
        if (slotLocking == null || !slotLocking.isLockingAllowed())
            return Result.createNotApplicable();
        if (context.isActuallyCrafting()) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeInt(handler.containerId);
            buf.writeResourceLocation(display.recipe.getId());
            ClientPlayNetworking.send(MachinePackets.C2S.REI_LOCK_SLOTS, buf);
        }
        return Result.createSuccessful().blocksFurtherHandling();
    }

    private boolean canApply(MachineMenuClient handler, MachineRecipeDisplay display) {
        // Check if the block is in the worktables - it's a hack but it should work. :P
        String blockId = handler.guiParams.blockId;
        List<EntryIngredient> workstations = CategoryRegistry.getInstance().get(display.getCategoryIdentifier()).getWorkstations();
        for (EntryIngredient workstationEntries : workstations) {
            for (EntryStack<?> entry : workstationEntries) {
                Item item = entry.<ItemStack>cast().getValue().getItem();
                if (Registry.ITEM.getKey(item).equals(new MIIdentifier(blockId))) {
                    return true;
                }
            }
        }
        return false;
    }
}
