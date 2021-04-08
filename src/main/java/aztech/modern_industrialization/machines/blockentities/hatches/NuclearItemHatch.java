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
package aztech.modern_industrialization.machines.blockentities.hatches;

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.components.NuclearReactorComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.sync.TemperatureBar;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.entity.BlockEntityType;

public class NuclearItemHatch extends HatchBlockEntity {

    private final MIInventory inventory;
    public NuclearReactorComponent nuclearReactorComponent = new NuclearReactorComponent();

    public NuclearItemHatch(BlockEntityType<?> type) {
        super(type, new MachineGuiParameters.Builder("nuclear_item_hatch", true).build(), new OrientationComponent.Params(false, false, false));
        List<ConfigurableItemStack> itemStack = new ArrayList<>();
        itemStack.add(ConfigurableItemStack.standardInputSlot());
        itemStack.add(ConfigurableItemStack.standardOutputSlot());
        inventory = new MIInventory(itemStack, Collections.emptyList(), new SlotPositions.Builder().addSlot(68, 35).addSlot(98, 35).build(),
                SlotPositions.empty());

        registerComponents(inventory, nuclearReactorComponent);

        TemperatureBar.Parameters temperatureParams = new TemperatureBar.Parameters(43, 60, 2500);
        registerClientComponent(new TemperatureBar.Server(temperatureParams, () -> (int) nuclearReactorComponent.temperature));
    }

    @Override
    public HatchType getHatchType() {
        return HatchType.NUCLEAR_ITEM;
    }

    @Override
    public boolean upgradesToSteel() {
        return false;
    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }

    @Override
    public final void tick() {
        super.tick();
        nuclearReactorComponent.temperature = Math.min(2500, nuclearReactorComponent.temperature + 1);
    }

}
