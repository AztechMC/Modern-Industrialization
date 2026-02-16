package aztech.modern_industrialization.client.machines.models;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public enum OverlayName implements StringRepresentable {
    TOP,
    TOP_ACTIVE,
    SIDE,
    SIDE_ACTIVE,
    BOTTOM,
    BOTTOM_ACTIVE,
    FRONT,
    FRONT_ACTIVE,
    LEFT,
    LEFT_ACTIVE,
    RIGHT,
    RIGHT_ACTIVE,
    BACK,
    BACK_ACTIVE,
    TOP_S,
    TOP_S_ACTIVE,
    TOP_W,
    TOP_W_ACTIVE,
    TOP_N,
    TOP_N_ACTIVE,
    TOP_E,
    TOP_E_ACTIVE,
    BOTTOM_S,
    BOTTOM_S_ACTIVE,
    BOTTOM_W,
    BOTTOM_W_ACTIVE,
    BOTTOM_N,
    BOTTOM_N_ACTIVE,
    BOTTOM_E,
    BOTTOM_E_ACTIVE,
    OUTPUT,
    ITEM_AUTO,
    FLUID_AUTO;

    public static final EnumCodec<OverlayName> CODEC = StringRepresentable.fromEnum(OverlayName::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
