package aztech.modern_industrialization.items.armor;

import aztech.modern_industrialization.MIIdentifier;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ArmorPackets {
    public static final Identifier UPDATE_KEYS = new MIIdentifier("update_keys");
    public static final PacketConsumer ON_UPDATE_KEYS = (context, buffer) -> {
        boolean up = buffer.readBoolean();
        context.getTaskQueue().execute(() -> {
            MIKeyMap.update(context.getPlayer(), up);
        });
    };
    public static final Identifier ACTIVATE_JETPACK = new MIIdentifier("activate_jetpack");
    public static final PacketConsumer ON_ACTIVATE_JETPACK = (context, buffer) -> {
        boolean activated = buffer.readBoolean();
        context.getTaskQueue().execute(() -> activateJetpack(context.getPlayer(), activated));
    };

    static void activateJetpack(PlayerEntity player, boolean activated) {
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof JetpackItem) {
            JetpackItem jetpack = (JetpackItem) chest.getItem();
            jetpack.setActivated(chest, activated);
        }
    }
}
