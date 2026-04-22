package aztech.modern_industrialization.client.machines.models;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.client.util.ModelHelper;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

public record MachineItemModel(ItemTransforms transforms, Matrix4fc transformation, Block machine) implements ItemModel {
    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(machine);

        // A bit dirty...
        var machineModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(machine.defaultBlockState());

        var layer = output.newLayer();
        layer.setUsesBlockLight(true);
        layer.setLocalTransform(transformation);
        layer.setParticleMaterial(machineModel.particleMaterial());
        layer.setItemTransform(transforms.getTransform(displayContext));

        var parts = new ArrayList<BlockStateModelPart>();
        machineModel.collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, machine.defaultBlockState(), RandomSource.create(seed), parts);
        for (var part : parts) {
            for (var direction : ModelHelper.DIRECTIONS_WITH_NULL) {
                layer.prepareQuadList().addAll(part.getQuads(direction));
            }
        }
    }

    public record Unbaked(Block machine) implements ItemModel.Unbaked {
        public static final Identifier TYPE_ID = MI.id("machine");

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i ->
                i.group(
                        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("machine").forGetter(Unbaked::machine))
                        .apply(i, Unbaked::new));

        private static final Identifier BLOCK_BLOCK = Identifier.withDefaultNamespace("block/block");

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public ItemModel bake(BakingContext context, Matrix4fc transformation) {
            return new MachineItemModel(
                    context.blockModelBaker().getModel(BLOCK_BLOCK).getTopTransforms(),
                    transformation,
                    machine);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(BLOCK_BLOCK);
        }
    }
}
