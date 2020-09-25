package aztech.modern_industrialization.items.armor;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;

/*
 * Borrowed from Iron Jetpacks! https://github.com/shedaniel/IronJetpacks/blob/1.16/src/main/java/com/blakebr0/ironjetpacks/handler/KeyBindingsHandler.java
 */
@Environment(EnvType.CLIENT)
public class ClientKeyHandler {
    private static boolean up = false;
    private static KeyBinding keyActivate;

    public static void setup() {
        keyActivate = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.modern_industrialization.activate", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "modern_industrialization"));
    }

    public static void onEndTick(MinecraftClient client) {
        updateState(client);
        updateKeyMap(client);
    }

    public static void updateState(MinecraftClient client) {
        if(client.player == null) return;

        ItemStack chest = client.player.getEquippedStack(EquipmentSlot.CHEST);
        if(chest.getItem() instanceof JetpackItem) {
            JetpackItem jetpack = (JetpackItem) chest.getItem();
            while(keyActivate.wasPressed()) {
                boolean activated = !jetpack.isActivated(chest);
                ArmorPackets.activateJetpack(client.player, activated);
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeBoolean(activated);
                ClientSidePacketRegistry.INSTANCE.sendToServer(ArmorPackets.ACTIVATE_JETPACK, buf);
            }
        }
    }

    public static void updateKeyMap(MinecraftClient client) {
        GameOptions settings = client.options;

        if(client.getNetworkHandler() == null) return;

        boolean upNow = settings.keyJump.isPressed();
        if(upNow != up) {
            up = upNow;

            MIKeyMap.update(client.player, up);
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBoolean(up);
            ClientSidePacketRegistry.INSTANCE.sendToServer(ArmorPackets.UPDATE_KEYS, buf);
        }
    }
}
