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
package aztech.modern_industrialization.blocks.toolstation;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.api.item.modular_tools.CasingRegistry;
import aztech.modern_industrialization.api.item.modular_tools.ComponentTier;
import aztech.modern_industrialization.api.item.modular_tools.EnergyConverterRegistry;
import aztech.modern_industrialization.api.item.modular_tools.EnergyStorageRegistry;
import aztech.modern_industrialization.api.item.modular_tools.HeadRegistry;
import aztech.modern_industrialization.api.item.modular_tools.ModuleRegistry;
import aztech.modern_industrialization.items.modulartools.ModularToolItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ToolStationScreenHandler extends AbstractContainerMenu {
    public final Slot tool;
    public final SingleItemToggelableSlot energyConverter;
    public final SingleItemToggelableSlot energyStorage;
    public final SingleItemToggelableSlot head;
    public final List<ToggleableSlot> addons;
    private final DataSlot numAddonSlots;
    private final DataSlot componentsEnabled;

    private final ContainerLevelAccess context;
    private final Level world;
    private final Player player;
    private boolean isUpdating;

    public ToolStationScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ContainerLevelAccess.NULL);
    }

    public ToolStationScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(ModernIndustrialization.SCREEN_HANDLER_TOOL_STATION, syncId);
        this.context = context;
        this.world = playerInventory.player.level();
        this.player = playerInventory.player;
        this.isUpdating = false;

        this.tool = new SingleItemSlot(new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                ToolStationScreenHandler.this.slotsChanged(this);
            }
        }, 0, 17, 33) {
            public boolean mayPlace(ItemStack stack) {
                return CasingRegistry.getProperties(stack.getItem()) != null;
            }
        };
        this.energyConverter = new SingleItemToggelableSlot(new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                ToolStationScreenHandler.this.slotsChanged(this);
            }
        }, 0, 44, 24) {
            public boolean mayPlace(ItemStack stack) {
                var properties = EnergyConverterRegistry.getProperties(stack.getItem());
                return isActive() && properties != null
                        && ComponentTier.canUse(getMaxComponentTier(), properties.tier());
            }
        };
        this.energyStorage = new SingleItemToggelableSlot(new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                ToolStationScreenHandler.this.slotsChanged(this);
            }
        }, 0, 62, 24) {
            public boolean mayPlace(ItemStack stack) {
                var properties = EnergyStorageRegistry.getProperties(stack.getItem());
                return isActive() && properties != null
                        && ComponentTier.canUse(getMaxComponentTier(), properties.tier());
            }
        };
        this.head = new SingleItemToggelableSlot(new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                ToolStationScreenHandler.this.slotsChanged(this);
            }
        }, 0, 80, 24) {
            public boolean mayPlace(ItemStack stack) {
                var properties = HeadRegistry.getProperties(stack.getItem());
                return isActive() && properties != null
                        && ComponentTier.canUse(getMaxComponentTier(), properties.tier());
            }
        };
        this.addons = new ArrayList<>();
        for (int idx = 0; idx < 5; ++idx) {
            this.addons.add(new ToggleableSlot(new SimpleContainer(1) {
                public void setChanged() {
                    super.setChanged();
                    ToolStationScreenHandler.this.slotsChanged(this);
                }
            }, 0, 44 + idx * 18, 42) {
                public boolean mayPlace(ItemStack stack) {
                    for (var addon : ToolStationScreenHandler.this.addons) {
                        if (addon != this && addon.hasItem() && addon.getItem().getItem() == stack.getItem()) {
                            return false;
                        }
                    }
                    return isActive() && ModuleRegistry.getProperties(stack.getItem()) != null;
                }
            });
        }
        this.numAddonSlots = DataSlot.standalone();
        this.numAddonSlots.set(0);
        this.componentsEnabled = DataSlot.standalone();
        this.componentsEnabled.set(0);

        this.addSlot(tool);
        this.addSlot(energyConverter);
        this.addSlot(energyStorage);
        this.addSlot(head);
        for (Slot slot : this.addons) {
            this.addSlot(slot);
        }
        this.addDataSlot(numAddonSlots);
        this.addDataSlot(componentsEnabled);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, i * 9 + j + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 58 + 84));
        }
    }

    private ComponentTier getMaxComponentTier() {
        if (!tool.hasItem()) {
            return null;
        }
        return CasingRegistry.getProperties(tool.getItem().getItem()).maxComponentTier();

    }

    public void slotsChanged(Container inventory) {
        if (isUpdating) {
            return;
        } else {
            isUpdating = true;
        }

        try {
            if (inventory == tool.container) {
                if (tool.hasItem()) {
                    ItemStack toolStack = tool.getItem();
                    int casingAddonSlots = CasingRegistry.getProperties(toolStack.getItem()).moduleSlots();

                    numAddonSlots.set(casingAddonSlots);
                    componentsEnabled.set(1);

                    energyConverter.container.clearContent();
                    energyConverter.setActive(true);
                    energyStorage.container.clearContent();
                    energyStorage.setActive(true);
                    head.container.clearContent();
                    head.setActive(true);
                    for (ToggleableSlot slot : this.addons) {
                        slot.container.clearContent();
                    }
                    for (int idx = 0; idx < casingAddonSlots; ++idx) {
                        this.addons.get(idx).setActive(true);
                    }

                    if (toolStack.hasTag()) {
                        CompoundTag tag = toolStack.getTag();

                        String energyConverterId = tag.getString("energyConverter");
                        if (!energyConverterId.equals("")) {
                            energyConverter.set(new ItemStack(BuiltInRegistries.ITEM
                                    .get(new ResourceLocation(energyConverterId))));
                        }

                        String energyStorageId = tag.getString("energyStorage");
                        if (!energyStorageId.equals("")) {
                            energyStorage.set(new ItemStack(BuiltInRegistries.ITEM
                                    .get(new ResourceLocation(energyStorageId))));
                        }

                        String headId = tag.getString("head");
                        if (!headId.equals("")) {
                            head.set(new ItemStack(BuiltInRegistries.ITEM
                                    .get(new ResourceLocation(headId))));
                        }

                        CompoundTag addonsTag = tag.getCompound("addons");
                        for (int idx = 0; idx < casingAddonSlots; ++idx) {
                            if (addonsTag.contains(Integer.toString(idx))) {
                                CompoundTag addonTag = addonsTag.getCompound(Integer.toString(idx));
                                addons.get(idx)
                                        .set(new ItemStack(
                                                BuiltInRegistries.ITEM
                                                        .get(new ResourceLocation(addonTag.getString("id"))),
                                                addonTag.getInt("amount")));
                            }
                        }
                    }
                } else {
                    numAddonSlots.set(0);
                    componentsEnabled.set(0);

                    energyConverter.container.clearContent();
                    energyConverter.setActive(false);
                    energyStorage.container.clearContent();
                    energyStorage.setActive(false);
                    head.container.clearContent();
                    head.setActive(false);
                    for (ToggleableSlot slot : this.addons) {
                        slot.container.clearContent();
                        slot.setActive(false);
                    }
                }
            } else {
                ItemStack toolStack = tool.getItem();
                CompoundTag tag = toolStack.getOrCreateTag();
                if (inventory == energyConverter.container) {
                    if (energyConverter.hasItem()) {
                        tag.putString("energyConverter",
                                BuiltInRegistries.ITEM.getKey(energyConverter.getItem().getItem()).toString());
                    } else {
                        tag.remove("energyConverter");
                    }
                } else if (inventory == energyStorage.container) {
                    if (energyStorage.hasItem()) {
                        tag.putString("energyStorage",
                                BuiltInRegistries.ITEM.getKey(energyStorage.getItem().getItem()).toString());
                    } else {
                        tag.remove("energyStorage");
                    }
                } else if (inventory == head.container) {
                    if (head.hasItem()) {
                        tag.putString("head",
                                BuiltInRegistries.ITEM.getKey(head.getItem().getItem()).toString());
                    } else {
                        tag.remove("head");
                    }
                } else {
                    // must be one of the addon slots
                    for (int idx = 0; idx < addons.size(); ++idx) {
                        Container addonSlot = addons.get(idx).container;
                        if (inventory == addonSlot) {
                            if (!tag.contains("addons")) {
                                tag.put("addons", new CompoundTag());
                            }
                            CompoundTag addonsTag = tag.getCompound("addons");
                            if (addonSlot.isEmpty()) {
                                addonsTag.remove(Integer.toString(idx));
                            } else {
                                CompoundTag addonTag = new CompoundTag();
                                addonTag.putString("id",
                                        BuiltInRegistries.ITEM.getKey(addonSlot.getItem(0).getItem()).toString());
                                addonTag.putInt("amount", addonSlot.getItem(0).getCount());
                                addonsTag.put(Integer.toString(idx), addonTag);
                            }
                        }
                    }
                }

                ModularToolItem.rebuildTool(toolStack);
            }
            super.slotsChanged(inventory);
        } finally {
            isUpdating = false;
        }
    }

    public int getNumAddonSlots() {
        return numAddonSlots.get();
    }

    public boolean isComponentsEnabled() {
        return componentsEnabled.get() != 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack inSlot = slot.getItem();
            moved = inSlot.copy();
            if (index >= 0 && index <= 8) {
                if (!this.moveItemStackTo(inSlot, 9, 9 + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // try to put it in an input slot
                boolean inserted = false;
                for (int idx = 0; idx < 9; ++idx) {
                    if (slots.get(idx).mayPlace(inSlot) && this.moveItemStackTo(inSlot, idx, idx + 1, false)) {
                        inserted = true;
                    }
                }
                if (!inserted) {
                    // move between player inventory and player hotbar
                    if (index < 9 + 27) {
                        if (!this.moveItemStackTo(inSlot, 9 + 27, 9 + 27 + 9, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(inSlot, 9, 9 + 27, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
            if (inSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (inSlot.getCount() == moved.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, inSlot);
        }
        return moved;
    }

    public void removed(Player player) {
        super.removed(player);
        this.context.execute((world, blockPos) -> {
            this.clearContainer(player, this.tool.container);
        });
    }

    private static class SingleItemSlot extends Slot {
        public SingleItemSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        public int getMaxStackSize() {
            return 1;
        }
    }

    private static class ToggleableSlot extends Slot {
        private boolean active;

        public ToggleableSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);

            this.active = false;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    private static class SingleItemToggelableSlot extends ToggleableSlot {
        public SingleItemToggelableSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        public int getMaxStackSize() {
            return 1;
        }
    }
}
