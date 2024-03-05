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

import aztech.modern_industrialization.api.machine.holder.CrafterComponentHolder;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import aztech.modern_industrialization.machines.components.MachineInventoryComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.AutoExtract;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.guicomponents.ReiSlotLocking;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.util.Tickable;
import java.util.UUID;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCraftingMachineBlockEntity extends MachineBlockEntity implements CrafterComponent.Behavior, Tickable,
        CrafterComponentHolder {
    public AbstractCraftingMachineBlockEntity(BEP bep, MachineRecipeType recipeType, MachineInventoryComponent inventory,
            MachineGuiParameters guiParams, ProgressBar.Parameters progressBarParams, MachineTier tier) {
        super(bep, guiParams, new OrientationComponent.Params(true, inventory.itemOutputCount > 0, inventory.fluidOutputCount > 0));
        this.inventory = inventory;
        this.crafter = new CrafterComponent(this, inventory, this);
        this.type = recipeType;
        this.tier = tier;
        this.isActiveComponent = new IsActiveComponent();
        registerGuiComponent(new AutoExtract.Server(orientation));
        registerGuiComponent(new ProgressBar.Server(progressBarParams, crafter::getProgress));
        registerGuiComponent(new ReiSlotLocking.Server(crafter::lockRecipe, () -> true));
        this.registerComponents(crafter, this.inventory, isActiveComponent);
    }

    private final MachineInventoryComponent inventory;
    protected final CrafterComponent crafter;

    private final MachineRecipeType type;
    public final MachineTier tier;
    protected IsActiveComponent isActiveComponent;

    @Override
    public MachineRecipeType recipeType() {
        return type;
    }

    @Override
    public CrafterComponent getCrafterComponent() {
        return crafter;
    }

    @Override
    public long getBaseRecipeEu() {
        return tier.getBaseEu();
    }

    @Override
    public long getMaxRecipeEu() {
        return tier.getMaxEu();
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            boolean newActive = crafter.tickRecipe();
            isActiveComponent.updateActive(newActive, this);
            if (orientation.extractItems) {
                inventory.inventory.autoExtractItems(level, worldPosition, orientation.outputDirection);
            }
            if (orientation.extractFluids) {
                inventory.inventory.autoExtractFluids(level, worldPosition, orientation.outputDirection);
            }
            setChanged();
        }
    }

    @Override
    public MIInventory getInventory() {
        return inventory.inventory;
    }

    @Override
    public Level getCrafterWorld() {
        return level;
    }

    @Override
    @Nullable
    public UUID getOwnerUuid() {
        return placedBy.placerId;
    }
}
