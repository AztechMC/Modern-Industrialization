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

import aztech.modern_industrialization.MIItem;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import org.apache.commons.lang3.mutable.MutableObject;

public class SteamDrillHighlight {
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        var player = Objects.requireNonNull(Minecraft.getInstance().player);

        if (!MIItem.STEAM_MINING_DRILL.is(player.getMainHandItem())) {
            return;
        }

        var area = SteamDrillItem.getArea(player.level(), player);
        if (area == null) {
            return;
        }

        var origin = area.center();
        MutableObject<VoxelShape> fullShape = new MutableObject<>(Shapes.empty());

        SteamDrillItem.forEachMineableBlock(player.level(), area, player, (blockPos, state) -> {
            // Call on Block directly to skip our BlockStateBase mixin...
            @SuppressWarnings("deprecation")
            float destroyProgress = state.getBlock().getDestroyProgress(state, player, player.level(), blockPos);
            if (destroyProgress <= 1e-9) {
                return;
            }

            var blockShape = state.getShape(player.level(), blockPos, CollisionContext.of(event.getCamera().getEntity()));
            blockShape = blockShape.move(blockPos.getX() - origin.getX(), blockPos.getY() - origin.getY(), blockPos.getZ() - origin.getZ());

            fullShape.setValue(Shapes.joinUnoptimized(fullShape.getValue(), blockShape, BooleanOp.OR));
        });

        if (fullShape.getValue() == Shapes.empty()) {
            return;
        }

        LevelRenderer.renderShape(
                event.getPoseStack(),
                event.getMultiBufferSource().getBuffer(RenderType.lines()),
                fullShape.getValue(),
                origin.getX() - event.getCamera().getPosition().x(),
                origin.getY() - event.getCamera().getPosition().y(),
                origin.getZ() - event.getCamera().getPosition().z(),
                0.0F,
                0.0F,
                0.0F,
                0.4F);
        event.setCanceled(true);
    }
}
