package aztech.modern_industrialization.client.machines.models;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.client.util.ModelHelper;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public record MachineItemModel(ItemTransforms transforms, Block machine) implements ItemModel {
    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(machine);

        // A bit dirty...
        var machineModel = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(machine.defaultBlockState());

        var layer = output.newLayer();
        layer.setUsesBlockLight(true);
        layer.setParticleIcon(machineModel.particleIcon());
        layer.setTransform(transforms.getTransform(displayContext));

        var parts = machineModel.collectParts(EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO, machine.defaultBlockState(), RandomSource.create(seed));
        for (var part : parts) {
            for (var direction : ModelHelper.DIRECTIONS_WITH_NULL) {
                layer.prepareQuadList().addAll(part.getQuads(direction));
            }
        }
        layer.setRenderType(Sheets.cutoutBlockSheet());
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
        public ItemModel bake(BakingContext context) {
            return new MachineItemModel(
                    context.blockModelBaker().getModel(BLOCK_BLOCK).getTopTransforms(),
                    machine);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(BLOCK_BLOCK);
        }
    }
}
