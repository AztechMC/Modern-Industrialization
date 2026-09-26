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

package aztech.modern_industrialization.client.compat.guideme.recipe.fluid;

import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariantAttributes;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.TextHelper;
import guideme.document.interaction.GuideTooltip;
import guideme.siteexport.ResourceExporter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforgespi.language.IModInfo;

public class MachineRecipeFluidTooltip implements GuideTooltip {
    private final FluidVariant fluid;
    private final ItemStack icon;
    private final long amount;
    private final float probability;
    private final boolean input;

    public MachineRecipeFluidTooltip(FluidVariant fluid, long amount, float probability, boolean input) {
        this.fluid = fluid;
        this.icon = fluid.getFluid().getFluidType().getBucket(fluid.toStack(1));
        this.amount = amount;
        this.probability = probability;
        this.input = input;
    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public List<ClientTooltipComponent> getLines() {
        List<Component> lines = new ArrayList<>();

        lines.add(FluidVariantAttributes.getName(fluid));

        if (Minecraft.getInstance().options.advancedItemTooltips) {
            lines.add(Component.literal(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString()).withStyle(ChatFormatting.DARK_GRAY));
        }

        if (amount > 1) {
            lines.add(FluidHelper.getFluidAmount(amount).withStyle(MITooltips.DEFAULT_STYLE));
        }

        var modDisplayName = ModList.get()
                .getModContainerById(NeoForgeRegistries.FLUID_TYPES.getKey(fluid.getFluid().getFluidType()).getNamespace())
                .map(ModContainer::getModInfo)
                .map(IModInfo::getDisplayName);
        if (modDisplayName.isPresent()) {
            lines.add(Component.literal(modDisplayName.orElse("")).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
        }

        if (probability != 1) {
            lines.add(TextHelper.getProbabilityTooltip(probability, input));
        }

        return lines.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();
    }

    @Override
    public void exportResources(ResourceExporter exporter) {
        exporter.referenceFluid(fluid.getFluid());
    }
}
