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
package aztech.modern_industrialization.items.armor;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
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
        keyActivate = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.modern_industrialization.activate", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "modern_industrialization"));
    }

    public static void onEndTick(MinecraftClient client) {
        updateState(client);
        updateKeyMap(client);
    }

    public static void updateState(MinecraftClient client) {
        if (client.player == null)
            return;

        ItemStack chest = client.player.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof ActivatableChestItem activatable) {
            while (keyActivate.wasPressed()) {
                boolean activated = !activatable.isActivated(chest);
                ArmorPackets.activateChest(client.player, activated);
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeBoolean(activated);
                ClientSidePacketRegistry.INSTANCE.sendToServer(ArmorPackets.ACTIVATE_CHEST, buf);
            }
        }
    }

    public static void updateKeyMap(MinecraftClient client) {
        GameOptions settings = client.options;

        if (client.getNetworkHandler() == null)
            return;

        boolean upNow = settings.keyJump.isPressed();
        if (upNow != up) {
            up = upNow;

            MIKeyMap.update(client.player, up);
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBoolean(up);
            ClientSidePacketRegistry.INSTANCE.sendToServer(ArmorPackets.UPDATE_KEYS, buf);
        }
    }
}
