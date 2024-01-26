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
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import aztech.modern_industrialization.api.machine.holder.EnergyComponentHolder;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.EnergyComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.components.RedstoneControlComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.helper.EnergyHelper;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.Tickable;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;

public abstract class AbstractStorageMachineBlockEntity extends MachineBlockEntity implements Tickable, EnergyComponentHolder {

    protected final EnergyComponent energy;
    private final RedstoneControlComponent redstoneControl;

    protected final MIEnergyStorage insertable;
    protected final MIEnergyStorage extractable;

    protected final long euCapacity;
    protected final CableTier from, to;

    protected boolean extractableOnOutputDirection;

    public AbstractStorageMachineBlockEntity(BEP bep, CableTier from, CableTier to, String name, long euCapacity) {
        this(bep, from, to, name, euCapacity, true);

    }

    public AbstractStorageMachineBlockEntity(BEP bep, CableTier from, CableTier to, String name, long euCapacity,
            boolean extractableOnOutputDirection) {
        super(bep, new MachineGuiParameters.Builder(name, false).build(), new OrientationComponent.Params(true, false, false));

        this.from = from;
        this.to = to;
        this.euCapacity = euCapacity;

        this.energy = new EnergyComponent(this, euCapacity);
        this.redstoneControl = new RedstoneControlComponent();

        this.insertable = energy.buildInsertable((CableTier tier) -> tier == from);
        this.extractable = energy.buildExtractable((CableTier tier) -> tier == to && redstoneControl.doAllowNormalOperation(this));

        this.registerComponents(energy, redstoneControl);

        EnergyBar.Parameters energyBarParams = new EnergyBar.Parameters(76, 39);
        registerGuiComponent(new EnergyBar.Server(energyBarParams, energy::getEu, energy::getCapacity),
                new SlotPanel.Server(this).withRedstoneControl(redstoneControl));

        this.extractableOnOutputDirection = extractableOnOutputDirection;

    }

    @Override
    public MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    public static MachineCasing getCasingFromTier(CableTier from, CableTier to) {
        if (from.eu > to.eu) {
            return from.casing;
        } else {
            return to.casing;
        }
    }

    @Override
    protected MachineModelClientData getMachineModelData() {
        MachineModelClientData data = new MachineModelClientData();
        orientation.writeModelData(data);
        return data;
    }

    @Override
    public void tick() {
        if (!level.isClientSide()) {
            if (extractableOnOutputDirection) {
                EnergyHelper.autoOutput(this, orientation, to, extractable);
            } else {
                for (Direction side : Direction.values()) {
                    if (side == orientation.outputDirection) {
                        continue;
                    }

                    EnergyHelper.autoOutput(this, side, to, extractable);
                }
            }
        }
    }

    @Override
    public EnergyComponent getEnergyComponent() {
        return energy;
    }

    @Override
    protected InteractionResult onUse(Player player, InteractionHand hand, Direction face) {
        var energyItem = player.getItemInHand(hand).getCapability(EnergyApi.ITEM);
        int stackSize = player.getItemInHand(hand).getCount();
        if (energyItem != null) {
            if (!player.level().isClientSide()) {
                boolean insertedSomething = false;

                for (int i = 0; i < 10000; ++i) { // Try up to 10000 times to bypass I/O limits
                    try (Transaction transaction = Transaction.openOuter()) {
                        long inserted = energyItem.receive(energy.getEu() / stackSize, false);

                        if (inserted == 0) {
                            break;
                        } else {
                            insertedSomething = true;
                        }

                        energy.consumeEu(inserted * stackSize, Simulation.ACT);
                        transaction.commit();
                    }
                }

                if (!insertedSomething) {
                    for (int i = 0; i < 10000; ++i) { // Try up to 10000 times to bypass I/O limits
                        try (Transaction transaction = Transaction.openOuter()) {
                            long extracted = energyItem.extract(energy.getRemainingCapacity() / stackSize, false);

                            if (extracted == 0) {
                                break;
                            }

                            energy.insertEu(extracted * stackSize, Simulation.ACT);
                            transaction.commit();
                        }
                    }
                }
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide());
        }
        return super.onUse(player, hand, face);
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        MICapabilities.onEvent(event -> {
            event.registerBlockEntity(EnergyApi.SIDED, bet, (be, direction) -> {
                AbstractStorageMachineBlockEntity abe = (AbstractStorageMachineBlockEntity) be;

                if (abe.extractableOnOutputDirection) {
                    if (abe.orientation.outputDirection == direction) {
                        return abe.extractable;
                    } else {
                        return abe.insertable;
                    }
                } else {
                    if (abe.orientation.outputDirection == direction) {
                        return abe.insertable;
                    } else {
                        return abe.extractable;
                    }
                }
            });
        });
    }
}
