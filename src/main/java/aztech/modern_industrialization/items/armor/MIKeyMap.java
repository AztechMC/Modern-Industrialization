package aztech.modern_industrialization.items.armor;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.player.PlayerEntity;

public class MIKeyMap {
    private static final Map<PlayerEntity, Boolean> HOLDING_UP = new HashMap<>();

    static boolean isHoldingUp(PlayerEntity player) {
        return HOLDING_UP.getOrDefault(player, false);
    }

    static void update(PlayerEntity player, boolean up) {
        HOLDING_UP.put(player, up);
    }

    // TODO: call this on leave and dimension change
    public static void clear(PlayerEntity player) {
        HOLDING_UP.remove(player);
    }
}
