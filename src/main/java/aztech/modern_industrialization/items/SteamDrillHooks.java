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
package aztech.modern_industrialization.items;

import aztech.modern_industrialization.MIItem;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public final class SteamDrillHooks {
    private SteamDrillHooks() {
    }

    public static void overrideDestroyProgress(Player pPlayer, BlockGetter pLevel, CallbackInfoReturnable<Float> cir) {
        if (!MIItem.STEAM_MINING_DRILL.is(pPlayer.getMainHandItem())) {
            return;
        }

        var area = SteamDrillItem.getArea(pLevel, pPlayer);
        if (area == null) {
            return;
        }

        MutableFloat minProgress = new MutableFloat(Float.MAX_VALUE);
        MutableBoolean foundAny = new MutableBoolean(false);

        SteamDrillItem.forEachMineableBlock(pLevel, area, pPlayer, (blockPos, state) -> {
            // Call on Block directly to avoid infinite recursion...
            @SuppressWarnings("deprecation")
            float destroyProgress = state.getBlock().getDestroyProgress(state, pPlayer, pLevel, blockPos);

            if (destroyProgress > 1e-9) {
                foundAny.setTrue();
                minProgress.setValue(Math.min(minProgress.getValue(), destroyProgress));
            }
        });

        if (foundAny.isTrue()) {
            cir.setReturnValue(minProgress.getValue());
            cir.cancel();
        }
    }

    /**
     * Client-side only. Forces a block break reset when the targeted side changes.
     * Server doesn't need this because it handles block breaking progress differently.
     */
    @Nullable
    public static Direction breakingSide = null;

    private static void onStartBreaking(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getUseItem() == Event.Result.DENY || event.getSide().isServer()) {
            return;
        }

        breakingSide = event.getFace();
    }

    public static void init() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, SteamDrillHooks::onStartBreaking);
    }
}
