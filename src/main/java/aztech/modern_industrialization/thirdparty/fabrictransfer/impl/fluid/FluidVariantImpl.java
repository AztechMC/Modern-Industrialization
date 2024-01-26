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
package aztech.modern_industrialization.thirdparty.fabrictransfer.impl.fluid;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FluidVariantImpl implements FluidVariant {
    private static final Map<Fluid, FluidVariant> noTagCache = new ConcurrentHashMap<>();

    public static FluidVariant of(Fluid fluid, @Nullable CompoundTag nbt) {
        Objects.requireNonNull(fluid, "Fluid may not be null.");

        if (!fluid.isSource(fluid.defaultFluidState()) && fluid != Fluids.EMPTY) {
            // Note: the empty fluid is not still, that's why we check for it specifically.

            if (fluid instanceof FlowingFluid flowable) {
                // Normalize FlowableFluids to their still variants.
                fluid = flowable.getSource();
            } else {
                // If not a FlowableFluid, we don't know how to convert -> crash.
                ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
                throw new IllegalArgumentException("Cannot convert flowing fluid %s (%s) into a still fluid.".formatted(id, fluid));
            }
        }

        if (nbt == null || fluid == Fluids.EMPTY) {
            // Use the cached variant inside the fluid
            return noTagCache.computeIfAbsent(fluid, f -> new FluidVariantImpl(f, null));
        } else {
            // TODO explore caching fluid variants for non null tags.
            return new FluidVariantImpl(fluid, nbt);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-transfer-api-v1/fluid");

    private final Fluid fluid;
    private final @Nullable CompoundTag nbt;
    private final int hashCode;

    public FluidVariantImpl(Fluid fluid, CompoundTag nbt) {
        this.fluid = fluid;
        this.nbt = nbt == null ? null : nbt.copy(); // defensive copy
        this.hashCode = Objects.hash(fluid, nbt);
    }

    @Override
    public boolean isBlank() {
        return fluid == Fluids.EMPTY;
    }

    @Override
    public Fluid getObject() {
        return fluid;
    }

    @Override
    public @Nullable CompoundTag getNbt() {
        return nbt;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag result = new CompoundTag();
        result.putString("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());

        if (nbt != null) {
            result.put("tag", nbt.copy());
        }

        return result;
    }

    public static FluidVariant fromNbt(CompoundTag compound) {
        try {
            Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(compound.getString("fluid")));
            CompoundTag nbt = compound.contains("tag") ? compound.getCompound("tag") : null;
            return of(fluid, nbt);
        } catch (RuntimeException runtimeException) {
            LOGGER.debug("Tried to load an invalid FluidVariant from NBT: {}", compound, runtimeException);
            return FluidVariant.blank();
        }
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        if (isBlank()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeVarInt(BuiltInRegistries.FLUID.getId(fluid));
            buf.writeNbt(nbt);
        }
    }

    public static FluidVariant fromPacket(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return FluidVariant.blank();
        } else {
            Fluid fluid = BuiltInRegistries.FLUID.byId(buf.readVarInt());
            CompoundTag nbt = buf.readNbt();
            return of(fluid, nbt);
        }
    }

    @Override
    public String toString() {
        return "FluidVariant{fluid=" + fluid + ", tag=" + nbt + '}';
    }

    @Override
    public boolean equals(Object o) {
        // succeed fast with == check
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FluidVariantImpl fluidVariant = (FluidVariantImpl) o;
        // fail fast with hash code
        return hashCode == fluidVariant.hashCode && fluid == fluidVariant.fluid && nbtMatches(fluidVariant.nbt);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
