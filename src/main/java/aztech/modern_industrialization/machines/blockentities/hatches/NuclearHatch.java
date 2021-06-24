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

import static net.minecraft.util.math.Direction.UP;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.SteamHeaterComponent;
import aztech.modern_industrialization.machines.components.TemperatureComponent;
import aztech.modern_industrialization.machines.components.sync.TemperatureBar;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import dev.technici4n.fasttransferlib.experimental.api.item.ItemStorage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.block.entity.BlockEntityType;

public class NuclearHatch extends HatchBlockEntity {

    private static final int MAX_TEMPERATURE = 3800;
    public static final int EU_PER_DEGREE = 128;

    private final MIInventory inventory;
    public final TemperatureComponent nuclearReactorComponent;
    public final boolean isFluid;
    public static final double BASE_HEAT_CONDUCTION = 0.01;

    public NuclearHatch(BEP bep, boolean isFluid) {
        super(bep, new MachineGuiParameters.Builder(isFluid ? "nuclear_fluid_hatch" : "nuclear_item_hatch", true).build(),
                new OrientationComponent.Params(false, false, false));

        this.isFluid = isFluid;
        SlotPositions slotPos = new SlotPositions.Builder().addSlot(68, 31).addSlots(98, 22, 2, 1).build();
        if (!isFluid) {
            List<ConfigurableItemStack> itemStack = new ArrayList<>();
            itemStack.add(ConfigurableItemStack.standardInputSlot());
            itemStack.add(ConfigurableItemStack.standardOutputSlot());
            itemStack.add(ConfigurableItemStack.standardOutputSlot());
            inventory = new MIInventory(itemStack, Collections.emptyList(), slotPos, SlotPositions.empty());
            nuclearReactorComponent = new TemperatureComponent(MAX_TEMPERATURE);
        } else {
            long capacity = 64000 * 81;
            List<ConfigurableFluidStack> fluidStack = new ArrayList<>();
            fluidStack.add(ConfigurableFluidStack.standardInputSlot(capacity));
            fluidStack.add(ConfigurableFluidStack.standardOutputSlot(capacity));
            fluidStack.add(ConfigurableFluidStack.standardOutputSlot(capacity));
            inventory = new MIInventory(Collections.emptyList(), fluidStack, SlotPositions.empty(), slotPos);
            nuclearReactorComponent = new SteamHeaterComponent(MAX_TEMPERATURE, 4096, EU_PER_DEGREE, true, true);
        }

        registerComponents(inventory, nuclearReactorComponent);
        TemperatureBar.Parameters temperatureParams = new TemperatureBar.Parameters(43, 63, MAX_TEMPERATURE);
        registerClientComponent(new TemperatureBar.Server(temperatureParams, () -> (int) nuclearReactorComponent.getTemperature()));
    }

    @Override
    public HatchType getHatchType() {
        return isFluid ? HatchType.NUCLEAR_FLUID : HatchType.NUCLEAR_ITEM;
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
        if (isFluid) {
            ((SteamHeaterComponent) nuclearReactorComponent).tick(Collections.singletonList(inventory.getFluidStacks().get(0)),
                    Collections.singletonList(inventory.getFluidStacks().get(1)));
        }
    }

    public static void registerItemApi(BlockEntityType<?> bet) {
        ItemStorage.SIDED.registerForBlockEntities((be, direction) -> direction == UP ? ((NuclearHatch) be).getInventory().itemStorage : null, bet);
    }

    public static void registerFluidApi(BlockEntityType<?> bet) {
        FluidStorage.SIDED.registerForBlockEntities((be, direction) -> direction == UP ? ((NuclearHatch) be).getInventory().fluidStorage : null, bet);
    }

}
