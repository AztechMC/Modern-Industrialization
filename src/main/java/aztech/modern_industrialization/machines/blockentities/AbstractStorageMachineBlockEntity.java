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

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.compat.megane.holder.EnergyComponentHolder;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.EnergyComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.sync.EnergyBar;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.helper.EnergyHelper;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.Tickable;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import team.reborn.energy.api.EnergyStorage;

public abstract class AbstractStorageMachineBlockEntity extends MachineBlockEntity implements Tickable, EnergyComponentHolder {

    protected final EnergyComponent energy;

    protected final EnergyInsertable insertable;
    protected final EnergyExtractable extractable;

    protected final long eu_capacity;
    protected final CableTier from, to;

    public AbstractStorageMachineBlockEntity(BEP bep, CableTier from, CableTier to, String name, long eu_capacity) {
        super(bep, new MachineGuiParameters.Builder(name, false).build(), new OrientationComponent.Params(true, false, false));

        this.from = from;
        this.to = to;
        this.eu_capacity = eu_capacity;

        this.energy = new EnergyComponent(eu_capacity);
        insertable = energy.buildInsertable((CableTier tier) -> tier == from);
        extractable = energy.buildExtractable((CableTier tier) -> tier == to);
        EnergyBar.Parameters energyBarParams = new EnergyBar.Parameters(76, 39);
        registerClientComponent(new EnergyBar.Server(energyBarParams, energy::getEu, energy::getCapacity));

        this.registerComponents(energy);

    }

    @Override
    public MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    public static MachineCasing getCasingFromTier(CableTier from, CableTier to) {
        return MachineCasings.casingFromCableTier(from.eu > to.eu ? from : to);
    }

    @Override
    protected MachineModelClientData getModelData() {
        MachineModelClientData data = new MachineModelClientData();
        orientation.writeModelData(data);
        return data;
    }

    @Override
    public void tick() {
        if (!level.isClientSide()) {
            EnergyHelper.autoOuput(this, orientation, to, energy);
        }
    }

    @Override
    public EnergyComponent getEnergyComponent() {
        return energy;
    }

    @Override
    protected InteractionResult onUse(Player player, InteractionHand hand, Direction face) {
        var energyItem = ContainerItemContext.ofPlayerHand(player, hand).find(EnergyStorage.ITEM);
        if (energyItem != null) {
            if (!player.level.isClientSide()) {
                for (int i = 0; i < 10000; ++i) { // Try up to 10000 times to bypass I/O limits
                    try (Transaction transaction = Transaction.openOuter()) {
                        long inserted = energyItem.insert(energy.getEu(), transaction);

                        if (inserted == 0) {
                            break;
                        }

                        energy.consumeEu(inserted, Simulation.ACT);
                        transaction.commit();
                    }
                }
            }
            return InteractionResult.sidedSuccess(player.level.isClientSide());
        }
        return super.onUse(player, hand, face);
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        EnergyApi.MOVEABLE.registerForBlockEntities((be, direction) -> {
            AbstractStorageMachineBlockEntity abe = (AbstractStorageMachineBlockEntity) be;
            if (abe.orientation.outputDirection == direction) {
                return abe.extractable;
            } else {
                return abe.insertable;
            }
        }, bet);
    }
}
