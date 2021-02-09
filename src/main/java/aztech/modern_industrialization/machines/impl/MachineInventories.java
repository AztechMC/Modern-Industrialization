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
package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

public class MachineInventories {
    public static MachineInventory clientOfBuf(PacketByteBuf buf) {
        List<ConfigurableItemStack> itemStacks = new ArrayList<>();
        List<ConfigurableFluidStack> fluidStacks = new ArrayList<>();
        MIInventory inventory = new MIInventory(itemStacks, fluidStacks);
        boolean hasOutput = buf.readBoolean();
        boolean[] autoExtract = new boolean[] { buf.readBoolean(), buf.readBoolean() };

        MachineInventory clientInv = new MachineInventory() {
            @Override
            public void setItemExtract(boolean extract) {
                autoExtract[0] = extract;
            }

            @Override
            public void setFluidExtract(boolean extract) {
                autoExtract[1] = extract;
            }

            @Override
            public boolean getItemExtract() {
                return autoExtract[0];
            }

            @Override
            public boolean getFluidExtract() {
                return autoExtract[1];
            }

            @Override
            public boolean hasOutput() {
                return hasOutput;
            }

            @Override
            public MIInventory getInventory() {
                return inventory;
            }

            @Override
            public void markDirty2() {
            }
        };

        int itemStackCnt = buf.readInt();
        while (itemStackCnt-- > 0)
            itemStacks.add(new ConfigurableItemStack());
        int fluidStackCnt = buf.readInt();
        while (fluidStackCnt-- > 0)
            fluidStacks.add(new ConfigurableFluidStack(0));

        inventory.readFromTag(buf.readCompoundTag());
        return clientInv;
    }

    public static void toBuf(PacketByteBuf buf, MachineInventory inventory) {
        buf.writeBoolean(inventory.hasOutput());
        buf.writeBoolean(inventory.getItemExtract());
        buf.writeBoolean(inventory.getFluidExtract());
        buf.writeInt(inventory.getInventory().itemStacks.size());
        buf.writeInt(inventory.getInventory().fluidStacks.size());
        CompoundTag tag = new CompoundTag();
        inventory.getInventory().writeToTag(tag);
        buf.writeCompoundTag(tag);
    }
}
