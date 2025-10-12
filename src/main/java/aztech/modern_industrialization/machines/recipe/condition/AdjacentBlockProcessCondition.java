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

package aztech.modern_industrialization.machines.recipe.condition;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public record AdjacentBlockProcessCondition(Block block, RelativePosition relativePosition) implements MachineProcessCondition {
    static final MapCodec<AdjacentBlockProcessCondition> CODEC = RecordCodecBuilder.mapCodec(
            g -> g.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(c -> c.block),
                    StringRepresentable.fromEnum(RelativePosition::values).fieldOf("position").forGetter(c -> c.relativePosition))
                    .apply(g, AdjacentBlockProcessCondition::new));
    static final StreamCodec<RegistryFriendlyByteBuf, AdjacentBlockProcessCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.BLOCK),
            AdjacentBlockProcessCondition::block,
            ByteBufCodecs.fromCodec(StringRepresentable.fromEnum(RelativePosition::values)),
            AdjacentBlockProcessCondition::relativePosition,
            AdjacentBlockProcessCondition::new);

    public AdjacentBlockProcessCondition(Block block, String relativePosition) {
        this(block, RelativePosition.valueOf(relativePosition.toUpperCase(Locale.ROOT)));
    }

    @Override
    public boolean canProcessRecipe(Context context, MachineRecipe recipe) {
        var checkPos = switch (relativePosition) {
        case BELOW -> context.getBlockEntity().getBlockPos().below();
        case BEHIND -> {
            var direction = context.getBlockEntity().orientation.facingDirection;
            yield context.getBlockEntity().getBlockPos().relative(direction.getOpposite());
        }
        };
        return context.getLevel().getBlockState(checkPos).is(block);
    }

    @Override
    public void appendDescription(List<Component> list) {
        var text = switch (relativePosition) {
        case BELOW -> MIText.RequiresBlockBelow;
        case BEHIND -> MIText.RequiresBlockBehind;
        };
        list.add(text.text(block.getName()));
    }

    @Override
    public ItemStack icon() {
        return block.asItem().getDefaultInstance();
    }

    @Override
    public MapCodec<? extends MachineProcessCondition> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ? extends MachineProcessCondition> streamCodec() {
        return STREAM_CODEC;
    }
}

enum RelativePosition implements StringRepresentable {
    BELOW,
    BEHIND;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    @Override
    public String getSerializedName() {
        return toString().toLowerCase(Locale.ROOT);
    }
}
