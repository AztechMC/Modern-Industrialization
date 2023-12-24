package aztech.modern_industrialization.thirdparty.fabrictransfer.api.client.fluid;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Client-side display of fluid variants.
 */
public final class FluidVariantRendering {
    private FluidVariantRendering () {
    }

    /**
     * Return the render handler for the passed fluid, if available, or the default instance otherwise.
     */
    public static IClientFluidTypeExtensions getExtensions(FluidVariant variant) {
        return IClientFluidTypeExtensions.of(variant.getFluid().getFluidType());
    }

    // TODO NEO
//    /**
//     * Return a mutable list: the tooltip for the passed fluid variant, including the name and additional lines if available
//     * and the id of the fluid if advanced tooltips are enabled.
//     *
//     * <p>Compared to {@linkplain #getTooltip(FluidVariant, TooltipContext) the other overload}, the current tooltip context is automatically used.
//     */
//    public static List<Text> getTooltip(FluidVariant fluidVariant) {
//        return getTooltip(fluidVariant, MinecraftClient.getInstance().options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC);
//    }
//
//    /**
//     * Return a mutable list: the tooltip for the passed fluid variant, including the name and additional lines if available
//     * and the id of the fluid if advanced tooltips are enabled.
//     */
//    public static List<Text> getTooltip(FluidVariant fluidVariant, TooltipContext context) {
//        List<Text> tooltip = new ArrayList<>();
//
//        // Name first
//        tooltip.add(FluidVariantAttributes.getName(fluidVariant));
//
//        // Additional tooltip information
//        getHandlerOrDefault(fluidVariant.getFluid()).appendTooltip(fluidVariant, tooltip, context);
//
//        // If advanced tooltips are enabled, render the fluid id
//        if (context.isAdvanced()) {
//            tooltip.add(Text.literal(Registries.FLUID.getId(fluidVariant.getFluid()).toString()).formatted(Formatting.DARK_GRAY));
//        }
//
//        // TODO: consider adding an event to append to tooltips?
//
//        return tooltip;
//    }

    /**
     * Return the still sprite that should be used to render the passed fluid variant, or null if it's not available.
     * The sprite should be rendered using the color returned by {@link #getColor}.
     */
    @Nullable
    public static TextureAtlasSprite getSprite(FluidVariant fluidVariant) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(getExtensions(fluidVariant).getStillTexture(fluidVariant.toStack(1)));
    }

    /**
     * Return the position-independent color that should be used to render {@linkplain #getSprite the sprite} of the passed fluid variant.
     */
    public static int getColor(FluidVariant fluidVariant) {
        return getExtensions(fluidVariant).getTintColor(fluidVariant.toStack(1));
    }

    // TODO NEO
//    /**
//     * Return the color that should be used when rendering {@linkplain #getSprite the sprite} of the passed fluid variant.
//     *
//     * <p>If the world and the position parameters are null, a position-independent color is returned.
//     * If the world and position parameters are not null, the color may depend on the position.
//     * For example, if world and position are passed, water will use them to return a biome-dependent color.
//     */
//    public static int getColor(FluidVariant fluidVariant, @Nullable BlockRenderView view, @Nullable BlockPos pos) {
//        return getHandlerOrDefault(fluidVariant.getFluid()).getColor(fluidVariant, view, pos);
//    }
}