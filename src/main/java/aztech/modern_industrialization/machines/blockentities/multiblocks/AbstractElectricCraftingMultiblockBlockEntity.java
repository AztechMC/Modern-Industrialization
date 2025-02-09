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
package aztech.modern_industrialization.machines.blockentities.multiblocks;

import aztech.modern_industrialization.api.machine.holder.EnergyListComponentHolder;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.*;
import aztech.modern_industrialization.machines.guicomponents.CraftingMultiblockGui;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.util.Simulation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractElectricCraftingMultiblockBlockEntity extends AbstractCraftingMultiblockBlockEntity
        implements EnergyListComponentHolder, CrafterComponent.Behavior {

    public AbstractElectricCraftingMultiblockBlockEntity(BEP bep, String name, OrientationComponent.Params orientationParams,
            ShapeTemplate[] shapeTemplates) {
        super(bep, name, orientationParams, shapeTemplates);

        this.redstoneControl = new RedstoneControlComponent();
        registerGuiComponent(new CraftingMultiblockGui.Server(() -> shapeValid.shapeValid, crafter::getProgress, crafter, () -> 0));
        registerComponents(redstoneControl);
    }

    protected final RedstoneControlComponent redstoneControl;
    protected final List<EnergyComponent> energyInputs = new ArrayList<>();

    @Override
    public List<EnergyComponent> getEnergyComponents() {
        return energyInputs;
    }

    @Override
    protected void onRematch(ShapeMatcher shapeMatcher) {
        super.onRematch(shapeMatcher);
        if (shapeMatcher.isMatchSuccessful()) {
            energyInputs.clear();
            for (HatchBlockEntity hatch : shapeMatcher.getMatchedHatches()) {
                hatch.appendEnergyInputs(energyInputs);
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(Player player, InteractionHand hand, Direction face) {
        var result = super.useItemOn(player, hand, face);
        if (!result.consumesAction()) {
            result = LubricantHelper.onUse(this.crafter, player, hand);
        }
        if (!result.consumesAction()) {
            result = components.mapOrDefault(UpgradeComponent.class, upgrade -> upgrade.onUse(this, player, hand), result);
        }
        if (!result.consumesAction()) {
            result = redstoneControl.onUse(this, player, hand);
        }
        if (!result.consumesAction()) {
            result = components.mapOrDefault(OverdriveComponent.class, overdrive -> overdrive.onUse(this, player, hand), result);
        }
        return result;
    }

    @Override
    protected final CrafterComponent.Behavior getBehavior() {
        return this;
    }

    @Override
    public final boolean isEnabled() {
        return redstoneControl.doAllowNormalOperation(this);
    }

    @Override
    public final long consumeEu(long max, Simulation simulation) {
        long total = 0;

        for (EnergyComponent energyComponent : energyInputs) {
            total += energyComponent.consumeEu(max - total, simulation);
        }

        return total;
    }

    @Override
    public final ServerLevel getCrafterWorld() {
        return (ServerLevel) level;
    }

    @Override
    @Nullable
    public final UUID getOwnerUuid() {
        return placedBy.placerId;
    }
}
