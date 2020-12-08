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

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeVoxelShape;
import java.util.List;

import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.NbtHelper;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * Overrides the name of the pipes in Waila to prevent
 * "block.modern_industrialization.pipe" being displayed.
 */
public class PipeComponentProvider implements IComponentProvider {
    private @Nullable PipeVoxelShape getHitShape(IDataAccessor accessor) {
        PipeBlockEntity pipe = (PipeBlockEntity) accessor.getBlockEntity();
        Vec3d hitPos = accessor.getHitResult().getPos();
        BlockPos blockPos = accessor.getPosition();
        for (PipeVoxelShape partShape : pipe.getPartShapes()) {
            Vec3d posInBlock = hitPos.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            for (Box box : partShape.shape.getBoundingBoxes()) {
                // move slightly towards box center
                Vec3d dir = box.getCenter().subtract(posInBlock).normalize().multiply(1e-4);
                if (box.contains(posInBlock.add(dir))) {
                    return partShape;
                }
            }
        }
        return null;
    }

    @Override
    public void appendHead(List<Text> tooltip, IDataAccessor accessor, IPluginConfig config) {
        PipeVoxelShape shape = getHitShape(accessor);
        if (shape != null) {
            Text text = new TranslatableText(MIPipes.INSTANCE.getPipeItem(shape.type).getTranslationKey())
                    .setStyle(Style.EMPTY.withColor(Formatting.WHITE));
            tooltip.set(0, text);
        }
    }

    @Override
    public void appendBody(List<Text> tooltip, IDataAccessor accessor, IPluginConfig config) {
        PipeVoxelShape shape = getHitShape(accessor);
        if (shape != null) {
            CompoundTag tag = accessor.getServerData().getCompound(shape.type.getIdentifier().toString());
            Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);

            if (tag.contains("fluid")) {
                Fluid fluid = NbtHelper.getFluid(tag, "fluid");
                int amount = tag.getInt("amount");
                int capacity = tag.getInt("capacity");
                if (fluid == Fluids.EMPTY) {
                    tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_empty"));
                } else {
                    tooltip.add(FluidHelper.getFluidName(fluid));
                    String quantity = amount + " / " + capacity;
                    tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_quantity", quantity).setStyle(style));
                }
            }

            if (tag.contains("eu")) {
                long eu = tag.getLong("eu");
                long maxEu = tag.getLong("maxEu");
                String tier = tag.getString("tier");
                tooltip.add(new TranslatableText("text.modern_industrialization.cable_tier_" + tier));
                tooltip.add(new TranslatableText("text.modern_industrialization.energy_bar", eu, maxEu).setStyle(style));
            }
        }
    }
}
