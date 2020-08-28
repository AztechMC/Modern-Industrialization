package aztech.modern_industrialization.blocks.forgehammer;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.material.MIMaterial;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

import java.util.HashMap;

public class ForgeHammerScreenHandler extends ScreenHandler {


    private static final HashMap<Item, Item> recipesHammer;
    private static final HashMap<Item, Item> recipesSaw;

    static{
        recipesHammer = new HashMap<>();
        recipesSaw = new HashMap<>();

        for(MIMaterial material : new MIMaterial[] {MIMaterial.iron, MIMaterial.bronze, MIMaterial.tin, MIMaterial.copper}){
            recipesHammer.put(material.getItem("double_ingot"), material.getItem("plate"));
            recipesHammer.put(material.getItem("plate"), material.getItem("curved_plate"));
            recipesHammer.put(material.getItem("nugget"), material.getItem("small_dust"));

            recipesSaw.put(material.getItem("large_plate"), material.getItem("gear"));
            recipesSaw.put(material.getItem("ingot"), material.getItem("rod"));
            recipesSaw.put(material.getItem("rod"), material.getItem("bolt"));
            recipesSaw.put(material.getItem("pipe"), material.getItem("ring"));
        }
        
    }

    private final CraftingResultInventory output = new CraftingResultInventory();
    private final Inventory input = new SimpleInventory(2) {
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
        if(!input.getStack(0).isEmpty()) {
            HashMap<Item, Item> recipes = isHammer ? recipesHammer : recipesSaw;
            Item inputItem = input.getStack(0).getItem();
            if(recipes.containsKey(inputItem)){
                this.output.setStack(0, new ItemStack(recipes.get(inputItem), 1));
                return;
            }
        }
        this.output.setStack(0, ItemStack.EMPTY);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        Slot slot = slots.get(index);
        if(slot != null && slot.hasStack()){
            ItemStack itemStack = slot.getStack();
            if(index < 36){ // click in inventory
                insertItem(itemStack, 36, 37, false);
                updateStatus();
            }else if(index == 36){ // click in input
                insertItem(itemStack, 0, 36, false);
                updateStatus();
            }else{ // click in output
                while(insertItem(itemStack, 0, 36, false)){
                    input.getStack(0).decrement(1);
                    updateStatus();
                    itemStack = slot.getStack();
                }
            }

        }
        return ItemStack.EMPTY;
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
