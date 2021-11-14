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
package aztech.modern_industrialization.machines.multiblocks;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.MachineBlockEntityRenderer;
import aztech.modern_industrialization.util.RenderHelper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class MultiblockMachineBER extends MachineBlockEntityRenderer<MultiblockMachineBlockEntity> {
    public MultiblockMachineBER(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(MultiblockMachineBlockEntity be, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay) {
        super.render(be, tickDelta, matrices, vcp, light, overlay);

        // Only render if holding a wrench AND if the shape is not valid.
        super.render(be, tickDelta, matrices, vcp, light, overlay);
        boolean drawHighlights = isHoldingWrench() && !be.isShapeValid();
        HatchType hatchType = getHeldHatchType();
        if (drawHighlights || hatchType != null) {
            ShapeMatcher matcher = new ShapeMatcher(be.getWorld(), be.getPos(), be.getOrientation().facingDirection, be.getActiveShape());

            for (BlockPos pos : matcher.getPositions()) {
                matrices.push();
                matrices.translate(pos.getX() - be.getPos().getX(), pos.getY() - be.getPos().getY(), pos.getZ() - be.getPos().getZ());

                HatchFlags hatchFlag = matcher.getHatchFlags(pos);
                if (hatchType != null) {
                    if (hatchFlag != null && hatchFlag.allows(hatchType)) {
                        // Highlight placeable hatches in green
                        matrices.translate(-0.005, -0.005, -0.005);
                        matrices.scale(1.01f, 1.01f, 1.01f);
                        RenderHelper.drawOverlay(matrices, vcp, 111f / 256, 1, 111f / 256, 15728880, overlay);
                    }
                }
                if (drawHighlights) {
                    if (!matcher.matches(pos, be.getWorld(), null)) {
                        if (be.getWorld().getBlockState(pos).isAir()) {
                            // Enqueue state preview
                            MultiblockErrorHighlight.enqueueHighlight(pos, matcher.getSimpleMember(pos).getPreviewState());
                        } else {
                            // Enqueue red cube
                            MultiblockErrorHighlight.enqueueHighlight(pos, null);
                        }
                    }
                }

                matrices.pop();
            }
        }
    }

    private static boolean isHoldingWrench() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        return player.getMainHandStack().isIn(ModernIndustrialization.WRENCHES) || player.getOffHandStack().isIn(ModernIndustrialization.WRENCHES);
    }

    @Nullable
    private static HatchType getHeldHatchType() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        HatchType mainHand = getHatchType(player.getMainHandStack());
        HatchType offHand = getHatchType(player.getOffHandStack());
        return mainHand == null ? offHand : mainHand;
    }

    @Nullable
    private static HatchType getHatchType(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem) item;
            if (blockItem.getBlock() instanceof MachineBlock) {
                MachineBlock block = (MachineBlock) blockItem.getBlock();
                BlockEntity be = block.createBlockEntity(new BlockPos(0, 0, 0), block.getDefaultState());
                if (be instanceof HatchBlockEntity) {
                    HatchBlockEntity hatch = (HatchBlockEntity) be;
                    return hatch.getHatchType();
                }
            }
        }
        return null;
    }
}
