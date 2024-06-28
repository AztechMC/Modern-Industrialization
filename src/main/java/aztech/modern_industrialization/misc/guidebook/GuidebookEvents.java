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
package aztech.modern_industrialization.misc.guidebook;

import aztech.modern_industrialization.MIAdvancementTriggers;
import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class GuidebookEvents {
    public static void init() {
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            var player = event.getEntity();
            if (MIConfig.getConfig().spawnWithGuideBook) {
                GuidebookPersistentState state = GuidebookPersistentState.get(player.getServer());
                if (!state.hasPlayerReceivedGuidebook(player)) {
                    if (player.getInventory().add(new ItemStack(MIItem.GUIDE_BOOK))) {
                        state.addPlayerReceivedGuidebook(player);
                    }
                }
            }
            // In any case, fire the logged in trigger
            MIAdvancementTriggers.PLAYER_LOGGED_IN.get().trigger((ServerPlayer) player);
        });

        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerRespawnEvent.class, event -> {
            if (!event.isEndConquered() && MIConfig.getConfig().respawnWithGuideBook) {
                event.getEntity().getInventory().add(new ItemStack(MIItem.GUIDE_BOOK));
            }
        });
    }
}
