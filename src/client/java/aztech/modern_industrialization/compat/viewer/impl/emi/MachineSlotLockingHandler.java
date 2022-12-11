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
package aztech.modern_industrialization.compat.viewer.impl.emi;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.MachinePackets;
import aztech.modern_industrialization.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.machines.gui.MachineMenuCommon;
import aztech.modern_industrialization.machines.guicomponents.ReiSlotLockingClient;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import dev.emi.emi.api.EmiFillAction;
import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.Slot;
import org.apache.commons.lang3.mutable.MutableBoolean;

class MachineSlotLockingHandler implements EmiRecipeHandler<MachineMenuCommon> {
    @Override
    public List<Slot> getInputSources(MachineMenuCommon menu) {
        return List.of();
    }

    @Override
    public List<Slot> getCraftingSlots(MachineMenuCommon menu) {
        return List.of();
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof ViewerCategoryEmi.ViewerRecipe r && r.recipe instanceof MachineRecipe;
    }

    @Override
    public boolean onlyDisplayWhenApplicable(EmiRecipe recipe) {
        return true;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiPlayerInventory inventory, AbstractContainerScreen<MachineMenuCommon> screen) {
        return transfer(recipe, screen, false);
    }

    @Override
    public boolean performFill(EmiRecipe recipe, AbstractContainerScreen<MachineMenuCommon> screen, EmiFillAction action, int amount) {
        return transfer(recipe, screen, true);
    }

    private boolean transfer(EmiRecipe recipe, AbstractContainerScreen<MachineMenuCommon> screen, boolean doTransfer) {
        var handler = screen.getMenu();
        if (!canApply(handler, ((ViewerCategoryEmi<?>.ViewerRecipe) recipe).getCategory()))
            return false;
        ReiSlotLockingClient slotLocking = ((MachineMenuClient) handler).getComponent(ReiSlotLockingClient.class);
        if (slotLocking == null || !slotLocking.isLockingAllowed())
            return false;
        if (doTransfer) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeInt(handler.containerId);
            buf.writeResourceLocation(recipe.getId());
            ClientPlayNetworking.send(MachinePackets.C2S.REI_LOCK_SLOTS, buf);

            Minecraft.getInstance().setScreen(screen);
        }
        return true;
    }

    private boolean canApply(MachineMenuCommon handler, ViewerCategoryEmi<?> category) {
        // Check if the block is in the worktables - it's a hack but it should work. :P
        String blockId = handler.guiParams.blockId;
        MutableBoolean hasWorkstation = new MutableBoolean(false);

        category.wrapped.buildWorkstations(items -> {
            for (var item : items) {
                if (Registry.ITEM.getKey(item.asItem()).equals(new MIIdentifier(blockId))) {
                    hasWorkstation.setTrue();
                }
            }
        });

        return hasWorkstation.booleanValue();
    }
}
