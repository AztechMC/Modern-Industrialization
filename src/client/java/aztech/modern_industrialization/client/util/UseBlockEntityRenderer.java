package aztech.modern_industrialization.client.util;

import aztech.modern_industrialization.MI;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public record UseBlockEntityRenderer() implements SpecialModelRenderer<ItemResource> {
    @Override
    public void submit(@Nullable ItemResource resource, ItemDisplayContext type, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        Objects.requireNonNull(resource, "UseBlockEntityRenderer resource must not be null");
        if (!(resource.getItem() instanceof BlockItem blockItem)) {
            throw new IllegalArgumentException("Stack must be a block item!");
        }
        if (!(blockItem.getBlock() instanceof EntityBlock entityBlock)) {
            throw new IllegalArgumentException("Block must be an entity block!");
        }

        var fakeBlockEntity = entityBlock.newBlockEntity(BlockPos.ZERO, blockItem.getBlock().defaultBlockState());
        Objects.requireNonNull(fakeBlockEntity);
        fakeBlockEntity.applyComponentsFromItemStack(resource.toStack());

        // Render additional data using the block entity renderer
        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(fakeBlockEntity);
        var renderState = Objects.requireNonNull(renderer).createRenderState();
        renderer.extractRenderState(fakeBlockEntity, renderState, 0, Vec3.ZERO, null);
        renderState.lightCoords = lightCoords;
        renderer.submit(renderState, poseStack, submitNodeCollector, new CameraRenderState());
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {}

    @Override
    public ItemResource extractArgument(ItemStack stack) {
        return ItemResource.of(stack);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final Identifier TYPE_ID = MI.id("use_block_entity_renderer");

        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public SpecialModelRenderer<?> bake(BakingContext context) {
            return new UseBlockEntityRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return CODEC;
        }
    }
}
