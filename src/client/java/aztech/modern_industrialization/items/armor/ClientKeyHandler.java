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

import com.mojang.blaze3d.platform.InputConstants;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/*
 * Borrowed from Iron Jetpacks! https://github.com/shedaniel/IronJetpacks/blob/1.16/src/main/java/com/blakebr0/ironjetpacks/handler/KeyBindingsHandler.java
 */
@Environment(EnvType.CLIENT)
public class ClientKeyHandler {
    private static boolean up = false;
    private static KeyMapping keyActivate;

    public static void setup() {
        keyActivate = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.modern_industrialization.activate", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "modern_industrialization"));
    }

    public static void onEndTick(Minecraft client) {
        updateState(client);
        updateKeyMap(client);
    }

    public static void updateState(Minecraft client) {
        if (client.player == null)
            return;

        ItemStack chest = client.player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof ActivatableChestItem activatable) {
            while (keyActivate.consumeClick()) {
                boolean activated = !activatable.isActivated(chest);
                ArmorPackets.activateChest(client.player, activated);
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeBoolean(activated);
                ClientPlayNetworking.send(ArmorPackets.ACTIVATE_CHEST, buf);
            }
        }
    }

    public static void updateKeyMap(Minecraft client) {
        Options settings = client.options;

        if (client.getConnection() == null)
            return;

        boolean upNow = settings.keyJump.isDown();
        if (upNow != up) {
            up = upNow;

            MIKeyMap.update(client.player, up);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBoolean(up);
            ClientPlayNetworking.send(ArmorPackets.UPDATE_KEYS, buf);
        }
    }
}
