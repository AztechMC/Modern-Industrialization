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
package aztech.modern_industrialization.mixin.client;

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.items.SteamDrillItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @reason The steam drill changes its NBT every tick, causing the client to
 *         stop mining the block.
 */
@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private BlockPos currentBreakingPos;
    @Shadow
    private ItemStack selectedStack;

    /**
     * Rewrites the isCurrentlyBreaking logic, checking for the steam drill in the
     * process. This is done to avoid a more invasive mixin such as a @Redirect.
     */
    @Inject(at = @At("HEAD"), method = "isCurrentlyBreaking", cancellable = true)
    private void isCurrentlyBreakingSteamDrillInject(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ItemStack handStack = client.player.getMainHandStack();
        if (handStack.getItem() == MIItem.ITEM_STEAM_MINING_DRILL && selectedStack.getItem() == MIItem.ITEM_STEAM_MINING_DRILL
                && currentBreakingPos.equals(pos)) {
            cir.setReturnValue(SteamDrillItem.canUse(handStack, client.player));
        }
    }
}
