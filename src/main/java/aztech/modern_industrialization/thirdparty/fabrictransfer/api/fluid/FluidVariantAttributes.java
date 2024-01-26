package aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Common fluid variant attributes, accessible both client-side and server-side.
 */
public final class FluidVariantAttributes {
    private FluidVariantAttributes() {
    }

    /**
     * Return the name that should be used for the passed fluid variant.
     */
    public static Component getName(FluidVariant variant) {
        return variant.getFluid().getFluidType().getDescription(variant.toStack(1));
    }
}
