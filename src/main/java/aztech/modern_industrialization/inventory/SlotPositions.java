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

package aztech.modern_industrialization.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;

public class SlotPositions {
    private final int[] x, y;

    private SlotPositions(int[] x, int[] y) {
        this.x = x;
        this.y = y;
    }

    public int getX(int index) {
        return x[index];
    }

    public int getY(int index) {
        return y[index];
    }

    public int size() {
        return x.length;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarIntArray(x);
        buf.writeVarIntArray(y);
    }

    public SlotPositions sublist(int start, int end) {
        return new SlotPositions(Arrays.copyOfRange(x, start, end), Arrays.copyOfRange(y, start, end));
    }

    public Builder toBuilder() {
        var builder = new Builder();

        for (int i = 0; i < x.length; i++) {
            builder.addSlot(x[i], y[i]);
        }

        return builder;
    }

    public static SlotPositions read(FriendlyByteBuf buf) {
        int[] x = buf.readVarIntArray();
        int[] y = buf.readVarIntArray();
        return new SlotPositions(x, y);
    }

    public static SlotPositions empty() {
        return new SlotPositions(new int[0], new int[0]);
    }

    public static class Builder {
        private final ArrayList<Integer> x = new ArrayList<>();
        private final ArrayList<Integer> y = new ArrayList<>();

        public Builder addSlot(int x, int y) {
            this.x.add(x);
            this.y.add(y);
            return this;
        }

        public Builder addSlots(int x, int y, int columns, int rows) {
            // The loop order is intended here:
            // We want to fill the slots horizontally first for correct insertion order.
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < columns; ++j) {
                    addSlot(x + j * 18, y + i * 18);
                }
            }
            return this;
        }

        public SlotPositions build() {
            return new SlotPositions(x.stream().mapToInt(x -> x).toArray(), y.stream().mapToInt(y -> y).toArray());
        }

        public SlotPositions buildWithConsumer(Consumer<Builder> consumer) {
            consumer.accept(this);
            return build();
        }
    }
}
