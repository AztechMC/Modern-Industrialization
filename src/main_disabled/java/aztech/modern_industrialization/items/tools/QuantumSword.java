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
package aztech.modern_industrialization.items.tools;

import java.util.List;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class QuantumSword extends Item {
    public QuantumSword(FabricItemSettings settings) {
        super(settings.customDamage((is, amt, e, cb) -> 0));
    }

    public static void init() {
        /**
         * This way of attacking skips vanilla's attacking to allow for attacking
         * even if the player has zero attack damage because of stuff like weakness effect or attribute modifiers
         */
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player.isSpectator()) {
                return InteractionResult.PASS;
            }
            if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().getItem() instanceof QuantumSword quantumSword) {
                if (entity instanceof LivingEntity livingEntity) {
                    quantumSword.onHurtEnemy(player.getMainHandItem(), livingEntity, player);
                } else {
                    entity.kill();
                }
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        });
    }

    private void onHurtEnemy(ItemStack stack, LivingEntity target, Player attacker) {
        target.hurt(attacker.level().damageSources().source(DamageTypes.GENERIC_KILL, attacker), Float.MAX_VALUE);

        // TODO: if lama was hit, kill the wander trader (and the opposite) and give an
        // advancement
        // TODO: same for phantoms
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag context) {
        list.add(Component.empty());
        list.add(Component.translatable("item.modifiers.mainhand").withStyle(ChatFormatting.GRAY));
        String infinity = "\u221e";
        list.add(Component.translatable("attribute.modifier.plus.0", infinity, Component.translatable("attribute.name.generic.attack_damage"))
                .withStyle(ChatFormatting.BLUE));
    }
}
