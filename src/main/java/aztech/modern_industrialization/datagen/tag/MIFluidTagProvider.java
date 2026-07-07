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

package aztech.modern_industrialization.datagen.tag;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.machines.blockentities.ReplicatorMachineBlockEntity;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

public class MIFluidTagProvider extends FluidTagsProvider {
    public MIFluidTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, MI.ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        for (var def : MIFluids.FLUID_DEFINITIONS.values()) {
            if (def.isGas) {
                tag(Tags.Fluids.GASEOUS).add(kf(def.asFluid()));
            }

            // Give a #c: tag to every MI fluid. That should allow other mods to use MI's fluids in many cases.
            tag(FluidTags.create(Identifier.fromNamespaceAndPath("c", def.path())))
                    .add(kf(def.asFluid()));
        }

        tag(ReplicatorMachineBlockEntity.BLACKLISTED_FLUIDS)
                .add(kf(MIFluids.UU_MATTER.asFluid()));
    }

    // MC 26.2: TagAppender.add now takes ResourceKey instead of the fluid object.
    private static ResourceKey<Fluid> kf(Fluid fluid) {
        return fluid.builtInRegistryHolder().key();
    }
}
