package aztech.modern_industrialization.blocks.creativetank;

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class CreativeTankRenderer extends BlockEntityRenderer<CreativeTankBlockEntity> {
    public CreativeTankRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(CreativeTankBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            int overlay) {
        if (!entity.fluid.isEmpty()) {
            List<FluidRenderFace> faces = new ArrayList<>();
            FluidRenderFace.appendCuboid(0.01, 0.01, 0.01, 0.99, 0.99, 0.99, 1, EnumSet.allOf(Direction.class), faces);
            entity.fluid.withAmount(FluidAmount.ONE).render(faces, vertexConsumers, matrices);
        }
    }
}
