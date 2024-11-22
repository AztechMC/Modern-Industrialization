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
package aztech.modern_industrialization.blocks.structure;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class StructureMultiblockFormatters {
    @Nullable
    public static MachineCasing casing(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        ResourceLocation casingId = null;
        if (input.contains(":")) {
            casingId = ResourceLocation.tryParse(input);
        } else if (ResourceLocation.isValidPath(input)) {
            casingId = MI.id(input);
        }
        if (casingId == null) {
            return null;
        }
        if (MachineCasings.registeredCasings.containsKey(casingId)) {
            return MachineCasings.get(casingId);
        }
        return null;
    }

    @Nullable
    public static BlockState preview(String inputPreview) {
        if (inputPreview == null || inputPreview.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
        }
        var registry = BuiltInRegistries.BLOCK.asLookup();

        try {
            var blockResult = BlockStateParser.parseForBlock(registry, inputPreview, true);
            return blockResult.blockState();
        } catch (CommandSyntaxException ignored) {
            return null;
        }
    }

    @Nullable
    public static List<Predicate<BlockState>> members(String inputMembers) {
        if (inputMembers == null || inputMembers.isEmpty()) {
            return new ArrayList<>();
        }
        var registry = BuiltInRegistries.BLOCK.asLookup();

        List<Predicate<BlockState>> predicates = new ArrayList<>();
        for (String part : inputMembers.split(";")) {
            if (part.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(part.substring(1));
                if (tagId == null) {
                    return null;
                }
                var tag = BlockTags.create(tagId);
                predicates.add(state -> state.is(tag));
            } else {
                try {
                    var blockResult = BlockStateParser.parseForBlock(registry, part, true);
                    predicates.add(state -> state == blockResult.blockState());
                } catch (CommandSyntaxException ignored) {
                    return null;
                }
            }
        }
        return predicates;
    }

    @Nullable
    public static HatchFlags hatchFlags(String input) {
        if (input == null || input.isEmpty()) {
            return HatchFlags.NO_HATCH;
        }
        HatchFlags.Builder builder = new HatchFlags.Builder();
        for (String part : input.split(";")) {
            try {
                builder.with(Integer.parseInt(part));
            } catch (NumberFormatException ignored) {
                try {
                    HatchType hatchType = HatchType.valueOf(part.toUpperCase(Locale.ROOT));
                    builder.with(hatchType);
                } catch (IllegalArgumentException ignored2) {
                    return null;
                }
            }
        }
        return builder.build();
    }

    private StructureMultiblockFormatters() {
    }
}
