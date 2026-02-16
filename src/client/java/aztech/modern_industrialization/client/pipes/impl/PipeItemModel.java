package aztech.modern_industrialization.client.pipes.impl;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.client.pipes.api.PipeRenderer;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.impl.PipeItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public record PipeItemModel(ItemTransforms transforms, PipeBlockStateModel blockModel) implements ItemModel {
    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        if (item.getItem() instanceof PipeItem pipe) {
            PipeNetworkType type = pipe.type;
            output.appendModelIdentityElement(type);
            int color = type.getColor();

            var layer = output.newLayer();
            layer.setUsesBlockLight(true);
            layer.setParticleIcon(blockModel.particleIcon());
            layer.setTransform(transforms.getTransform(displayContext));
            Consumer<BakedQuad> quadConsumer = quad -> {
                layer.prepareQuadList().add(new BakedQuad(
                        scalePos(quad.position0()), scalePos(quad.position1()), scalePos(quad.position2()), scalePos(quad.position3()),
                        quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                        quad.tintIndex(), quad.direction(), quad.sprite(), quad.shade(), quad.lightEmission(),
                        quad.bakedNormals(), quad.bakedColors(), quad.hasAmbientOcclusion()));
            };

            PipeEndpointType[][] connections = new @Nullable PipeEndpointType[][] {
                    { null, null, null, null, PipeEndpointType.BLOCK, PipeEndpointType.BLOCK } };
            blockModel.renderers.get(PipeRenderer.get(type)).draw(
                    quadConsumer, q -> {},
                    EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO,
                    0, connections, color, null);
            layer.setRenderType(Sheets.cutoutBlockSheet());
        }
    }

    /**
     * Scale pipe to make items look better.
     */
    private static Vector3fc scalePos(Vector3fc pos) {
        return new Vector3f(pos.x(), pos.y() * 2 - 0.5f, pos.z() * 2 - 0.5f);
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final Identifier TYPE_ID = MI.id("pipe");

        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(INSTANCE);

        private static final Identifier BLOCK_BLOCK = Identifier.withDefaultNamespace("block/block");

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public ItemModel bake(BakingContext context) {
            return new PipeItemModel(
                    context.blockModelBaker().getModel(BLOCK_BLOCK).getTopTransforms(),
                    PipeUnbakedModel.getOrBakeBlockModel(context.blockModelBaker()));
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(BLOCK_BLOCK);
        }
    }
}
