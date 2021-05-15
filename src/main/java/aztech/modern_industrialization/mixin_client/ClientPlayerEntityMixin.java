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
package aztech.modern_industrialization.mixin_client;

import aztech.modern_industrialization.api.IElytraItem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerEntityMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"), method = "tickMovement", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isClimbing()Z"), to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;checkFallFlying()Z")), require = 1, allow = 1)
    void startElytraFlight(CallbackInfo info) {
        ClientPlayerEntity entity = (ClientPlayerEntity) (Object) this;
        ItemStack stack = entity.getEquippedStack(EquipmentSlot.CHEST);
        if (stack.getItem() instanceof IElytraItem && checkFallFlying(entity)) {
            if (((IElytraItem) stack.getItem()).allowElytraFlight(stack, entity)) {
                entity.networkHandler.sendPacket(new ClientCommandC2SPacket(entity, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }
    }

    @Unique
    boolean checkFallFlying(ClientPlayerEntity entity) {
        return !entity.isOnGround() && !entity.isFallFlying() && !entity.isTouchingWater() && !entity.hasStatusEffect(StatusEffects.LEVITATION);
    }
}
