package aztech.modern_industrialization.pipes.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public enum PipeConfigType {
    NONE(0),
    ITEM(1),
    FLUID(2);

    private int id;

    PipeConfigType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PipeConfigType decodeConnectionType(int i) {
        return i == 0 ? NONE : i == 1 ? ITEM : FLUID;
    }

    public static int encodeConnectionType(PipeConfigType config) {
        return config == NONE ? 0 : config == ITEM ? 1 : 2;
    }

    public static final Codec<PipeConfigType> CODEC = Codec.INT.comapFlatMap(
            i -> switch (i) {
                case 0 -> DataResult.success(NONE);
                case 1 -> DataResult.success(ITEM);
                case 2 -> DataResult.success(FLUID);
                default -> DataResult.error(() -> "Unknown pipe connection type: " + i);
            },
            PipeConfigType::encodeConnectionType);
}
