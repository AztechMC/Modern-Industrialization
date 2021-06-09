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

import net.minecraft.nbt.NbtCompound;

public interface IComponent {
    void writeNbt(NbtCompound tag);

    void readNbt(NbtCompound tag);

    default void writeClientNbt(NbtCompound tag) {
        writeNbt(tag);
    }

    default void readClientNbt(NbtCompound tag) {
        readNbt(tag);
    }

    interface ClientOnly extends IComponent {
        @Override
        default void writeNbt(NbtCompound tag) {
        }

        @Override
        default void readNbt(NbtCompound tag) {
        }

        @Override
        void writeClientNbt(NbtCompound tag);

        @Override
        void readClientNbt(NbtCompound tag);
    }

    interface ServerOnly extends IComponent {
        @Override
        default void writeClientNbt(NbtCompound tag) {
        }

        @Override
        default void readClientNbt(NbtCompound tag) {
        }
    }
}
