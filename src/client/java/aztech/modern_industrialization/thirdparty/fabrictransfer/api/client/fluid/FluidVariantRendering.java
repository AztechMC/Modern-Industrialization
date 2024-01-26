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
package aztech.modern_industrialization.thirdparty.fabrictransfer.api.client.fluid;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariantAttributes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side display of fluid variants.
 */
public final class FluidVariantRendering {
    private FluidVariantRendering() {
    }

    /**
     * Return the render handler for the passed fluid, if available, or the default instance otherwise.
     */
    public static IClientFluidTypeExtensions getExtensions(FluidVariant variant) {
        return IClientFluidTypeExtensions.of(variant.getFluid().getFluidType());
    }

    /**
     * Return a mutable list: the tooltip for the passed fluid variant, including the name and additional lines if available
     * and the id of the fluid if advanced tooltips are enabled.
     *
     * <p>
     * Compared to {@linkplain #getTooltip(FluidVariant, TooltipFlag) the other overload}, the current tooltip context is automatically used.
     */
    public static List<Component> getTooltip(FluidVariant fluidVariant) {
        return getTooltip(fluidVariant,
                Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    /**
     * Return a mutable list: the tooltip for the passed fluid variant, including the name and additional lines if available
     * and the id of the fluid if advanced tooltips are enabled.
     */
    public static List<Component> getTooltip(FluidVariant fluidVariant, TooltipFlag context) {
        List<Component> tooltip = new ArrayList<>();

        // Name first
        tooltip.add(FluidVariantAttributes.getName(fluidVariant));

        // If advanced tooltips are enabled, render the fluid id
        if (context.isAdvanced()) {
            tooltip.add(Component.literal(BuiltInRegistries.FLUID.getKey(fluidVariant.getFluid()).toString()).withStyle(ChatFormatting.DARK_GRAY));
        }

        // TODO: consider adding an event to append to tooltips?

        return tooltip;
    }

    /**
     * Return the still sprite that should be used to render the passed fluid variant, or null if it's not available.
     * The sprite should be rendered using the color returned by {@link #getColor}.
     */
    @Nullable
    public static TextureAtlasSprite getSprite(FluidVariant fluidVariant) {
        if (fluidVariant.isBlank()) {
            return null;
        }
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(getExtensions(fluidVariant).getStillTexture(fluidVariant.toStack(1)));
    }

    /**
     * Return the position-independent color that should be used to render {@linkplain #getSprite the sprite} of the passed fluid variant.
     */
    public static int getColor(FluidVariant fluidVariant) {
        return getExtensions(fluidVariant).getTintColor(fluidVariant.toStack(1));
    }

    /**
     * Return the position-independent color that should be used to render {@linkplain #getSprite the sprite} of the passed fluid variant.
     */
    public static int getColor(FluidVariant fluidVariant, @Nullable BlockAndTintGetter level, BlockPos pos) {
        if (level == null) {
            return getColor(fluidVariant);
        }
        return getExtensions(fluidVariant).getTintColor(fluidVariant.getFluid().defaultFluidState(), level, pos);
    }
}
