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

import aztech.modern_industrialization.MIComponents;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.pipes.impl.CamouflageHelper;
import aztech.modern_industrialization.pipes.impl.PipeBlock;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ConfigCardItem extends Item {
    public static final String TAG_SAVEDCONFIG = "savedconfig";

    public ConfigCardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext c) {
        var player = c.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            var hitState = c.getLevel().getBlockState(c.getClickedPos());
            var usedHand = c.getHand();

            if (hitState.getBlock() instanceof PipeBlock pipe) {
                var pipeUseResult = pipe.useItemOn(c.getItemInHand(), c.getLevel().getBlockState(c.getClickedPos()), c.getLevel(), c.getClickedPos(),
                        c.getPlayer(), c.getHand(), new BlockHitResult(c.getClickLocation(), c.getClickedFace(), c.getClickedPos(), c.isInside()));
                if (pipeUseResult.consumesAction()) {
                    return pipeUseResult.result();
                }
            }

            // Try to save block for pipe facade
            if (setCamouflage(player, usedHand, hitState)) {
                return InteractionResult.sidedSuccess(c.getLevel().isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public static boolean setCamouflage(Player player, InteractionHand usedHand, BlockState hitState) {
        if (CamouflageHelper.isReasonableCamouflage(hitState)) {
            player.getItemInHand(usedHand).remove(MIComponents.SAVED_CONFIG);
            player.getItemInHand(usedHand).set(MIComponents.CAMOUFLAGE, hitState);
            player.displayClientMessage(
                    MITooltips.line(MIText.ConfigCardSetCamouflage, Style.EMPTY).arg(hitState, MITooltips.BLOCK_STATE_PARSER).build(), true);
            return true;
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            player.getItemInHand(usedHand).remove(MIComponents.SAVED_CONFIG);
            player.getItemInHand(usedHand).remove(MIComponents.CAMOUFLAGE);
            player.displayClientMessage(MIText.ConfigCardCleared.text(), true);
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide());
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        var savedConfig = stack.get(MIComponents.SAVED_CONFIG);
        if (savedConfig != null) {
            var filterSize = savedConfig.filter().size();
            MutableComponent component;
            if (filterSize == 0) {
                component = MIText.ConfigCardConfiguredNoItems.text();
            } else {
                component = MIText.ConfigCardConfiguredItems.text(Component.literal("" + filterSize).setStyle(MITooltips.NUMBER_TEXT));
            }
            tooltipComponents.add(component.withStyle(MITooltips.DEFAULT_STYLE));
        }

        var camouflage = readCamouflage(stack);
        if (!camouflage.isAir()) {
            tooltipComponents
                    .add(MITooltips.line(MIText.ConfigCardConfiguredCamouflage, Style.EMPTY).arg(camouflage, MITooltips.BLOCK_STATE_PARSER).build());
        }
    }

    public static BlockState readCamouflage(ItemStack stack) {
        return stack.getOrDefault(MIComponents.CAMOUFLAGE, Blocks.AIR.defaultBlockState());
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var savedConfig = stack.get(MIComponents.SAVED_CONFIG);
        if (savedConfig != null) {
            var stacks = savedConfig.filter();
            return stacks.isEmpty() ? Optional.empty() : Optional.of(new TooltipData(stacks));
        }

        var camouflage = readCamouflage(stack);
        if (!camouflage.isAir()) {
            return Optional.of(new TooltipData(List.of(new ItemStack(camouflage.getBlock()))));
        }

        return Optional.empty();
    }

    public record TooltipData(List<ItemStack> filter) implements TooltipComponent {}
}
