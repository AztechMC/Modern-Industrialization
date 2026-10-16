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

package aztech.modern_industrialization.compat.kubejs.machine;

import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchTypes;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface ShapeTemplateHelper {
    default ShapeTemplate.LayeredBuilder layeredShape(String hatchCasing, String[][] layers) {
        return new ShapeTemplate.LayeredBuilder(MachineCasings.get(hatchCasing), layers);
    }

    default ShapeTemplate.Builder startShape(String hatchCasing) {
        return new ShapeTemplate.Builder(MachineCasings.get(hatchCasing));
    }

    default SimpleMember memberOfBlock(String blockId) {
        return SimpleMember.forBlockId(ResourceLocation.parse(blockId));
    }

    default SimpleMember memberOfBlockTag(String blockId, String blockTag) {
        return SimpleMember.forBlockTagId(ResourceLocation.parse(blockId), ResourceLocation.parse(blockTag));
    }

    default SimpleMember memberOfBlockState(String blockId, Function<BlockState, BlockState> modifier) {
        return SimpleMember.forBlockStateId(blockId, modifier);
    }

    default SimpleMember memberOfCustom(String previewBlockId,
            Function<BlockState, BlockState> previewBlockState,
            BiPredicate<BlockState, BlockEntity> predicate,
            Ingredient itemPreview,
            Component... tooltip) {
        return new SimpleMember() {
            @Override
            public boolean matchesState(BlockState state, @Nullable BlockEntity blockEntity) {
                return predicate.test(state, blockEntity);
            }

            @Override
            public BlockState getPreviewState() {
                var block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(previewBlockId)).defaultBlockState();
                return previewBlockState.apply(block);
            }

            @Override
            public Ingredient getItemPreviewState(RegistryAccess registries) {
                return itemPreview;
            }

            @Override
            public List<Component> getTooltipPreview(RegistryAccess registries) {
                return Arrays.asList(tooltip);
            }
        };
    }

    default HatchFlags noHatch() {
        return HatchFlags.NO_HATCH;
    }

    default HatchFlags hatchOf(String... hatches) {
        var builder = new HatchFlags.Builder();
        for (String hatch : hatches) {
            builder.with(HatchTypes.get(hatch));
        }
        return builder.build();
    }
}
