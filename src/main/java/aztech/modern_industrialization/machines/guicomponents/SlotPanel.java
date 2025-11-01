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

package aztech.modern_industrialization.machines.guicomponents;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.inventory.HackySlot;
import aztech.modern_industrialization.inventory.SlotGroup;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CasingComponent;
import aztech.modern_industrialization.machines.components.OverdriveComponent;
import aztech.modern_industrialization.machines.components.RedstoneControlComponent;
import aztech.modern_industrialization.machines.components.UpgradeComponent;
import aztech.modern_industrialization.machines.gui.GuiComponentServer;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public class SlotPanel implements GuiComponentServer<List<SlotPanel.SlotType>, Unit> {
    public static final Type<List<SlotPanel.SlotType>, Unit> TYPE = new Type<>(MI.id("slot_panel"),
            NeoForgeStreamCodecs.enumCodec(SlotPanel.SlotType.class).apply(ByteBufCodecs.list()), StreamCodec.unit(Unit.INSTANCE));

    private final MachineBlockEntity machine;
    private final List<Consumer<MenuFacade>> slotFactories = new ArrayList<>();
    private final List<SlotType> slotTypes = new ArrayList<>();

    public SlotPanel(MachineBlockEntity machine) {
        this.machine = machine;
    }

    public SlotPanel withRedstoneControl(RedstoneControlComponent redstoneControlComponent) {
        return addSlot(SlotType.REDSTONE_MODULE, () -> redstoneControlComponent.getDrop().copy(), redstoneControlComponent::setStackServer);
    }

    public SlotPanel withUpgrades(UpgradeComponent upgradeComponent) {
        return addSlot(SlotType.UPGRADES, () -> upgradeComponent.getDrop().copy(), upgradeComponent::setStackServer);
    }

    public SlotPanel withCasing(CasingComponent casingComponent) {
        return addSlot(SlotType.CASINGS, () -> casingComponent.getDrop().copy(), casingComponent::setCasingServer);
    }

    public SlotPanel withOverdrive(OverdriveComponent overdriveComponent) {
        return addSlot(SlotType.OVERDRIVE_MODULE, () -> overdriveComponent.getDrop().copy(), overdriveComponent::setStackServer);
    }

    private SlotPanel addSlot(SlotType type, Supplier<ItemStack> getStack, BiConsumer<MachineBlockEntity, ItemStack> setStack) {
        int slotIndex = slotTypes.size();
        slotFactories.add(facade -> facade.addSlotToMenu(new HackySlot(getSlotX(machine.guiParams), getSlotY(slotIndex)) {
            @Override
            protected ItemStack getRealStack() {
                return getStack.get();
            }

            @Override
            protected void setRealStack(ItemStack stack) {
                setStack.accept(machine, stack);
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return type.mayPlace(stack);
            }

            @Override
            public int getMaxStackSize() {
                return type.slotLimit;
            }
        }, type.group));
        slotTypes.add(type);
        return this;
    }

    @Override
    public List<SlotType> getParams() {
        return slotTypes;
    }

    @Override
    public Unit extractData() {
        return Unit.INSTANCE;
    }

    @Override
    public Type<List<SlotType>, Unit> getType() {
        return TYPE;
    }

    @Override
    public void setupMenu(MenuFacade menu) {
        for (var factory : slotFactories) {
            factory.accept(menu);
        }
    }

    public static int getSlotY(int slotIndex) {
        return 19 + slotIndex * 20;
    }

    public static int getSlotX(MachineGuiParameters guiParameters) {
        return guiParameters.backgroundWidth + 6;
    }

    public enum SlotType {
        REDSTONE_MODULE(SlotGroup.REDSTONE_MODULE, 1, MIItem.REDSTONE_CONTROL_MODULE::is, 36, 80, MIText.AcceptsRedstoneControlModule),
        UPGRADES(SlotGroup.UPGRADES, 64, stack -> UpgradeComponent.getExtraEu(stack.getItem()) > 0, 0, 80, MIText.AcceptsUpgrades),
        // Assumes that the default casing is always the LV casing for now,
        CASINGS(SlotGroup.CASING, 1, stack -> CasingComponent.getCasingTier(stack.getItem()) != null, 18, 80, MIText.AcceptsMachineHull),
        OVERDRIVE_MODULE(SlotGroup.OVERDRIVE_MODULE, 1, MIItem.OVERDRIVE_MODULE::is, 0, 98, MIText.AcceptsOverdriveModule);

        public final SlotGroup group;
        public final int slotLimit;
        public final Predicate<ItemStack> insertionChecker;
        public final int u, v;
        public final MIText tooltip;

        SlotType(SlotGroup group, int slotLimit, Predicate<ItemStack> insertionChecker, int u, int v, MIText tooltip) {
            this.group = group;
            this.slotLimit = slotLimit;
            this.insertionChecker = insertionChecker;
            this.u = u;
            this.v = v;
            this.tooltip = tooltip;
        }

        public boolean mayPlace(ItemStack stack) {
            return insertionChecker.test(stack);
        }
    }
}
