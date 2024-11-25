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
package aztech.modern_industrialization.blocks.structure.member;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.structure.StructureMultiblockInputFormatters;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class StructureMultiblockMemberBlockItem extends BlockItem {
    public StructureMultiblockMemberBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static StructureMemberMode getMode(ItemStack stack) {
        if (!stack.is(MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.asItem())) {
            return null;
        }

        String modeId = stack.has(DataComponents.BLOCK_ENTITY_DATA)
                ? stack.get(DataComponents.BLOCK_ENTITY_DATA).copyTag().getString("mode")
                : "";
        if (!modeId.isEmpty()) {
            return StructureMemberMode.valueOf(modeId.toUpperCase(Locale.ROOT));
        }
        return StructureMemberMode.SIMPLE;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (stack.has(DataComponents.BLOCK_ENTITY_DATA)) {
            StructureMemberMode mode = getMode(stack);

            CompoundTag beTag = stack.get(DataComponents.BLOCK_ENTITY_DATA).copyTag();

            var name = beTag.getString("name");
            var preview = StructureMultiblockInputFormatters.preview(beTag.getString("preview"));
            var members = StructureMultiblockInputFormatters.members(beTag.getString("members"));
            var casing = StructureMultiblockInputFormatters.casing(beTag.getString("casing"));
            var hatchFlags = StructureMultiblockInputFormatters.hatchFlags(beTag.getString("hatch_flags"));

            return Optional.of(new TooltipData(mode, name, preview, members, casing, hatchFlags));
        }

        return Optional.empty();
    }

    public record TooltipData(StructureMemberMode mode, String name, BlockState preview, List<StructureMemberTest> members, MachineCasing casing,
            HatchFlags hatchFlags) implements TooltipComponent {
    }
}
