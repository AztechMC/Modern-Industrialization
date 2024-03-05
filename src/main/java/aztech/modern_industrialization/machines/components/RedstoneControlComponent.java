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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.items.RedstoneControlModuleItem;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RedstoneControlComponent implements IComponent.ServerOnly, DropableComponent {

    public static final ResourceLocation ID = new MIIdentifier("redstone_control");

    private ItemStack controlModule = ItemStack.EMPTY;

    public boolean doAllowNormalOperation(MachineBlockEntity machine) {
        if (controlModule.isEmpty()) {
            return true;
        }

        return RedstoneControlModuleItem.isRequiresLowSignal(controlModule) != machine.hasRedstoneHighSignal();
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.put("redstoneModuleStack", controlModule.save(new CompoundTag()));
    }

    @Override
    public void readNbt(CompoundTag tag, boolean isUpgradingMachine) {
        controlModule = ItemStack.of(tag.getCompound("redstoneModuleStack"));
    }

    public InteractionResult onUse(MachineBlockEntity be, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (stackInHand.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (MIItem.REDSTONE_CONTROL_MODULE.is(stackInHand) && controlModule.isEmpty()) {
            controlModule = stackInHand.copy();
            controlModule.setCount(1);
            stackInHand.shrink(1);

            be.setChanged();
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public ItemStack getDrop() {
        return controlModule;
    }

    public void setStackServer(MachineBlockEntity be, ItemStack stack) {
        controlModule = stack;
        be.setChanged();
        be.sync();
    }
}
