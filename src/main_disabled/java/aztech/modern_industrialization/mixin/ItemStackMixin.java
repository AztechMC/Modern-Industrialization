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

import aztech.modern_industrialization.items.DynamicEnchantmentItem;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    protected abstract Item getItem();

    @Inject(method = "getEnchantmentTags", at = @At("RETURN"), cancellable = true)
    private void getEnchantmentsHook(CallbackInfoReturnable<ListTag> cir) {
        if (getItem() instanceof DynamicEnchantmentItem dyn) {
            Reference2IntMap<Enchantment> enchantments = dyn.getEnchantments((ItemStack) (Object) this);
            ListTag resultCopy = cir.getReturnValue().copy();

            for (Reference2IntMap.Entry<Enchantment> entry : enchantments.reference2IntEntrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getIntValue();
                String id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment).toString();

                boolean replacedAny = false;

                for (Tag subTag : resultCopy) {
                    if (subTag instanceof CompoundTag compoundTag) {
                        if (compoundTag.getString("id").equals(id)) {
                            compoundTag.putShort("lvl", (short) level);
                            replacedAny = true;
                            break;
                        }
                    }
                }

                if (!replacedAny) {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("id", BuiltInRegistries.ENCHANTMENT.getKey(enchantment).toString());
                    tag.putInt("lvl", level);
                    resultCopy.add(tag);
                }
            }

            cir.setReturnValue(resultCopy);
        }
    }
}
