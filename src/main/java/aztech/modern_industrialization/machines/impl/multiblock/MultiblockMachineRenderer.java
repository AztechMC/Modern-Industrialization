/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.machines.impl.multiblock;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.impl.MachineBlock;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.util.RenderHelper;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class MultiblockMachineRenderer extends BlockEntityRenderer<MultiblockMachineBlockEntity> {
    public MultiblockMachineRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(MultiblockMachineBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vc, int light,
            int overlay) {
        HatchType handHatch = null;
        ItemStack handStack = MinecraftClient.getInstance().player.getMainHandStack();
        if (handStack.getItem() instanceof BlockItem) {
            BlockItem blockItem = (BlockItem) handStack.getItem();
            if (blockItem.getBlock() instanceof MachineBlock) {
                MachineBlockEntity be = ((MachineBlock) blockItem.getBlock()).factory.blockEntityConstructor.get();
                if (be instanceof HatchBlockEntity) {
                    handHatch = ((HatchBlockEntity) be).type;
                }
            }
        }
        boolean renderHints = !entity.ready && handStack.getItem().isIn(ModernIndustrialization.TAG_WRENCH);
        if (!renderHints && handHatch == null) {
            return;
        }
        MultiblockShape shape = entity.shapes.get(entity.selectedShape);
        for (Map.Entry<BlockPos, MultiblockShape.Entry> entry : shape.entries.entrySet()) {
            BlockPos worldPos = MultiblockShapes.toWorldPos(entry.getKey(), entity.getFacingDirection(), entity.getPos());
            matrices.push();
            matrices.translate(worldPos.getX() - entity.getPos().getX(), worldPos.getY() - entity.getPos().getY(),
                    worldPos.getZ() - entity.getPos().getZ());
            if (handHatch != null && entry.getValue().allowsHatch(handHatch)) {
                // Highlight placeable hatches in green
                matrices.translate(-0.005, -0.005, -0.005);
                matrices.scale(1.01f, 1.01f, 1.01f);
                RenderHelper.drawOverlay(matrices, vc, 111f / 256, 1, 111f / 256, 15728880, overlay);
            } else if (renderHints && !entry.getValue().matches(entity.getWorld(), worldPos)) {
                if (entity.getWorld().getBlockState(worldPos).isAir()) {
                    // Draw hologram
                    matrices.translate(0.25, 0.25, 0.25);
                    matrices.scale(0.5f, 0.5f, 0.5f);
                    MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(entry.getValue().getPreviewState(), matrices, vc,
                            15728880, overlay);
                } else {
                    // Highlight in red
                    matrices.translate(-0.005, -0.005, -0.005);
                    matrices.scale(1.01f, 1.01f, 1.01f);
                    RenderHelper.drawOverlay(matrices, vc, 1, 50f / 256, 50f / 256, 15728880, overlay);
                }
            }
            matrices.pop();
        }
    }
}
