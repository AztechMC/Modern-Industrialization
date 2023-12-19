package aztech.modern_industrialization.fluid;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.definition.BlockDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.function.Consumer;

public class MIFluidType extends FluidType {
    private final DeferredBlock<MIFluidBlock> fluidBlock;

    public MIFluidType(DeferredBlock<MIFluidBlock> fluidBlock, Properties properties) {
        super(properties);
        this.fluidBlock = fluidBlock;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            private ResourceLocation textureLocation;

            @Override
            public ResourceLocation getStillTexture() {
                if (textureLocation == null) {
                    textureLocation = MI.id("fluid/%s_still".formatted(fluidBlock.getId().getPath()));
                }
                return textureLocation;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return IClientFluidTypeExtensions.of(Fluids.WATER).getFlowingTexture();
            }
        });
    }
}
