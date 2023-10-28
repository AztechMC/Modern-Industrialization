/*
 * MIT License
 *
 * Copyright (c) 2023 Justin Hu
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
import java.util.ArrayList;
import java.util.List;
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
    public final Slot energyConverter;
    public final Slot energyStorage;
    public final Slot head;
    public final List<Slot> addons;
    private final DataSlot numAddonSlots;
    private final DataSlot componentsEnabled;

    private final ContainerLevelAccess context;
    private final Level world;
    private final Player player;
    private long lastSoundTime = 0;

    public ToolStationScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ContainerLevelAccess.NULL);
    }

    public ToolStationScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(ModernIndustrialization.SCREEN_HANDLER_TOOL_STATION, syncId);
        this.context = context;
        this.world = playerInventory.player.level();
        this.player = playerInventory.player;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, i * 9 + j + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 58 + 84));
        }

        this.tool = new SingleItemSlot(new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                ToolStationScreenHandler.this.slotsChanged(this);
            }
        }, 0, 17, 33);
        this.energyConverter = new SingleItemSlot(new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                ToolStationScreenHandler.this.slotsChanged(this);
            }
        }, 0, 44, 24);
        this.energyStorage = new SingleItemSlot(new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                ToolStationScreenHandler.this.slotsChanged(this);
            }
        }, 0, 62, 24);
        this.head = new SingleItemSlot(new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                ToolStationScreenHandler.this.slotsChanged(this);
            }
        }, 0, 80, 24);
        this.addons = new ArrayList<>();
        for (int idx = 0; idx < 5; ++idx) {
            this.addons.add(new ToggleableSlot(new SimpleContainer(1) {
                public void setChanged() {
                    super.setChanged();
                    ToolStationScreenHandler.this.slotsChanged(this);
                }
            }, 0, 44 + idx * 18, 42));
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
    }

    public void slotsChanged(Container inventory) {
        if (inventory == tool) {
            // TODO: populate slots accordingly
        } else if (tool.hasItem()) {
            // TODO: update tool item
        }
        super.slotsChanged(inventory);
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
        return ItemStack.EMPTY;
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
}
