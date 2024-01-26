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

import aztech.modern_industrialization.network.armor.ActivateChestPacket;
import aztech.modern_industrialization.network.armor.UpdateKeysPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/*
 * Borrowed from Iron Jetpacks! https://github.com/shedaniel/IronJetpacks/blob/1.16/src/main/java/com/blakebr0/ironjetpacks/handler/KeyBindingsHandler.java
 */
public class ClientKeyHandler {
    private static boolean up = false;
    public static KeyMapping keyActivate = new KeyMapping("key.modern_industrialization.activate", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "modern_industrialization");

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
                ActivateChestPacket.activateChest(client.player, activated);
                new ActivateChestPacket(activated).sendToServer();
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
            new UpdateKeysPacket(up).sendToServer();
        }
    }
}
