package aztech.modern_industrialization.inventory;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class ConfigurableInventories {
    public static ConfigurableInventory clientOfBuf(PacketByteBuf buf) {
        List<ConfigurableItemStack> itemStacks = new ArrayList<>();
        List<ConfigurableFluidStack> fluidStacks = new ArrayList<>();

        ConfigurableInventory clientInv = new ConfigurableInventory() {
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
            public boolean providesFluidExtractionForce(Direction direction, Fluid fluid) {
                return false;
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

    public static void toBuf(PacketByteBuf buf, ConfigurableInventory inventory) {
        buf.writeInt(inventory.getItemStacks().size());
        buf.writeInt(inventory.getFluidStacks().size());
        CompoundTag tag = new CompoundTag();
        inventory.writeToTag(tag);
        buf.writeCompoundTag(tag);
    }
}
