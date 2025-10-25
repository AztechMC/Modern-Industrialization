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

package aztech.modern_industrialization.machines.blockentities;

import aztech.modern_industrialization.MICapabilities;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.CableTierHolder;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import aztech.modern_industrialization.api.machine.holder.EnergyComponentHolder;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.CasingComponent;
import aztech.modern_industrialization.machines.components.EnergyComponent;
import aztech.modern_industrialization.machines.components.RedstoneControlComponent;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.Simulation;
import java.util.Collections;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;

public class ElectricWaterPumpBlockEntity extends AbstractWaterPumpBlockEntity implements EnergyComponentHolder, CableTierHolder {
    public ElectricWaterPumpBlockEntity(BEP bep) {
        super(bep, "electric_water_pump");

        long capacity = FluidType.BUCKET_VOLUME * 32;
        this.inventory = new MIInventory(Collections.emptyList(),
                Collections.singletonList(ConfigurableFluidStack.lockedOutputSlot(capacity, Fluids.WATER)), SlotPositions.empty(),
                new SlotPositions.Builder().addSlot(OUTPUT_SLOT_X, OUTPUT_SLOT_Y).build());
        this.redstoneControl = new RedstoneControlComponent();
        this.casing = new CasingComponent();
        this.energy = new EnergyComponent(this, casing::getEuCapacity);
        this.insertable = energy.buildInsertable(casing::canInsertEu);

        this.registerComponents(inventory, redstoneControl, casing, energy);

        registerGuiComponent(new EnergyBar.Server(new EnergyBar.Parameters(18, 32), energy::getEu, energy::getCapacity));
        registerGuiComponent(new SlotPanel.Server(this)
                .withRedstoneControl(redstoneControl)
                .withCasing(casing));
    }

    private final MIInventory inventory;
    private final RedstoneControlComponent redstoneControl;
    private final CasingComponent casing;
    private final EnergyComponent energy;
    private final MIEnergyStorage insertable;

    @Override
    protected long consumeEu(long max) {
        if (redstoneControl.doAllowNormalOperation(
                this)) {
            return energy.consumeEu(max, Simulation.ACT);
        } else {
            return 0;
        }
    }

    @Override
    protected int getWaterMultiplier() {
        return 16;
    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }

    @Override
    protected MachineModelClientData getMachineModelData() {
        MachineModelClientData data = new MachineModelClientData(casing.getCasing());
        data.isActive = isActiveComponent.isActive;
        orientation.writeModelData(data);
        return data;
    }

    @Override
    public EnergyComponent getEnergyComponent() {
        return energy;
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(EnergyApi.SIDED, bet, (be, direction) -> ((ElectricWaterPumpBlockEntity) be).insertable);
        });
    }

    @Override
    protected ItemInteractionResult useItemOn(Player player, InteractionHand hand, Direction face) {
        var result = super.useItemOn(player, hand, face);
        if (!result.consumesAction()) {
            result = redstoneControl.onUse(this, player, hand);
        }
        if (!result.consumesAction()) {
            result = casing.onUse(this, player, hand);
        }
        return result;
    }

    @Override
    public CableTier getCableTier() {
        return casing.getCableTier();
    }
}
