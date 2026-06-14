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

package aztech.modern_industrialization.machines;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface MachineComponent {
    void writeNbt(CompoundTag tag, HolderLookup.Provider registries);

    void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine);

    default void writeClientNbt(CompoundTag tag, HolderLookup.Provider registries) {
        writeNbt(tag, registries);
    }

    default void readClientNbt(CompoundTag tag, HolderLookup.Provider registries) {
        readNbt(tag, registries, false);
    }

    interface ClientOnly extends MachineComponent {
        @Override
        default void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {}

        @Override
        default void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {}

        @Override
        void writeClientNbt(CompoundTag tag, HolderLookup.Provider registries);

        @Override
        void readClientNbt(CompoundTag tag, HolderLookup.Provider registries);
    }

    interface ServerOnly extends MachineComponent {
        @Override
        default void writeClientNbt(CompoundTag tag, HolderLookup.Provider registries) {}

        @Override
        default void readClientNbt(CompoundTag tag, HolderLookup.Provider registries) {}
    }
}
