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

package aztech.modern_industrialization.machines.models;

import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class MachineCasing {
    public final ResourceLocation key;
    /**
     * Not null when registered as an imitation. The actual model might not be an imitation since it is resource pack driven.
     * Mostly used to pull the name of the casing from the block it imitates. Will also generate a corresponding casing model.
     */
    @Nullable
    public final Supplier<? extends Block> imitatedBlock;

    MachineCasing(ResourceLocation key, @Nullable Supplier<? extends Block> imitatedBlock) {
        this.key = key;
        this.imitatedBlock = imitatedBlock;
    }

    public String getTranslationKey() {
        if (imitatedBlock != null) {
            throw new IllegalArgumentException("Cannot get translation key for casing imitating a block.");
        }
        return Util.makeDescriptionId("machine_casing", key);
    }

    public MutableComponent getName() {
        if (imitatedBlock != null) {
            return imitatedBlock.get().getName();
        }
        return Component.translatable(getTranslationKey());
    }
}
