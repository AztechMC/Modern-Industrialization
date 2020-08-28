package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableInventory;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class MachineInventories {
    public static MachineInventory clientOfBuf(PacketByteBuf buf) {
        List<ConfigurableItemStack> itemStacks = new ArrayList<>();
        List<ConfigurableFluidStack> fluidStacks = new ArrayList<>();
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
            public List<ConfigurableItemStack> getItemStacks() {
                return itemStacks;
            }

            @Override
            public List<ConfigurableFluidStack> getFluidStacks() {
                return fluidStacks;
            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void markDirty() {

            }
        };


        int itemStackCnt = buf.readInt();
        while(itemStackCnt --> 0) itemStacks.add(new ConfigurableItemStack());
        int fluidStackCnt = buf.readInt();
        while(fluidStackCnt --> 0) fluidStacks.add(new ConfigurableFluidStack(clientInv, 0));

        clientInv.readFromTag(buf.readCompoundTag());
        return clientInv;
    }

    public static void toBuf(PacketByteBuf buf, MachineInventory inventory) {
        buf.writeBoolean(inventory.hasOutput());
        buf.writeBoolean(inventory.getItemExtract());
        buf.writeBoolean(inventory.getFluidExtract());
        buf.writeInt(inventory.getItemStacks().size());
        buf.writeInt(inventory.getFluidStacks().size());
        CompoundTag tag = new CompoundTag();
        inventory.writeToTag(tag);
        buf.writeCompoundTag(tag);
    }
}
