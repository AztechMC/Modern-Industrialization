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

package aztech.modern_industrialization.pipes.api;

import aztech.modern_industrialization.MIComponents;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface NetworkNodeConnection {
    SavedPipeConfig getConfig();

    void applyConfig(PipeBlockEntity pipe, @Nullable SavedPipeConfig config, Player player);

    Direction getDirection();

    default void useConfigCard(PipeBlockEntity pipe, ItemStack stack, Player player) {
        if (player.isShiftKeyDown()) {
            stack.remove(MIComponents.CAMOUFLAGE);
            stack.set(MIComponents.SAVED_CONFIG, getConfig());
            player.displayClientMessage(MIText.ConfigCardSet.text(), true);
        } else if (stack.has(MIComponents.SAVED_CONFIG)) {
            applyConfig(pipe, stack.get(MIComponents.SAVED_CONFIG), player);
            player.displayClientMessage(MIText.ConfigCardApplied.text(), true);
        }
    }

    static boolean use(Iterable<? extends NetworkNodeConnection> connections, PipeBlockEntity pipe, Player player, InteractionHand hand, @Nullable Direction hitDirection) {
        for (NetworkNodeConnection conn : connections) {
            if (conn.getDirection() != hitDirection) {
                continue;
            }

            var stack = player.getItemInHand(hand);
            if (!MIItem.CONFIG_CARD.is(stack)) {
                return false;
            }
            conn.useConfigCard(pipe, stack, player);
            return true;
        }
        return false;
    }
}
