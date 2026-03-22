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

package aztech.modern_industrialization.client.compat.jade;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.compat.jade.server.MIJadeCommonPlugin;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeVoxelShape;
import aztech.modern_industrialization.pipes.item.ItemNetwork;
import aztech.modern_industrialization.util.FluidHelper;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.JadeUI;

/**
 * Overrides the name of the pipes in Waila to prevent "block.modern_industrialization.pipe" being displayed.
 */
public class PipeComponentProvider implements IBlockComponentProvider {
    @Override
    public Identifier getUid() {
        return MI.id("pipe");
    }

    private @Nullable PipeVoxelShape getHitShape(BlockAccessor accessor) {
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
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        PipeVoxelShape shape = getHitShape(accessor);
        if (shape != null) {
            CompoundTag tag = accessor.getServerData().getCompoundOrEmpty(shape.type.getIdentifier().toString());
            if (tag.contains("fluid")) {
                FluidResource fluid = tag.read("fluid", FluidResource.OPTIONAL_CODEC).orElse(FluidResource.EMPTY);
                long stored = tag.getLongOr("amount", 0);
                long capacity = tag.getIntOr("capacity", 0);
                long transfer = tag.getLongOr("transfer", 0);
                long maxTransfer = tag.getLongOr("maxTransfer", 0);

                var fluidName = IDisplayHelper.get().stripColor(FluidHelper.getFluidName(fluid, true));

                // Stored fluid
                var storedText = MIJadeClientPlugin.getUnicodeMillibuckets(stored, true);
                var capacityText = MIJadeClientPlugin.getUnicodeMillibuckets(capacity, true);

                Component text;
                if (fluid.isEmpty()) {
                    text = Component.translatable("jade.fluid", MIText.Empty.text(), Component.literal(capacityText).withStyle(ChatFormatting.GRAY));
                } else if (accessor.showDetails()) {
                    text = Component.translatable("jade.fluid2", fluidName.withStyle(ChatFormatting.WHITE),
                            Component.literal(storedText).withStyle(ChatFormatting.WHITE), capacityText).withStyle(ChatFormatting.GRAY);
                } else {
                    text = Component.translatable("jade.fluid", fluidName, storedText);
                }
                // TODO 26.1
//                var progressStyle = JadeUI.progressStyle().overlay(JadeUI.fluid(MIJadeCommonPlugin.fluidStack(fluid, stored)));
//
//                tooltip.add(JadeUI.progress(
//                        MIJadeClientPlugin.ratio(stored, capacity),
//                        text,
//                        progressStyle,
//                        BoxStyle.nestedBox(),
//                        true));

                // Transfer rate
                if (!fluid.isEmpty()) {
                    var transferText = MIJadeClientPlugin.getUnicodeMillibuckets(transfer, true);
                    var maxTransferText = MIJadeClientPlugin.getUnicodeMillibuckets(maxTransfer, true);

                    Component maxText;
                    if (accessor.showDetails()) {
                        maxText = MIJadeClientPlugin.textAndRatio(MIText.NetworkTransfer.text(), transferText, maxTransferText);
                    } else {
                        maxText = MIText.NetworkTransfer.text()
                                .append(" ")
                                .append(Component.literal(transferText));
                    }

                    // TODO 26.1
//                    tooltip.add(JadeUI.progress(
//                            MIJadeClientPlugin.ratio(transfer, maxTransfer),
//                            maxText,
//                            progressStyle,
//                            BoxStyle.nestedBox(),
//                            true));
                }
            }

            if (tag.contains("eu")) {
                long stored = tag.getLongOr("eu", 0);
                long capacity = tag.getLongOr("maxEu", 0);
                long transfer = tag.getLongOr("transfer", 0);
                long maxTransfer = tag.getLongOr("maxTransfer", 0);

                // Total EU
                // TODO: fix spacing between EU and the rest (another day)
                // TODO @6.1
//                tooltip.add(JadeUI.progress(
//                        MIJadeClientPlugin.ratio(stored, capacity),
//                        Component.literal("")
//                                .append(Component.literal(IDisplayHelper.get().humanReadableNumber(stored, " EU", false))
//                                        .withStyle(ChatFormatting.WHITE))
//                                .append(" / ")
//                                .append(Component.literal(IDisplayHelper.get().humanReadableNumber(capacity, " EU", false)))
//                                .withStyle(ChatFormatting.GRAY),
//                        JadeUI.progressStyle().color(-5636096, -10092544).textColor(-1),
//                        BoxStyle.nestedBox(),
//                        true));

                // Voltage tier
                tooltip.add(
                        List.of(
                                JadeUI.text(MIText.NetworkTier.text().withStyle(ChatFormatting.WHITE)),
                                JadeUI.spacer(10, 0),
                                JadeUI.text(MIPipes.ELECTRICITY_PIPE_TIER.get(MIPipes.INSTANCE.getPipeItem(shape.type).type).longEnglishName()
                                        .withStyle(MITooltips.NUMBER_TEXT))
                                // TODO 26.1
                                        /*.align(IElement.Align.CENTER)*/));

                // EU/t
                // TODO: 26.1
//                tooltip.add(JadeUI.progress(
//                        MIJadeClientPlugin.ratio(transfer, maxTransfer),
//                        MIJadeClientPlugin.textAndRatio(
//                                MIText.NetworkTransfer.text(),
//                                String.valueOf(IDisplayHelper.get().humanReadableNumber(transfer, "", false)),
//                                String.valueOf(IDisplayHelper.get().humanReadableNumber(maxTransfer, " EU/t", false))),
//                        JadeUI.progressStyle().color(-5636096, -10092544).textColor(-1),
//                        BoxStyle.nestedBox(),
//                        true));
            }

            if (tag.contains("items")) {
                long items = tag.getLongOr("items", 0);
                int pulse = tag.getIntOr("pulse", 0);

                double delay = (ItemNetwork.TICK_RATE - pulse) / 20.0;
                double maxDelay = ItemNetwork.TICK_RATE / 20.0;

                // Delay
                // TODO 26.1
//                tooltip.add(JadeUI.progress(
//                        MIJadeClientPlugin.ratio(delay, maxDelay),
//                        MIJadeClientPlugin.textAndRatio(
//                                MIText.NetworkDelay.text(),
//                                String.valueOf(IDisplayHelper.get().humanReadableNumber(delay + 0.3, "", false)),
//                                String.valueOf(IDisplayHelper.get().humanReadableNumber(maxDelay, " s", false))),
//                        JadeUI.progressStyle().color(0xFFFFD14A, 0xFFCCA73B).textColor(-1),
//                        BoxStyle.nestedBox(),
//                        false)
//                        .tag(MI.id("pipe/items/delay")) // tag to give the progress bar its own interpolation
//                );

                // Moved items
                tooltip.add(
                        List.of(
                                JadeUI.text(MIText.NetworkMovedItems.text().withStyle(ChatFormatting.WHITE)),
                                JadeUI.spacer(10, 0),
                                JadeUI.text(Component.literal("" + items).withStyle(MITooltips.NUMBER_TEXT))
                                // TODO 26.1
                                        /*.align(IElement.Align.CENTER)*/));
            }
        }
    }
}
