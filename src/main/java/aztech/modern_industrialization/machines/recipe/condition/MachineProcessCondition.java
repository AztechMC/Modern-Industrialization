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

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public interface MachineProcessCondition {
    private static Codec<MachineProcessCondition> makeCodec(boolean syncToClient) {
        return ResourceLocation.CODEC
                .<Codec<? extends MachineProcessCondition>>flatXmap(
                        resLoc -> Optional.ofNullable(MachineProcessConditions.get(resLoc))
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error(() -> "Unknown machine process condition " + resLoc)),
                        codec -> Optional.ofNullable(MachineProcessConditions.getId(codec))
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error(() -> "Unknown machine process condition codec " + codec)))
                .dispatch(cond -> cond.codec(syncToClient), c -> c);
    }

    Codec<MachineProcessCondition> CODEC = makeCodec(false);
    Codec<MachineProcessCondition> CODEC_FOR_SYNC = makeCodec(true);

    boolean canProcessRecipe(Context context, MachineRecipe recipe);

    void appendDescription(List<Component> list);

    Codec<? extends MachineProcessCondition> codec(boolean syncToClient);

    interface Context {
        MachineBlockEntity getBlockEntity();

        default ServerLevel getLevel() {
            return (ServerLevel) getBlockEntity().getLevel();
        }
    }
}
