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
package aztech.modern_industrialization.compat.viewer.impl.jei;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.MachinePackets;
import aztech.modern_industrialization.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.machines.guicomponents.ReiSlotLockingClient;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import java.util.Optional;
import java.util.function.Supplier;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IRecipeCatalystLookup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.runtime.IJeiRuntime;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

class MachineSlotLockingHandler implements IRecipeTransferHandler<MachineMenuClient, MachineRecipe> {
    private final Supplier<IJeiRuntime> runtimeSupplier;
    private final mezz.jei.api.recipe.RecipeType<MachineRecipe> type;
    private final IRecipeTransferHandlerHelper helper;

    public MachineSlotLockingHandler(IRecipeTransferHandlerHelper helper, Supplier<IJeiRuntime> runtimeSupplier, RecipeType<MachineRecipe> type) {
        this.helper = helper;
        this.runtimeSupplier = runtimeSupplier;
        this.type = type;
    }

    private IRecipeCatalystLookup getLookup() {
        var runtime = runtimeSupplier.get();
        if (runtime != null) {
            return runtime.getRecipeManager().createRecipeCatalystLookup(type);
        }
        return null;
    }

    @Override
    public Class<? extends MachineMenuClient> getContainerClass() {
        return MachineMenuClient.class;
    }

    @Override
    public Optional<MenuType<MachineMenuClient>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<MachineRecipe> getRecipeType() {
        return type;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(MachineMenuClient menu, MachineRecipe recipe, IRecipeSlotsView recipeSlots, Player player,
            boolean maxTransfer, boolean doTransfer) {
        if (!canApply(menu))
            return helper.createInternalError();
        ReiSlotLockingClient slotLocking = menu.getComponent(ReiSlotLockingClient.class);
        if (slotLocking == null || !slotLocking.isLockingAllowed())
            return helper.createInternalError();
        if (doTransfer) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeInt(menu.containerId);
            buf.writeResourceLocation(recipe.getId());
            ClientPlayNetworking.send(MachinePackets.C2S.REI_LOCK_SLOTS, buf);
        }
        return null;
    }

    private boolean canApply(MachineMenuClient handler) {
        // Check if the block is in the worktables - it's a hack but it should work. :P
        String blockId = handler.guiParams.blockId;

        var lookup = getLookup();
        if (lookup == null) {
            return false;
        }

        var item = BuiltInRegistries.ITEM.get(new MIIdentifier(blockId));
        return lookup.getItemStack().anyMatch(is -> is.is(item));
    }
}
