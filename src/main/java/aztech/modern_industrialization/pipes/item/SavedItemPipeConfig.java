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

package aztech.modern_industrialization.pipes.item;

import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public record SavedItemPipeConfig(
        PipeEndpointType connectionType,
        boolean whitelist,
        int insertPriority,
        int extractPriority,
        List<ItemStack> filter,
        ItemStack upgrade) {
    public static final Codec<SavedItemPipeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemNetworkNode.CONNECTION_TYPE_CODEC.fieldOf("connectionType").forGetter(SavedItemPipeConfig::connectionType),
            Codec.BOOL.fieldOf("whitelist").forGetter(SavedItemPipeConfig::whitelist),
            Codec.INT.fieldOf("insertPriority").forGetter(SavedItemPipeConfig::insertPriority),
            Codec.INT.fieldOf("extractPriority").forGetter(SavedItemPipeConfig::extractPriority),
            ItemStack.OPTIONAL_CODEC.listOf(ItemPipeInterface.SLOTS, ItemPipeInterface.SLOTS).fieldOf("filter")
                    .forGetter(SavedItemPipeConfig::filter),
            ItemStack.OPTIONAL_CODEC.fieldOf("upgrade").forGetter(SavedItemPipeConfig::upgrade)).apply(instance, SavedItemPipeConfig::new));
}
