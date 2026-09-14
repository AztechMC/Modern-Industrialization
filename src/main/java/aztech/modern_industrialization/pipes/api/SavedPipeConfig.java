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

package aztech.modern_industrialization.pipes.api;

import aztech.modern_industrialization.pipes.item.ItemNetworkNode;
import aztech.modern_industrialization.pipes.item.ItemPipeInterface;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public record SavedPipeConfig(
        PipeEndpointType connectionType,
        FluidVariant fluid,
        boolean whitelist,
        int insertPriority,
        int extractPriority,
        List<ItemStack> filter,
        ItemStack upgrade) {
    public static final Codec<SavedPipeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemNetworkNode.CONNECTION_TYPE_CODEC.fieldOf("connectionType").forGetter(SavedPipeConfig::connectionType),
            FluidVariant.CODEC.fieldOf("fluid").forGetter(SavedPipeConfig::fluid),
            Codec.BOOL.fieldOf("whitelist").forGetter(SavedPipeConfig::whitelist),
            Codec.INT.fieldOf("insertPriority").forGetter(SavedPipeConfig::insertPriority),
            Codec.INT.fieldOf("extractPriority").forGetter(SavedPipeConfig::extractPriority),
            ItemStack.OPTIONAL_CODEC.listOf(ItemPipeInterface.SLOTS, ItemPipeInterface.SLOTS).fieldOf("filter")
                    .forGetter(SavedPipeConfig::filter),
            ItemStack.OPTIONAL_CODEC.fieldOf("upgrade").forGetter(SavedPipeConfig::upgrade)).apply(instance, SavedPipeConfig::new));
}
