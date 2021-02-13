package aztech.modern_industrialization.inventory;

import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;

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

    public void write(PacketByteBuf buf) {
        buf.writeIntArray(x);
        buf.writeIntArray(y);
    }

    public static SlotPositions read(PacketByteBuf buf) {
        int[] x = buf.readIntArray();
        int[] y = buf.readIntArray();
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

        public Builder addSlots(int x, int y, int rows, int columns) {
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < columns; ++j) {
                    addSlot(x + i * 18, y + j * 18);
                }
            }
            return this;
        }

        public SlotPositions build() {
            return new SlotPositions(x.stream().mapToInt(x -> x).toArray(), y.stream().mapToInt(y -> y).toArray());
        }
    }
}
