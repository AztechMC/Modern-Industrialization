package aztech.modern_industrialization.blocks.forgehammer;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class ForgeHammerScreenHandler extends ScreenHandler {


    // TODO : create custom recipe json
    public static final MachineRecipeType RECIPE_HAMMER = new MachineRecipeType(new MIIdentifier("forge_hammer_hammer")).withItemInputs().withItemOutputs();
    public static final MachineRecipeType RECIPE_SAW = new MachineRecipeType(new MIIdentifier("forge_hammer_saw")).withItemInputs().withItemOutputs();

    public static void setupRecipes() {
        registerRecipe("forge_hammer_hammer", RECIPE_HAMMER);
        registerRecipe("forge_hammer_saw", RECIPE_SAW);

    }

    private static void registerRecipe(String name, MachineRecipeType type) {
        Registry.register(Registry.RECIPE_TYPE, new MIIdentifier(name), type);
        Registry.register(Registry.RECIPE_SERIALIZER, new MIIdentifier(name), type);
    }



    private final Inventory output = new SimpleInventory(1) {
        public void markDirty() {
            super.markDirty();
            ForgeHammerScreenHandler.this.onContentChanged(this);
        }
    };
    private final Inventory input = new SimpleInventory(1) {
        public void markDirty() {
            super.markDirty();
            ForgeHammerScreenHandler.this.onContentChanged(this);
        }
    };
    private final ScreenHandlerContext context;
    private final PlayerInventory playerInventory;

    private boolean isHammer;

    public ForgeHammerScreenHandler(int syncId, PlayerInventory playerInventory){
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public ForgeHammerScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModernIndustrialization.SCREEN_HANDLER_FORGE_HAMMER, syncId);
        this.playerInventory = playerInventory;
        this.context = context;
        this.isHammer = true;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, i * 9 + j + 9,
                        8 + j * 18, 84 + i * 18));
            }
        }

        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j,
                    8 + j * 18, 58 + 84));
        }

        this.addSlot(new Slot(this.input, 0, 47, 47));
        this.addSlot(new Slot(this.output, 0, 134, 47) {
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
                input.getStack(0).decrement(1);
                ItemStack current = getStack();
                updateStatus();
                return current;
            }
        });
    }


    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (inventory == this.input) {
            updateStatus();
        }
    }

    public void updateStatus(){
        this.context.run((world, blockPos) -> {
            updateStatus(this.syncId, world);
        });
    }

    public void updateStatus(int syncId, World world){
        if (!world.isClient) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)playerInventory.player;
            ItemStack outputStack = ItemStack.EMPTY;
            if (!input.getStack(0).isEmpty()) {

                // absolutely nothing could go wrong
                for (MachineRecipe recipe : (isHammer ? RECIPE_HAMMER : RECIPE_SAW).getRecipes((ServerWorld) world)) {
                    Item inputItem = input.getStack(0).getItem();
                    if (recipe.itemInputs.get(0).item == inputItem) {
                        MachineRecipe.ItemOutput output = recipe.itemOutputs.get(0);
                        outputStack = new ItemStack(output.item, output.amount);
                        break;
                    }
                }
            }
            this.output.setStack(0, outputStack);
            serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, 37, outputStack));
        }
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index == 37) {
                if (!this.insertItem(itemStack2, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onStackChanged(itemStack2, itemStack);
            } else if (index >= 0 && index < 36) { // inventory
                if (!this.insertItem(itemStack2, 36, 37, false)) {
                    if (index < 27) { // inside inventory
                        if (!this.insertItem(itemStack2, 27, 36, false)) { // toolbar
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.insertItem(itemStack2, 0, 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.insertItem(itemStack2, 0, 36, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            ItemStack itemStack3 = slot.onTakeItem(player, itemStack2);
            if (index == 37) {
                player.dropItem(itemStack3, false);
            }
        }

        return itemStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public void close(PlayerEntity player) {
        super.close(player);
        this.context.run((world, blockPos) -> {
            this.dropInventory(player, world, this.input);
        });
    }

    public boolean isHammer() {
        return isHammer;
    }

    public void setHammer(boolean hammer) {
        this.isHammer = hammer;
        updateStatus();
    }

}
