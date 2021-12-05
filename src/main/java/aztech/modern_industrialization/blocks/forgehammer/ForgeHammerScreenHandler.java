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
package aztech.modern_industrialization.blocks.forgehammer;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class ForgeHammerScreenHandler extends ScreenHandler {

    private final Property selectedRecipe;
    private final List<MachineRecipe> availableRecipes;

    private final Slot output;

    private final Slot tool;
    private final Slot input;
    private final ScreenHandlerContext context;
    private final World world;

    private ItemStack inputStackCache = ItemStack.EMPTY, toolStackCache = ItemStack.EMPTY;

    public ForgeHammerScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public ForgeHammerScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModernIndustrialization.SCREEN_HANDLER_FORGE_HAMMER, syncId);
        this.context = context;
        this.selectedRecipe = Property.create();
        this.availableRecipes = new ArrayList<>();
        this.world = playerInventory.player.world;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, i * 9 + j + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 58 + 84));
        }

        this.input = new Slot(new SimpleInventory(1) {
            public void markDirty() {
                super.markDirty();
                ForgeHammerScreenHandler.this.onContentChanged(this);
            }
        }, 0, 34, 33);

        this.tool = new Slot(new SimpleInventory(1) {
            public void markDirty() {
                super.markDirty();
                ForgeHammerScreenHandler.this.onContentChanged(this);
            }
        }, 0, 8, 33);

        this.output = new Slot(new SimpleInventory(1) {
            public void markDirty() {
                super.markDirty();
                ForgeHammerScreenHandler.this.onContentChanged(this);
            }
        }, 0, 143, 32) {
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                ForgeHammerScreenHandler.this.onCraft();
            }
        };

        this.addSlot(input);
        this.addSlot(tool);
        this.addSlot(output);
        this.addProperty(selectedRecipe);
    }

    public int getSelectedRecipe() {
        return selectedRecipe.get();
    }

    public List<MachineRecipe> getAvailableRecipes() {
        return availableRecipes;
    }

    public int getAvailableRecipeCount() {
        return availableRecipes.size();
    }

    private boolean isInBounds(int id) {
        return id >= 0 && id < this.availableRecipes.size();
    }

    public void onContentChanged(Inventory inventory) {
        if (!ItemStack.areEqual(this.inputStackCache, input.getStack()) || !ItemStack.areEqual(this.toolStackCache, tool.getStack())) {
            updateStatus();
        }

        super.onContentChanged(inventory);

    }

    public void updateStatus() {

        this.inputStackCache = input.getStack().copy();
        this.toolStackCache = tool.getStack().copy();

        MachineRecipe old = isInBounds(selectedRecipe.get()) ? availableRecipes.get(selectedRecipe.get()) : null;

        this.availableRecipes.clear();
        this.selectedRecipe.set(-1);
        this.output.setStack(ItemStack.EMPTY);

        if (!input.getStack().isEmpty()) {

            for (MachineRecipe recipe : MIMachineRecipeTypes.FORGE_HAMMER.getRecipes(this.world)) {

                MachineRecipe.ItemInput recipeInput = recipe.itemInputs.get(0);

                if ((recipe.eu == 0) == (tool.getStack().isEmpty())) {
                    if (recipeInput.matches(input.getStack()) && recipeInput.amount <= input.getStack().getCount()) {
                        availableRecipes.add(recipe);
                    }
                }
            }

            availableRecipes.sort(Comparator.comparing(MachineRecipe::getId));

            for (int i = 0; i < availableRecipes.size(); i++) {
                if (old == availableRecipes.get(i)) {
                    this.selectedRecipe.set(i);
                    break;
                }
            }
            populateResult();
        }
    }

    void populateResult() {
        if (!this.availableRecipes.isEmpty() && this.isInBounds(this.selectedRecipe.get())) {
            MachineRecipe current = this.availableRecipes.get(getSelectedRecipe());
            this.output.setStack(current.getOutput());
        } else {
            this.output.setStack(ItemStack.EMPTY);
        }

        this.sendContentUpdates();
    }

    public boolean onButtonClick(PlayerEntity player, int id) {
        if (this.isInBounds(id)) {
            this.selectedRecipe.set(id);
            this.populateResult();
        }
        return true;
    }

    private void onCraft() {
        MachineRecipe current = this.availableRecipes.get(this.selectedRecipe.get());
        this.input.getStack().decrement(current.itemInputs.get(0).amount);
        this.updateStatus();

        // TODO : ADD TOOLS USE
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {

        // TODO : Rework

        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index == 37) {
                if (!this.insertItem(itemStack2, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(itemStack2, itemStack);
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

            slot.onTakeItem(player, itemStack2);
            if (index == 37) {
                player.dropItem(itemStack2, false);
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
            this.dropInventory(player, this.input.inventory);
            this.dropInventory(player, this.tool.inventory);
        });
    }

}
