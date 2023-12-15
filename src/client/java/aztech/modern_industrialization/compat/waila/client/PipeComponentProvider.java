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
package aztech.modern_industrialization.compat.waila.client;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.compat.waila.client.component.CenteredTextComponent;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeVoxelShape;
import aztech.modern_industrialization.pipes.item.ItemNetwork;
import aztech.modern_industrialization.util.FluidHelper;
import java.util.Objects;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaConfig;
import mcp.mobius.waila.api.WailaConstants;
import mcp.mobius.waila.api.component.BarComponent;
import mcp.mobius.waila.api.component.PairComponent;
import mcp.mobius.waila.api.component.SpriteBarComponent;
import mcp.mobius.waila.api.component.WrappedComponent;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Overrides the name of the pipes in Waila to prevent "block.modern_industrialization.pipe" being displayed.
 */
public class PipeComponentProvider implements IBlockComponentProvider {

    private @Nullable PipeVoxelShape getHitShape(IBlockAccessor accessor) {
        PipeBlockEntity pipe = accessor.getBlockEntity();
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
    public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        PipeVoxelShape shape = getHitShape(accessor);
        if (shape != null) {
            Component text = IWailaConfig.get().getFormatter().blockName(
                    I18n.get(MIPipes.INSTANCE.getPipeItem(shape.type).getDescriptionId()));
            tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, text);
        }
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        PipeVoxelShape shape = getHitShape(accessor);
        if (shape != null) {
            CompoundTag tag = accessor.getData().raw().getCompound(shape.type.getIdentifier().toString());

            if (tag.contains("fluid")) {
                FluidVariant fluid = FluidVariant.fromNbt(tag.getCompound("fluid"));
                double stored = tag.getLong("amount") / 81.0;
                double capacity = tag.getInt("capacity") / 81.0;
                double transfer = tag.getLong("transfer") / 81.0;
                double maxTransfer = tag.getLong("maxTransfer") / 81.0;

                if (fluid.isBlank()) {
                    // Show "Empty"
                    tooltip.addLine(MIText.Empty.text());
                } else {
                    var fluidName = FluidHelper.getFluidName(fluid, true);
                    var sprite = FluidVariantRendering.getSprite(fluid);
                    int color = FluidVariantRendering.getColor(fluid);

                    if (sprite == null) {
                        sprite = Objects.requireNonNull(FluidVariantRendering.getSprite(FluidVariant.of(Fluids.WATER)));
                    }

                    // Total fluid
                    tooltip.addLine(new PairComponent(
                            new WrappedComponent(MIText.NetworkAmount.text()),
                            new SpriteBarComponent(MIWailaClientPlugin.ratio(stored, capacity), sprite.atlasLocation(),
                                    sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), 16, 16, color,
                                    Component.literal(MIWailaClientPlugin.fraction(stored, capacity) + " mB"))));

                    // Which fluid
                    tooltip.addLine(new PairComponent(
                            new WrappedComponent(MIText.NetworkFluid.text()),
                            new CenteredTextComponent(fluidName)));

                    // Transfer rate
                    tooltip.addLine(new PairComponent(
                            new WrappedComponent(MIText.NetworkTransfer.text()),
                            new SpriteBarComponent(MIWailaClientPlugin.ratio(transfer, maxTransfer), sprite.atlasLocation(),
                                    sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), 16, 16, color,
                                    Component.literal(MIWailaClientPlugin.fraction(transfer, maxTransfer) + " mB/t"))));
                }
            }

            if (tag.contains("eu")) {
                long stored = tag.getLong("eu");
                long capacity = tag.getLong("maxEu");
                long transfer = tag.getLong("transfer");
                long maxTransfer = tag.getLong("maxTransfer");

                // Total EU
                tooltip.addLine(new PairComponent(
                        new WrappedComponent(MIText.NetworkEnergy.text()),
                        new BarComponent(MIWailaClientPlugin.ratio(stored, capacity), MIWailaClientPlugin.ENERGY_COLOR,
                                MIWailaClientPlugin.fraction(stored, capacity) + " EU")));

                // Voltage tier
                tooltip.addLine(new PairComponent(
                        new WrappedComponent(MIText.NetworkTier.text()),
                        new CenteredTextComponent(
                                MIPipes.ELECTRICITY_PIPE_TIER.get(MIPipes.INSTANCE.getPipeItem(shape.type)).longEnglishName()
                                        .withStyle(MITooltips.NUMBER_TEXT))));

                // EU/t
                tooltip.addLine(new PairComponent(
                        new WrappedComponent(MIText.NetworkTransfer.text()),
                        new BarComponent(
                                MIWailaClientPlugin.ratio(transfer, maxTransfer), MIWailaClientPlugin.ENERGY_COLOR,
                                MIWailaClientPlugin.fraction(transfer, maxTransfer) + " EU/t")));
            }

            if (tag.contains("items")) {
                long items = tag.getLong("items");
                int pulse = tag.getInt("pulse");

                double delay = (ItemNetwork.TICK_RATE - pulse) / 20.0;
                double maxDelay = ItemNetwork.TICK_RATE / 20.0;

                // Delay
                tooltip.addLine(new PairComponent(
                        new WrappedComponent(MIText.NetworkDelay.text()),
                        new BarComponent(MIWailaClientPlugin.ratio(delay, maxDelay), 0xFFFFD14A,
                                MIWailaClientPlugin.fraction(delay, maxDelay) + " s")));

                // Moved items
                tooltip.addLine(new PairComponent(
                        new WrappedComponent(MIText.NetworkMovedItems.text()),
                        new CenteredTextComponent(Component.literal("" + items).withStyle(MITooltips.NUMBER_TEXT))));
            }
        }
    }

}
