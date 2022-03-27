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
import aztech.modern_industrialization.items.ForgeTool;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import java.util.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ForgeHammerScreenHandler extends AbstractContainerMenu {

    private final DataSlot selectedRecipe;
    private final List<MachineRecipe> availableRecipes;

    private final Slot output;

    private final Slot tool;
    private final Slot input;
    private final ContainerLevelAccess context;
    private final Level world;
    private final Player player;

    private ItemStack inputStackCache = ItemStack.EMPTY, toolStackCache = ItemStack.EMPTY;

    public ForgeHammerScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ContainerLevelAccess.NULL);
    }

    public ForgeHammerScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(ModernIndustrialization.SCREEN_HANDLER_FORGE_HAMMER, syncId);
        this.context = context;
        this.selectedRecipe = DataSlot.standalone();
        this.availableRecipes = new ArrayList<>();
        this.world = playerInventory.player.level;
        this.player = playerInventory.player;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, i * 9 + j + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 58 + 84));
        }

        this.input = new Slot(new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                ForgeHammerScreenHandler.this.slotsChanged(this);
            }
        }, 0, 34, 33);

        this.tool = new Slot(new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                ForgeHammerScreenHandler.this.slotsChanged(this);
            }
        }, 0, 8, 33) {

            public boolean mayPlace(ItemStack stack) {
                return stack.is(ForgeTool.TAG);
            }
        };

        this.output = new Slot(new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                ForgeHammerScreenHandler.this.slotsChanged(this);
            }
        }, 0, 143, 32) {
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                ForgeHammerScreenHandler.this.onCraft();
            }
        };

        this.addSlot(input);
        this.addSlot(tool);
        this.addSlot(output);
        this.addDataSlot(selectedRecipe);
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

    public void slotsChanged(Container inventory) {
        if (!ItemStack.matches(this.inputStackCache, input.getItem()) || !ItemStack.matches(this.toolStackCache, tool.getItem())) {
            updateStatus();
        }

        super.slotsChanged(inventory);

    }

    public void updateStatus() {

        this.inputStackCache = input.getItem().copy();
        this.toolStackCache = tool.getItem().copy();

        MachineRecipe old = isInBounds(selectedRecipe.get()) ? availableRecipes.get(selectedRecipe.get()) : null;

        this.availableRecipes.clear();
        this.selectedRecipe.set(-1);
        this.output.set(ItemStack.EMPTY);

        if (!input.getItem().isEmpty()) {

            Map<ResourceLocation, MachineRecipe> recipeMap = new HashMap<>();

            for (MachineRecipe recipe : MIMachineRecipeTypes.FORGE_HAMMER.getRecipes(this.world)) {

                MachineRecipe.ItemInput recipeInput = recipe.itemInputs.get(0);

                if (recipeInput.matches(input.getItem()) && recipeInput.amount <= input.getItem().getCount()) {
                    ResourceLocation idOutput = Registry.ITEM.getKey(recipe.itemOutputs.get(0).item);
                    if ((recipe.eu != 0) && (!tool.getItem().isEmpty())) {
                        recipeMap.put(idOutput, recipe);
                    } else if (recipe.eu == 0 && !recipeMap.containsKey(idOutput)) {
                        recipeMap.put(idOutput, recipe);
                    }
                }
            }

            availableRecipes.addAll(recipeMap.values());

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
            if (current.eu == 0 || (!tool.getItem().isEmpty() && tool.getItem().getDamageValue() < tool.getItem().getMaxDamage())) {
                this.output.set(current.getResultItem());
            } else {
                this.output.set(ItemStack.EMPTY);
            }
        } else {
            this.output.set(ItemStack.EMPTY);
        }

        this.broadcastChanges();
    }

    public boolean clickMenuButton(Player player, int id) {
        if (this.isInBounds(id)) {
            this.selectedRecipe.set(id);
            this.populateResult();
        }
        return true;
    }

    private void onCraft() {

        MachineRecipe current = this.availableRecipes.get(this.selectedRecipe.get());
        this.input.getItem().shrink(current.itemInputs.get(0).amount);
        if (!tool.getItem().isEmpty()) {
            if (!world.isClientSide()) {
                tool.getItem().hurt(current.eu, world.getRandom(), (ServerPlayer) this.player);
            }
            if (tool.getItem().getDamageValue() >= tool.getItem().getMaxDamage()) {
                tool.set(ItemStack.EMPTY);

                context.execute((world, pos) -> {
                    world.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                });
            }

        } else if (current.eu > 0) {
            throw new IllegalStateException("Forge Hammer Exception : Tool crafting without a tool");
        }

        this.updateStatus();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            Item item = itemStack2.getItem();
            itemStack = itemStack2.copy();
            if (index == 38) {
                item.onCraftedBy(itemStack2, player.level, player);
                if (!this.moveItemStackTo(itemStack2, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemStack2, itemStack);
            } else if (index == 37 || index == 36) {
                if (!this.moveItemStackTo(itemStack2, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 36) {
                if (!this.moveItemStackTo(itemStack2, 36, 38, true)) {
                    if (index < 27) {
                        if (!this.moveItemStackTo(itemStack2, 27, 36, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }

            slot.setChanged();
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
            this.broadcastChanges();
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public void removed(Player player) {
        super.removed(player);
        this.context.execute((world, blockPos) -> {
            this.clearContainer(player, this.input.container);
            this.clearContainer(player, this.tool.container);
        });
    }

}
