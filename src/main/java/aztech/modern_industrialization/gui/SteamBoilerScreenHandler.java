package aztech.modern_industrialization.gui;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.blockentity.SteamBoilerBlockEntity;
import aztech.modern_industrialization.fluid.gui.FluidContainerScreenHandler;
import aztech.modern_industrialization.fluid.gui.FluidSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * Screen handler for the steam boiler.
 */
public class SteamBoilerScreenHandler extends FluidContainerScreenHandler {
    private Inventory inventory;

    public SteamBoilerScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(3)); // TODO: don't hardcode constant
    }

    public SteamBoilerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModernIndustrialization.SCREEN_HANDLER_TYPE_STEAM_BOILER, syncId, 3);
        this.inventory = inventory;

        this.addSlot(new FuelSlot(inventory, 0, 45, 32));
        this.addSlot(new WaterSlot(inventory, 1, 81, 18));
        this.addSlot(new SteamSlot(inventory, 2, 105, 42));

        // Player inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, i * 9 + j + 9, 8 + j * 18, 84 + i * 18));
            }
        }


        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private static class FuelSlot extends Slot {
        public FuelSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return SteamBoilerBlockEntity.canInsertItem(stack.getItem());
        }
    }

    private static class WaterSlot extends FluidSlot {
        public WaterSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsertFluid(Fluid fluid) {
            return fluid == Fluids.WATER;
        }
    }

    private static class SteamSlot extends FluidSlot {
        public SteamSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsertFluid(Fluid fluid) {
            return false;
        }

        @Override
        public boolean canExtractFluid(Fluid fluid) {
            return fluid == ModernIndustrialization.FLUID_STEAM;
        }
    }
}
