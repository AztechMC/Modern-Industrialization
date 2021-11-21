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
import aztech.modern_industrialization.machines.components.sync.AutoExtract;
import aztech.modern_industrialization.machines.components.sync.ProgressBar;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.util.ResourceUtil;
import aztech.modern_industrialization.util.Tickable;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class ReplicatorMachineBlockEntity extends MachineBlockEntity implements Tickable {

    private final IsActiveComponent isActiveComponent;
    private final MachineInventoryComponent inventoryComponent;

    private int progressTick = 0;

    private static Tag<Item> blacklisted;

    public static void initTag() {
        Identifier blacklist = new MIIdentifier("replicator_blacklist");
        blacklisted = TagRegistry.item(blacklist);

        ResourceUtil.appendToItemTag(blacklist, new Identifier("minecraft", "bundle"));
        ResourceUtil.appendToItemTag(blacklist, new MIIdentifier("bucket_uu_matter"));

        ResourceUtil.appendTagToItemTag(blacklist, new Identifier("c", "shulker_box"));
        ResourceUtil.appendTagToItemTag(blacklist, new MIIdentifier("tanks"));
        ResourceUtil.appendTagToItemTag(blacklist, new MIIdentifier("barrels"));

    }

    public ReplicatorMachineBlockEntity(BEP bep) {

        super(bep, new MachineGuiParameters.Builder("replicator", true).build(), new OrientationComponent.Params(true, true, false));

        isActiveComponent = new IsActiveComponent();
        ProgressBar.Parameters progressBarParams = new ProgressBar.Parameters(85, 34, "arrow");

        long capacity = 81000 * 256;

        List<ConfigurableFluidStack> fluidInput = Collections.singletonList(ConfigurableFluidStack.lockedInputSlot(capacity, MIFluids.UU_MATER));
        List<ConfigurableItemStack> itemInputs = Collections.singletonList(ConfigurableItemStack.standardInputSlot());
        List<ConfigurableItemStack> itemOutputs = Collections.singletonList(ConfigurableItemStack.standardOutputSlot());

        SlotPositions fluidSlotPositions = new SlotPositions.Builder().addSlot(35, 35).build();
        SlotPositions itemSlotPositions = new SlotPositions.Builder().addSlot(60, 35).addSlot(115, 35).build();

        this.inventoryComponent = new MachineInventoryComponent(itemInputs, itemOutputs, fluidInput, Collections.emptyList(), itemSlotPositions,
                fluidSlotPositions);

        this.registerComponents(isActiveComponent, inventoryComponent, new IComponent() {
            @Override
            public void writeNbt(NbtCompound tag) {
                tag.putInt("progressTick", progressTick);
            }

            @Override
            public void readNbt(NbtCompound tag) {
                progressTick = tag.getInt("progressTick");
            }
        });

        registerClientComponent(new ProgressBar.Server(progressBarParams, () -> (float) progressTick / 20));
        registerClientComponent(new AutoExtract.Server(orientation, false));

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

    @Override
    public void onPlaced(LivingEntity placer, ItemStack itemStack) {
        orientation.onPlaced(placer, itemStack);
    }

    public boolean replicationStep(boolean simulate) {

        ItemVariant itemVariant = inventoryComponent.getItemInputs().get(0).getResource();

        if (!itemVariant.isBlank()) {

            if (blacklisted.contains(itemVariant.getItem())) {
                return false;
            }

            try (Transaction tx = Transaction.openOuter()) {
                MIItemStorage itemStorage = new MIItemStorage(inventoryComponent.getItemOutputs());
                MIFluidStorage fluidStorage = new MIFluidStorage(inventoryComponent.getFluidInputs());

                long inserted;

                try (Transaction simul = Transaction.openNested(tx)) {
                    inserted = itemStorage.insertAllSlot(itemVariant, 1, simul);
                }

                long uuMatterExtraced;

                try (Transaction simul = Transaction.openNested(tx)) {
                    uuMatterExtraced = fluidStorage.extractAllSlot(FluidVariant.of(MIFluids.UU_MATER), FluidConstants.BUCKET / 10, simul);
                }

                if (inserted == 1 && uuMatterExtraced == FluidConstants.BUCKET / 10) {
                    if (!simulate) {
                        itemStorage.insertAllSlot(itemVariant, 1, tx);
                        fluidStorage.extractAllSlot(FluidVariant.of(MIFluids.UU_MATER), FluidConstants.BUCKET / 10, tx);
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
        if (!world.isClient) {

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
                inventoryComponent.inventory.autoExtractItems(world, pos, orientation.outputDirection);
            }

            markDirty();
        }
    }
}
