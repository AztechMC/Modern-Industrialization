package aztech.modern_industrialization.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class MIStreamCodecs {
    private MIStreamCodecs() {
    }

    public static final StreamCodec<ByteBuf, Integer> BYTE = ByteBufCodecs.BYTE.map(b -> (int) b, i -> (byte) i.intValue());
}
