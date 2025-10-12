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

package aztech.modern_industrialization.blocks.storage;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import aztech.modern_industrialization.util.MIExtraCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ResourceStorage<T extends TransferVariant<?>>(T resource, long amount, boolean locked) {
    public ResourceStorage {
        if (resource.isBlank() && amount != 0) {
            throw new IllegalArgumentException("Expected 0 amount for blank resoruce, got " + amount);
        }
    }

    public static final ResourceStorage<FluidVariant> FLUID_EMPTY = new ResourceStorage<>(FluidVariant.blank(), 0, false);
    public static final ResourceStorage<ItemVariant> ITEM_EMPTY = new ResourceStorage<>(ItemVariant.blank(), 0, false);

    public ResourceStorage<T> withResource(T resource) {
        return new ResourceStorage<>(resource, amount, locked);
    }

    public ResourceStorage<T> withAmount(long amount) {
        return new ResourceStorage<>(resource, amount, locked);
    }

    public ResourceStorage<T> withLocked(boolean locked) {
        return new ResourceStorage<>(resource, amount, locked);
    }

    public static <T extends TransferVariant<?>> Codec<ResourceStorage<T>> codec(Codec<T> variantCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
                variantCodec.fieldOf("resource").forGetter(ResourceStorage::resource),
                MIExtraCodecs.POSITIVE_LONG.optionalFieldOf("amount", 0L).forGetter(ResourceStorage::amount),
                Codec.BOOL.optionalFieldOf("locked", false).forGetter(ResourceStorage::locked)).apply(instance, ResourceStorage::new));
    }

    public static <T extends TransferVariant<?>> StreamCodec<RegistryFriendlyByteBuf, ResourceStorage<T>> streamCodec(
            StreamCodec<RegistryFriendlyByteBuf, T> variantCodec) {
        return StreamCodec.composite(
                variantCodec,
                ResourceStorage::resource,
                ByteBufCodecs.VAR_LONG,
                ResourceStorage::amount,
                ByteBufCodecs.BOOL,
                ResourceStorage::locked,
                (resource, amount, locked) -> {
                    if (resource.isBlank()) {
                        amount = 0L;
                    }
                    return new ResourceStorage<>(resource, amount, locked);
                });
    }

    public static <T extends TransferVariant<?>> UnaryOperator<DataComponentType.Builder<ResourceStorage<T>>> component(Codec<T> variantCodec,
            StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return builder -> builder.persistent(codec(variantCodec)).networkSynchronized(streamCodec(streamCodec));
    }
}
