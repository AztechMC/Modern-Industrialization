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

import appeng.parts.p2p.P2PTunnelPart;
import aztech.modern_industrialization.compat.ae2.EnergyP2PTunnelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(P2PTunnelPart.class)
public class P2PTunnelPartMixin {

    // Disable attunement for EnergyP2PTunnelPart
    @Inject(method = "onPartActivate", at = @At(value = "INVOKE", target = "Lappeng/api/features/P2PTunnelAttunement;getTunnelPartByTriggerItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void onPartActivate(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if ((Object) this instanceof EnergyP2PTunnelPart) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }
}
