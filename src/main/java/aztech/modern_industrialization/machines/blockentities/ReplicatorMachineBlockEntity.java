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

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.inventory.*;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import aztech.modern_industrialization.machines.components.MachineInventoryComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.AutoExtract;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.Tickable;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ReplicatorMachineBlockEntity extends MachineBlockEntity implements Tickable {

    private final IsActiveComponent isActiveComponent;
    private final MachineInventoryComponent inventoryComponent;

    private int progressTick = 0;

    public static final TagKey<Item> BLACKLISTED = TagKey.create(BuiltInRegistries.ITEM.key(), new MIIdentifier("replicator_blacklist"));

    public ReplicatorMachineBlockEntity(BEP bep) {

        super(bep, new MachineGuiParameters.Builder("replicator", true).build(), new OrientationComponent.Params(true, true, false));

        isActiveComponent = new IsActiveComponent();
        ProgressBar.Parameters progressBarParams = new ProgressBar.Parameters(85, 34, "arrow");

        long capacity = 81000 * 256;

        List<ConfigurableFluidStack> fluidInput = Collections
                .singletonList(ConfigurableFluidStack.lockedInputSlot(capacity, MIFluids.UU_MATER.asFluid()));
        List<ConfigurableItemStack> itemInputs = Collections.singletonList(ConfigurableItemStack.standardInputSlot());
        List<ConfigurableItemStack> itemOutputs = Collections.singletonList(ConfigurableItemStack.standardOutputSlot());

        SlotPositions fluidSlotPositions = new SlotPositions.Builder().addSlot(35, 35).build();
        SlotPositions itemSlotPositions = new SlotPositions.Builder().addSlot(60, 35).addSlot(115, 35).build();

        this.inventoryComponent = new MachineInventoryComponent(itemInputs, itemOutputs, fluidInput, Collections.emptyList(), itemSlotPositions,
                fluidSlotPositions);

        this.registerComponents(isActiveComponent, inventoryComponent, new IComponent() {
            @Override
            public void writeNbt(CompoundTag tag) {
                tag.putInt("progressTick", progressTick);
            }

            @Override
            public void readNbt(CompoundTag tag) {
                progressTick = tag.getInt("progressTick");
            }
        });

        registerGuiComponent(new ProgressBar.Server(progressBarParams, () -> (float) progressTick / 20));
        registerGuiComponent(new AutoExtract.Server(orientation, false));

    }

    @Override
    public MIInventory getInventory() {
        return inventoryComponent.inventory;
    }

    @Override
    protected MachineModelClientData getModelData() {
        MachineModelClientData data = new MachineModelClientData();
        data.isActive = isActiveComponent.isActive;
        orientation.writeModelData(data);
        return data;
    }

    public boolean replicationStep(boolean simulate) {

        ItemVariant itemVariant = inventoryComponent.getItemInputs().get(0).getResource();

        if (!itemVariant.isBlank()) {
            // check blacklist
            if (itemVariant.toStack().is(BLACKLISTED)) {
                return false;
            }
            // check that the item doesn't contain uu matter
            Storage<FluidVariant> fluidItem = ContainerItemContext.withInitial(itemVariant, 1).find(FluidStorage.ITEM);
            if (fluidItem != null) {
                for (var view : fluidItem) {
                    if (view.getResource().isOf(MIFluids.UU_MATER.asFluid())) {
                        return false;
                    }
                }
            }

            try (Transaction tx = Transaction.openOuter()) {
                MIItemStorage itemStorage = new MIItemStorage(inventoryComponent.getItemOutputs());
                MIFluidStorage fluidStorage = new MIFluidStorage(inventoryComponent.getFluidInputs());

                long inserted = itemStorage.insertAllSlot(itemVariant, 1, tx);
                long uuMatterExtraced = fluidStorage.extractAllSlot(MIFluids.UU_MATER.variant(), FluidConstants.BUCKET / 10, tx);

                if (inserted == 1 && uuMatterExtraced == FluidConstants.BUCKET / 10) {
                    if (!simulate) {
                        tx.commit();
                    }
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {

            if (replicationStep(true)) {
                progressTick++;
                isActiveComponent.updateActive(true, this);
                if (progressTick == 20) {
                    replicationStep(false);
                    progressTick = 0;
                }
            } else {
                isActiveComponent.updateActive(false, this);
                progressTick = 0;
            }

            if (orientation.extractItems) {
                inventoryComponent.inventory.autoExtractItems(level, worldPosition, orientation.outputDirection);
            }

            setChanged();
        }
    }
}
