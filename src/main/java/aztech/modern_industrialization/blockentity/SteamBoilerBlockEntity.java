package aztech.modern_industrialization.blockentity;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.fluid.FluidStackItem;
import aztech.modern_industrialization.fluid.FluidUnit;
import aztech.modern_industrialization.gui.SteamBoilerScreenHandler;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Direction;

/**
 * Steam boiler BlockEntity.
 * Slots: 0 is the burnable fuel, 1 is the input water.
 */
public class SteamBoilerBlockEntity extends AbstractMachineBlockEntity implements SidedInventory {

    public SteamBoilerBlockEntity() {
        super(ModernIndustrialization.BLOCK_ENTITY_STEAM_BOILER, 3, Direction.NORTH);

        ItemStack waterFluidStack = FluidStackItem.getEmptyStack();
        FluidStackItem.setCapacity(waterFluidStack, 4 * FluidUnit.DROPS_PER_BUCKET);
        this.inventory.set(1, waterFluidStack);

        ItemStack steamFluidStack = FluidStackItem.getEmptyStack();
        FluidStackItem.setCapacity(steamFluidStack, 4 * FluidUnit.DROPS_PER_BUCKET);
        this.inventory.set(2, steamFluidStack);
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText("machine.steam_boiler");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new SteamBoilerScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return slot == 0 && canInsertItem(stack.getItem());
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 0;
    }

    public static boolean canInsertItem(ItemConvertible item) {
        return FuelRegistryImpl.INSTANCE.get(item) != null;
    }
}
