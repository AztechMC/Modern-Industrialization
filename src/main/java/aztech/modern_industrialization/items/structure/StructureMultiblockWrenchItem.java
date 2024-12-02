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
package aztech.modern_industrialization.items.structure;

import aztech.modern_industrialization.MIComponents;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.blocks.structure.controller.StructureMultiblockControllerBlockEntity;
import aztech.modern_industrialization.blocks.structure.member.StructureMultiblockMemberBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.structure.StructureNBTMode;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StructureMultiblockWrenchItem extends Item {
    public StructureMultiblockWrenchItem(Properties properties) {
        super(properties
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .component(MIComponents.STRUCTURE_WRENCH_MODE, StructureNBTMode.IGNORE)
                .component(MIComponents.STRUCTURE_WRENCH_SELECTION, StructureWrenchSelection.EMPTY));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack ignored, UseOnContext context) {
        boolean isClientSide = context.getLevel().isClientSide();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        boolean isShiftKeyDown = player.isShiftKeyDown();
        ItemStack stack = player.getItemInHand(context.getHand());
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (blockEntity != null) {
            boolean isStructureBlock = blockEntity instanceof StructureMultiblockControllerBlockEntity
                    || blockEntity instanceof StructureMultiblockMemberBlockEntity;
            if (isStructureBlock) {
                if (blockEntity instanceof StructureMultiblockControllerBlockEntity controller) {
                    if (isShiftKeyDown) {
                        if (!isClientSide) {
                            var mode = stack.getOrDefault(MIComponents.STRUCTURE_WRENCH_MODE, StructureNBTMode.IGNORE);
                            var selection = stack.getOrDefault(MIComponents.STRUCTURE_WRENCH_SELECTION, StructureWrenchSelection.EMPTY);
                            var positions = selection.positions();
                            if (mode == StructureNBTMode.IGNORE) {
                                controller.getWeakNBTPositions().removeAll(positions);
                                controller.setIgnoreNBTPositions(positions);
                            } else if (mode == StructureNBTMode.WEAK) {
                                controller.getIgnoreNBTPositions().removeAll(positions);
                                controller.setWeakNBTPositions(selection.positions());
                            } else {
                                return InteractionResult.sidedSuccess(isClientSide);
                            }
                            controller.sync();
                            controller.setChanged();
                            player.displayClientMessage(MIText.StructureMultiblockWrenchApplied.text(), true);
                        }
                    } else {
                        // TODO SWEDZ: load the controller settings for this wrench's mode into the wrench
                    }
                    return InteractionResult.sidedSuccess(isClientSide);
                }
            } else if (!isShiftKeyDown) {
                if (!isClientSide) {
                    var selection = stack.getOrDefault(MIComponents.STRUCTURE_WRENCH_SELECTION, StructureWrenchSelection.EMPTY);
                    BlockPos pos = blockEntity.getBlockPos();
                    if (selection.contains(pos)) {
                        selection = selection.remove(pos);
                        player.displayClientMessage(MIText.StructureMultiblockWrenchRemoved.text(pos.toShortString()), true);
                    } else {
                        selection = selection.add(pos);
                        player.displayClientMessage(MIText.StructureMultiblockWrenchAdded.text(pos.toShortString()), true);
                    }
                    stack.set(MIComponents.STRUCTURE_WRENCH_SELECTION, selection);
                }
                return InteractionResult.sidedSuccess(isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown()) {
            stack.set(MIComponents.STRUCTURE_WRENCH_SELECTION, StructureWrenchSelection.EMPTY);
            player.displayClientMessage(MIText.StructureMultiblockWrenchCleared.text(), true);
        } else {
            StructureNBTMode currentMode = stack.getOrDefault(MIComponents.STRUCTURE_WRENCH_MODE, StructureNBTMode.IGNORE);
            StructureNBTMode newMode = currentMode == StructureNBTMode.IGNORE ? StructureNBTMode.WEAK : StructureNBTMode.IGNORE;
            stack.set(MIComponents.STRUCTURE_WRENCH_MODE, newMode);
            player.displayClientMessage(MIText.StructureMultiblockWrenchChangedMode.text(newMode.textTooltip()), true);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        var nbtMode = stack.getOrDefault(MIComponents.STRUCTURE_WRENCH_MODE, StructureNBTMode.IGNORE);
        tooltipComponents.add(MIText.StructureMultiblockNBTMode.text(nbtMode.textTooltip().withStyle(MITooltips.HIGHLIGHT_STYLE))
                .withStyle(MITooltips.DEFAULT_STYLE));

        var selection = stack.get(MIComponents.STRUCTURE_WRENCH_SELECTION);
        if (selection != null && !selection.positions().isEmpty()) {
            tooltipComponents.add(MIText.StructureMultiblockWrenchTooltip.text(selection.positions().size()));
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !newStack.is(this) || slotChanged;
    }
}
