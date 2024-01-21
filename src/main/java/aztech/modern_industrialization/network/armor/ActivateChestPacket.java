package aztech.modern_industrialization.network.armor;

import aztech.modern_industrialization.items.armor.ActivatableChestItem;
import aztech.modern_industrialization.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ActivateChestPacket(boolean activated) implements BasePacket {
    public ActivateChestPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(activated);
    }

    @Override
    public void handle(Context ctx) {
        activateChest(ctx.getPlayer(), activated);
    }

    public static void activateChest(Player player, boolean activated) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof ActivatableChestItem activatable) {
            activatable.setActivated(chest, activated);
        }
    }
}
