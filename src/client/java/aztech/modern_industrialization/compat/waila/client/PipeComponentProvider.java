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
import aztech.modern_industrialization.compat.waila.client.component.BarComponent;
import aztech.modern_industrialization.compat.waila.client.component.CenteredTextComponent;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeVoxelShape;
import aztech.modern_industrialization.pipes.item.ItemNetwork;
import aztech.modern_industrialization.util.FluidHelper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import lol.bai.megane.api.provider.FluidInfoProvider;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.PairComponent;
import mcp.mobius.waila.api.component.WrappedComponent;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Overrides the name of the pipes in Waila to prevent
 * "block.modern_industrialization.pipe" being displayed.
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
            CompoundTag tag = accessor.getServerData().getCompound(shape.type.getIdentifier().toString());

            if (tag.contains("fluid")) {
                FluidVariant fluid = FluidVariant.fromNbt(tag.getCompound("fluid"));
                long stored = tag.getLong("amount");
                long capacity = tag.getInt("capacity");
                long transfer = tag.getLong("transfer");
                long maxTransfer = tag.getLong("maxTransfer");

                if (fluid.isBlank()) {
                    // Show "Empty"
                    tooltip.addLine(MIText.Empty.text());
                } else {
                    var fluidName = FluidHelper.getFluidName(fluid, true);
                    int color = FluidVariantRendering.getColor(fluid);

                    if (FabricLoader.getInstance().isModLoaded("megane-runtime")) {
                        color = improveFluidColor(accessor, color, fluid);
                    }

                    // Total fluid
                    tooltip.addLine(new PairComponent(
                            new WrappedComponent(MIText.NetworkAmount.text()),
                            new BarComponent(color, stored / 81.0, capacity / 81.0, "mb", false)));

                    // Which fluid
                    tooltip.addLine(new PairComponent(
                            new WrappedComponent(MIText.NetworkFluid.text()),
                            new CenteredTextComponent(
                                    fluidName)));

                    // Transfer rate
                    tooltip.addLine(new PairComponent(
                            new WrappedComponent(MIText.NetworkTransfer.text()),
                            new BarComponent(color, transfer / 81.0, maxTransfer / 81.0, "mb/t", false)));

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
                        new BarComponent(0x710C00, stored, capacity, "EU", false)));

                // Voltage tier
                tooltip.addLine(new PairComponent(
                        new WrappedComponent(MIText.NetworkTier.text()),
                        new CenteredTextComponent(
                                MIPipes.ELECTRICITY_PIPE_TIER.get(MIPipes.INSTANCE.getPipeItem(shape.type)).englishNameComponent
                                        .copy().withStyle(MITooltips.NUMBER_TEXT))));

                // EU/t
                tooltip.addLine(new PairComponent(
                        new WrappedComponent(MIText.NetworkTransfer.text()),
                        new BarComponent(0x710C00, transfer, maxTransfer, "EU/t", false)));
            }

            if (tag.contains("items")) {
                long items = tag.getLong("items");
                int pulse = tag.getInt("pulse");

                // Delay
                tooltip.addLine(new PairComponent(
                        new WrappedComponent(MIText.NetworkDelay.text()),
                        new BarComponent(0xFFD14A, (ItemNetwork.TICK_RATE - pulse) / 20.0, ItemNetwork.TICK_RATE / 20.0, "s", false)));

                // Moved items
                tooltip.addLine(new PairComponent(
                        new WrappedComponent(MIText.NetworkMovedItems.text()),
                        new CenteredTextComponent(Component.literal("" + items).withStyle(MITooltips.NUMBER_TEXT))));
            }
        }
    }

    /**
     * Try to find a better color if possible by reflecting into megane's internals ;-)
     */
    private static int improveFluidColor(IBlockAccessor accessor, int color, FluidVariant fluid) {
        try {
            Class<?> registrarClass = Class.forName("lol.bai.megane.runtime.registry.Registrar");
            Field fluidInfoField = registrarClass.getField("FLUID_INFO");
            fluidInfoField.setAccessible(true);
            Object registry = fluidInfoField.get(null);
            Method getMethod = registry.getClass().getMethod("get", Object.class);
            List<FluidInfoProvider<Fluid>> providers = (List<FluidInfoProvider<Fluid>>) getMethod.invoke(registry, fluid.getFluid());

            for (var provider : providers) {
                provider.setContext(accessor.getWorld(), accessor.getPosition(), accessor.getHitResult(), accessor.getPlayer(), fluid.getFluid());
                provider.setFluidInfoContext(fluid.getNbt());
                if (provider.hasFluidInfo()) {
                    return provider.getColor();
                }
            }
        } catch (Exception ignored) {
        }

        return color;
    }
}
