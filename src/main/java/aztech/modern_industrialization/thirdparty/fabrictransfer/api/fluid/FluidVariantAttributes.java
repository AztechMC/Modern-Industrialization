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

    // TODO NEO
//    /**
//     * Return the sound corresponding to a container of this fluid variant being filled if available,
//     * or the default (water) filling sound otherwise.
//     */
//    public static SoundEvent getFillSound(FluidVariant variant) {
//        return getHandlerOrDefault(variant.getFluid()).getFillSound(variant)
//                .or(() -> variant.getFluid().getBucketFillSound())
//                .orElse(SoundEvents.ITEM_BUCKET_FILL);
//    }
//
//    /**
//     * Return the sound corresponding to a container of this fluid variant being emptied if available,
//     * or the default (water) emptying sound otherwise.
//     */
//    public static SoundEvent getEmptySound(FluidVariant variant) {
//        return getHandlerOrDefault(variant.getFluid()).getEmptySound(variant).orElse(SoundEvents.ITEM_BUCKET_EMPTY);
//    }
//
//    /**
//     * Return an integer in [0, 15]: the light level emitted by this fluid variant, or 0 if it doesn't naturally emit light.
//     */
//    public static int getLuminance(FluidVariant variant) {
//        int luminance = getHandlerOrDefault(variant.getFluid()).getLuminance(variant);
//
//        if (luminance < 0 || luminance > 15) {
//            TransferApiImpl.LOGGER.warn("Broken FluidVariantAttributeHandler. Invalid luminance %d for fluid variant %s".formatted(luminance, variant));
//            return DEFAULT_HANDLER.getLuminance(variant);
//        }
//
//        return luminance;
//    }
//
//    /**
//     * Return a non-negative integer, representing the temperature of this fluid in Kelvin.
//     * The reference values are {@value FluidConstants#WATER_TEMPERATURE} for water, and {@value FluidConstants#LAVA_TEMPERATURE} for lava.
//     */
//    public static int getTemperature(FluidVariant variant) {
//        int temperature = getHandlerOrDefault(variant.getFluid()).getTemperature(variant);
//
//        if (temperature < 0) {
//            TransferApiImpl.LOGGER.warn("Broken FluidVariantAttributeHandler. Invalid temperature %d for fluid variant %s".formatted(temperature, variant));
//            return DEFAULT_HANDLER.getTemperature(variant);
//        }
//
//        return temperature;
//    }
//
//    /**
//     * Return a positive integer, representing the viscosity of this fluid variant.
//     * Fluids with lower viscosity generally flow faster than fluids with higher viscosity.
//     *
//     * <p>More precisely, viscosity should be {@value FluidConstants#VISCOSITY_RATIO} * {@link FlowableFluid#getFlowSpeed} for flowable fluids.
//     * The reference values are {@value FluidConstants#WATER_VISCOSITY} for water,
//     * {@value FluidConstants#LAVA_VISCOSITY_NETHER} for lava in ultrawarm dimensions (such as the nether),
//     * and {@value FluidConstants#LAVA_VISCOSITY} for lava in other dimensions.
//     *
//     * @param world World if available, otherwise null.
//     */
//    public static int getViscosity(FluidVariant variant, @Nullable World world) {
//        int viscosity = getHandlerOrDefault(variant.getFluid()).getViscosity(variant, world);
//
//        if (viscosity <= 0) {
//            TransferApiImpl.LOGGER.warn("Broken FluidVariantAttributeHandler. Invalid viscosity %d for fluid variant %s".formatted(viscosity, variant));
//            return DEFAULT_HANDLER.getViscosity(variant, world);
//        }
//
//        return viscosity;
//    }
//
//    /**
//     * Return true if this fluid is lighter than air.
//     * Fluids that are lighter than air generally flow upwards.
//     */
//    public static boolean isLighterThanAir(FluidVariant variant) {
//        return getHandlerOrDefault(variant.getFluid()).isLighterThanAir(variant);
//    }
}
