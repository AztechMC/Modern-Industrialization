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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.items.RedstoneControlModuleItem;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.MachineComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class RedstoneControlComponent implements MachineComponent.ServerOnly, DropableComponent {
    public static final Identifier ID = MI.id("redstone_control");

    private ItemStack controlModule = ItemStack.EMPTY;

    public boolean doAllowNormalOperation(MachineBlockEntity machine) {
        if (controlModule.isEmpty()) {
            return true;
        }

        return RedstoneControlModuleItem.isRequiresLowSignal(controlModule) != machine.hasRedstoneHighSignal();
    }

    @Override
    public void writeNbt(ValueOutput output) {
        output.store("redstoneModuleStack", ItemStack.OPTIONAL_CODEC, controlModule);
    }

    @Override
    public void readNbt(ValueInput input, boolean isUpgradingMachine) {
        controlModule = input.read("redstoneModuleStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    public InteractionResult onUse(MachineBlockEntity be, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (stackInHand.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (MIItem.REDSTONE_CONTROL_MODULE.is(stackInHand) && controlModule.isEmpty()) {
            controlModule = stackInHand.copyWithCount(1);
            stackInHand.consume(1, player);

            be.setChanged();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
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
