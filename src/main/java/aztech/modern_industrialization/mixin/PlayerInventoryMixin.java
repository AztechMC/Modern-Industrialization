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
package aztech.modern_industrialization.mixin;

import aztech.modern_industrialization.items.SteamDrillItem;
import aztech.modern_industrialization.mixin_impl.SteamDrillHooks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @reason Grabs the current player so that it can be used in
 *         {@link SteamDrillItem#getMiningLevel}, when it would otherwise be
 *         null. The priority is low so that the mixin from Fabric API can
 *         inject its regular hook, and we can add our RETURN hook before the
 *         early return as well.
 */
@Mixin(value = PlayerInventory.class, priority = 100)
public class PlayerInventoryMixin {
    @Inject(at = @At("HEAD"), method = "getBlockBreakingSpeed")
    private void getBlockBreakingSpeedHead(BlockState block, CallbackInfoReturnable<Float> cir) {
        SteamDrillHooks.set(((PlayerInventory) (Object) this).player);
    }

    @Inject(at = @At("RETURN"), method = "getBlockBreakingSpeed")
    private void getBlockBreakingSpeedReturn(BlockState block, CallbackInfoReturnable<Float> cir) {
        SteamDrillHooks.remove();
    }
}
