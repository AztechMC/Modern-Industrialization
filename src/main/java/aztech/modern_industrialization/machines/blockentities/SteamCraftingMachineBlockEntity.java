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

import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.MachineInventoryComponent;
import aztech.modern_industrialization.machines.components.OverclockComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.guicomponents.GunpowderOverclockGui;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.helper.SteamHelper;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.util.Simulation;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class SteamCraftingMachineBlockEntity extends AbstractCraftingMachineBlockEntity {

    private final OverclockComponent overclockComponent;

    public SteamCraftingMachineBlockEntity(BEP bep, MachineRecipeType recipeType, MachineInventoryComponent inventory, MachineGuiParameters guiParams,
            ProgressBar.Parameters progressBarParams, MachineTier tier, List<OverclockComponent.Catalyst> overclockCatalysts) {
        super(bep, recipeType, inventory, guiParams, progressBarParams, tier);
        this.overclockComponent = new OverclockComponent(overclockCatalysts);

        GunpowderOverclockGui.Parameters gunpowderOverclockGuiParams = new GunpowderOverclockGui.Parameters(progressBarParams.renderX,
                progressBarParams.renderY + 20);
        registerGuiComponent(new GunpowderOverclockGui.Server(gunpowderOverclockGuiParams, overclockComponent::getTicks));
        this.registerComponents(overclockComponent);
    }

    @Override
    public long consumeEu(long max, Simulation simulation) {
        return SteamHelper.consumeSteamEu(getInventory().getFluidStacks(), max, simulation);
    }

    @Override
    protected MachineModelClientData getMachineModelData() {
        MachineModelClientData data = new MachineModelClientData();
        orientation.writeModelData(data);
        data.isActive = isActiveComponent.isActive;
        return data;
    }

    @Override
    protected InteractionResult onUse(Player player, InteractionHand hand, Direction face) {
        InteractionResult result = super.onUse(player, hand, face);
        if (!result.consumesAction()) {
            return overclockComponent.onUse(this, player, hand);
        }
        return result;
    }

    @Override
    public long getMaxRecipeEu() {
        return overclockComponent.getRecipeEu(tier.getMaxEu());
    }

    @Override
    public long getBaseRecipeEu() {
        return overclockComponent.getRecipeEu(tier.getBaseEu());
    }

    public void tick() {
        super.tick();
        overclockComponent.tick(this);
    }

    @Override
    public List<Component> getTooltips() {
        return overclockComponent.getTooltips();
    }
}
