package aztech.modern_industrialization.gui;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.fluid.gui.FluidContainerScreenHandler;
import aztech.modern_industrialization.fluid.gui.FluidSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.slot.Slot;

/**
 * Screen handler for the steam boiler.
 */
public class SteamBoilerScreenHandler extends FluidContainerScreenHandler {
    private Inventory inventory;

    public SteamBoilerScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(2)); // TODO: don't hardcode constant
    }

    public SteamBoilerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModernIndustrialization.SCREEN_HANDLER_TYPE_STEAM_BOILER, syncId);
        this.inventory = inventory;

        this.addSlot(new Slot(inventory, 0, 68, 32));
        this.addSlot(new FluidSlot(inventory, 1, 45, 32));

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
}
