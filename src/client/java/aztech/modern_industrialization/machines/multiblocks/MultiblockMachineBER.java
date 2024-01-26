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

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.MachineBlockEntityRenderer;
import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class MultiblockMachineBER extends MachineBlockEntityRenderer<MultiblockMachineBlockEntity> {
    public MultiblockMachineBER(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(MultiblockMachineBlockEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vcp, int light, int overlay) {
        super.render(be, tickDelta, matrices, vcp, light, overlay);

        // Only render if holding a wrench AND if the shape is not valid.
        super.render(be, tickDelta, matrices, vcp, light, overlay);
        boolean drawHighlights = isHoldingWrench() && !be.isShapeValid();
        HatchType hatchType = getHeldHatchType();
        if (drawHighlights || hatchType != null) {
            ShapeMatcher matcher = new ShapeMatcher(be.getLevel(), be.getBlockPos(), be.getOrientation().facingDirection, be.getActiveShape());

            for (BlockPos pos : matcher.getPositions()) {
                matrices.pushPose();
                matrices.translate(pos.getX() - be.getBlockPos().getX(), pos.getY() - be.getBlockPos().getY(), pos.getZ() - be.getBlockPos().getZ());

                HatchFlags hatchFlag = matcher.getHatchFlags(pos);
                if (hatchType != null) {
                    if (MIConfig.getConfig().enableHatchPlacementOverlay && hatchFlag != null && hatchFlag.allows(hatchType)) {
                        // Highlight placeable hatches in green
                        matrices.translate(-0.005, -0.005, -0.005);
                        matrices.scale(1.01f, 1.01f, 1.01f);
                        RenderHelper.drawOverlay(matrices, vcp, 111f / 256, 1, 111f / 256, 15728880, overlay);
                    }
                }
                if (drawHighlights) {
                    if (!matcher.matches(pos, be.getLevel(), null)) {
                        if (be.getLevel().getBlockState(pos).isAir()) {
                            // Enqueue state preview
                            MultiblockErrorHighlight.enqueueHighlight(pos, matcher.getSimpleMember(pos).getPreviewState());
                        } else {
                            // Enqueue red cube
                            MultiblockErrorHighlight.enqueueHighlight(pos, null);
                        }
                    }
                }

                matrices.popPose();
            }
        }
    }

    private static boolean isHoldingWrench() {
        Player player = Minecraft.getInstance().player;
        return player.getMainHandItem().is(MITags.WRENCHES) || player.getOffhandItem().is(MITags.WRENCHES);
    }

    @Nullable
    private static HatchType getHeldHatchType() {
        Player player = Minecraft.getInstance().player;
        HatchType mainHand = getHatchType(player.getMainHandItem());
        HatchType offHand = getHatchType(player.getOffhandItem());
        return mainHand == null ? offHand : mainHand;
    }

    @Nullable
    private static HatchType getHatchType(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem) item;
            if (blockItem.getBlock() instanceof MachineBlock) {
                MachineBlock block = (MachineBlock) blockItem.getBlock();
                BlockEntity be = block.newBlockEntity(new BlockPos(0, 0, 0), block.defaultBlockState());
                if (be instanceof HatchBlockEntity) {
                    HatchBlockEntity hatch = (HatchBlockEntity) be;
                    return hatch.getHatchType();
                }
            }
        }
        return null;
    }

    @Override
    public boolean shouldRenderOffScreen(MultiblockMachineBlockEntity pBlockEntity) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(MultiblockMachineBlockEntity blockEntity) {
        return INFINITE_EXTENT_AABB;
    }
}
