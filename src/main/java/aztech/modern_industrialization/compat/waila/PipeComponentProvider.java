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
package aztech.modern_industrialization.compat.waila;

import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeVoxelShape;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.List;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Overrides the name of the pipes in Waila to prevent
 * "block.modern_industrialization.pipe" being displayed.
 */
public class PipeComponentProvider implements IComponentProvider {
    private @Nullable PipeVoxelShape getHitShape(IDataAccessor accessor) {
        PipeBlockEntity pipe = (PipeBlockEntity) accessor.getBlockEntity();
        Vec3 hitPos = accessor.getHitResult().getLocation();
        BlockPos blockPos = accessor.getPosition();
        for (PipeVoxelShape partShape : pipe.getPartShapes()) {
            Vec3 posInBlock = hitPos.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            for (AABB box : partShape.shape.toAabbs()) {
                // move slightly towards box center
                Vec3 dir = box.getCenter().subtract(posInBlock).normalize().scale(1e-4);
                if (box.contains(posInBlock.add(dir))) {
                    return partShape;
                }
            }
        }
        return null;
    }

    @Override
    public void appendHead(List<Component> tooltip, IDataAccessor accessor, IPluginConfig config) {
        PipeVoxelShape shape = getHitShape(accessor);
        if (shape != null) {
            Component text = new TranslatableComponent(MIPipes.INSTANCE.getPipeItem(shape.type).getDescriptionId())
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
            tooltip.set(0, text);
        }
    }

    @Override
    public void appendBody(List<Component> tooltip, IDataAccessor accessor, IPluginConfig config) {
        PipeVoxelShape shape = getHitShape(accessor);
        if (shape != null) {
            CompoundTag tag = accessor.getServerData().getCompound(shape.type.getIdentifier().toString());
            Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);

            if (tag.contains("fluid")) {
                FluidVariant fluid = NbtHelper.getFluidCompatible(tag, "fluid");
                long amount = tag.getLong("amount");
                int capacity = tag.getInt("capacity");
                tooltip.addAll(FluidHelper.getTooltipForFluidStorage(fluid, amount, capacity));

            }

            if (tag.contains("eu")) {
                long eu = tag.getLong("eu");
                long maxEu = tag.getLong("maxEu");
                String tier = tag.getString("tier");
                tooltip.add(new TranslatableComponent("text.modern_industrialization.cable_tier_" + tier));
                tooltip.add(new TranslatableComponent("text.modern_industrialization.energy_bar", eu, maxEu).setStyle(style));
            }
        }
    }
}
