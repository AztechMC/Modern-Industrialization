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
package aztech.modern_industrialization.blocks;

import aztech.modern_industrialization.util.MobSpawning;
import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.fluid.FluidIo;
import dev.technici4n.fasttransferlib.api.item.ItemIo;
import dev.technici4n.fasttransferlib.api.item.ItemKey;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.BlockSoundGroup;

public class TrashCanBlock extends Block {
    public static final ItemIo ITEM_TRASH = new ItemIo() {
        @Override
        public int getItemSlotCount() {
            return 0;
        }

        @Override
        public ItemKey getItemKey(int i) {
            return ItemKey.EMPTY;
        }

        @Override
        public int getItemCount(int i) {
            return 0;
        }

        @Override
        public boolean supportsItemInsertion() {
            return true;
        }

        @Override
        public int insert(ItemKey key, int count, Simulation simulation) {
            return 0;
        }
    };

    public static final FluidIo FLUID_TRASH = new FluidIo() {
        @Override
        public int getFluidSlotCount() {
            return 0;
        }

        @Override
        public Fluid getFluid(int i) {
            return Fluids.EMPTY;
        }

        @Override
        public long getFluidAmount(int i) {
            return 0;
        }

        @Override
        public boolean supportsFluidInsertion() {
            return true;
        }

        @Override
        public long insert(Fluid fluid, long amount, Simulation simulation) {
            return 0;
        }
    };

    public TrashCanBlock() {
        super(FabricBlockSettings.of(Material.METAL).hardness(6.0f).resistance(1200).sounds(BlockSoundGroup.METAL)
                .allowsSpawning(MobSpawning.NO_SPAWN));
    }
}
