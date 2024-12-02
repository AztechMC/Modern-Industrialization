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
package aztech.modern_industrialization.items;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIComponents;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.blocks.structure.controller.StructureMultiblockControllerBlockEntity;
import aztech.modern_industrialization.blocks.structure.member.StructureMultiblockMemberBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.structure.MIStructureTemplateManager;
import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = MI.ID)
public class StructureMultiblockWrenchHighlight {
    private static final MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(new ByteBufferBuilder(128));

    @SubscribeEvent
    private static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        List<BlockPos> positions = getWrenchSelectedPositions(player);
        if (!positions.isEmpty()) {
            RenderSystem.clear(256, Minecraft.ON_OSX);
            var poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.mulPose(event.getModelViewMatrix());
            for (BlockPos pos : positions) {
                poseStack.pushPose();
                Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                double x = pos.getX() - cameraPos.x;
                double y = pos.getY() - cameraPos.y;
                double z = pos.getZ() - cameraPos.z;
                poseStack.translate(x - 0.005, y - 0.005, z - 0.005);
                poseStack.scale(1.01f, 1.01f, 1.01f);
                RenderHelper.drawOverlay(poseStack, immediate, 1, 111f / 256, 1, 15728880, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
            poseStack.popPose();
            immediate.endBatch();
        }
    }

    public static void onRenderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && isHoldingWrench(mc.player) && mc.hitResult instanceof BlockHitResult hitResult) {
            Level level = mc.level;
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof StructureMultiblockControllerBlockEntity || blockEntity instanceof StructureMultiblockMemberBlockEntity) {
                ItemStack stack = new ItemStack(state.getBlock().asItem());
                CompoundTag tag = MIStructureTemplateManager.maybeTag(blockEntity);
                stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
                guiGraphics.renderTooltip(mc.font, stack, mc.getWindow().getGuiScaledWidth() / 2, mc.getWindow().getGuiScaledHeight() / 2);
            }
        }
    }

    private static List<BlockPos> getWrenchSelectedPositions(Player player) {
        List<BlockPos> positions = new ArrayList<>();
        positions.addAll(getWrenchSelectedPositions(player.getMainHandItem()));
        positions.addAll(getWrenchSelectedPositions(player.getOffhandItem()));
        return positions;
    }

    private static List<BlockPos> getWrenchSelectedPositions(ItemStack stack) {
        return stack.is(MIItem.STRUCTURE_MULTIBLOCK_WRENCH.asItem()) && stack.has(MIComponents.STRUCTURE_WRENCH_SELECTION)
                ? stack.get(MIComponents.STRUCTURE_WRENCH_SELECTION).positions()
                : List.of();
    }

    private static boolean isHoldingWrench(Player player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).is(MIItem.STRUCTURE_MULTIBLOCK_WRENCH.asItem())
                || player.getItemInHand(InteractionHand.OFF_HAND).is(MIItem.STRUCTURE_MULTIBLOCK_WRENCH.asItem());
    }
}
