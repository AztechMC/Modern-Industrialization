package aztech.modern_industrialization.blocks.tank;

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

public class TankRenderer extends BlockEntityRenderer<TankBlockEntity> {
    public TankRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TankBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            int overlay) {
        if (!entity.fluid.isEmpty() && entity.amount > 0) {
            List<FluidRenderFace> faces = new ArrayList<>();
            double fillFraction = (double) entity.amount / entity.capacity;
            FluidRenderFace.appendCuboid(0.01, 0.01, 0.01, 0.99, fillFraction - 0.01, 0.99, 1, EnumSet.allOf(Direction.class), faces);
            entity.fluid.withAmount(FluidAmount.ONE).render(faces, vertexConsumers, matrices);
        }
    }
}
