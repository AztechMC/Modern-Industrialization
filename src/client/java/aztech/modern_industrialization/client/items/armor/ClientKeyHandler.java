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

package aztech.modern_industrialization.client.items.armor;

import aztech.modern_industrialization.client.MIKeybinds;
import aztech.modern_industrialization.items.armor.MIKeyMap;
import aztech.modern_industrialization.network.armor.UpdateKeysPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public class ClientKeyHandler {
    private static boolean up = false;

    public static void onEndTick(Minecraft client) {
        updateKeyMap(client);

        for (MIKeybinds.Keybind keybind : MIKeybinds.getMappings()) {
            while (keybind.holder().get().consumeClick()) {
                keybind.action().run();
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
