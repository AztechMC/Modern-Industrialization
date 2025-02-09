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
package aztech.modern_industrialization.attributes;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.items.tools.QuantumSword;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.TooltipFlag;

public class InfiniteDamageAttribute extends DisplayNamedAttribute {
    private static final String INFINITY = "\u221e";

    public InfiniteDamageAttribute() {
        super(MIText.AttributeInfiniteDamage.getTranslationKey(), 0);
    }

    // Allows a modifier with the base id to render green with the effective value.
    // Other modifiers still render blue with the additional value.
    @Override
    public ResourceLocation getBaseId() {
        return QuantumSword.BASE_INFINITE_DAMAGE;
    }

    @Override
    public MutableComponent toValueComponent(AttributeModifier.Operation operation, double value, TooltipFlag flag) {
        return Component.literal(INFINITY);
    }

    @Override
    public MutableComponent toBaseComponent(double value, double entityBase, boolean merged, TooltipFlag flag) {
        String stringValue = value > Mth.EPSILON ? INFINITY : FORMAT.format(0);
        MutableComponent comp = Component.translatable("attribute.modifier.equals.0", stringValue,
                Component.translatable(this.getTooltipDescriptionId()));
        if (flag.isAdvanced() && !merged) {
            Component debugInfo = Component.literal(" ")
                    .append(Component.translatable("neoforge.attribute.debug.base", FORMAT.format(entityBase), FORMAT.format(value - entityBase))
                            .withStyle(ChatFormatting.GRAY));
            comp.append(debugInfo);
        }
        return comp;
    }

    @Override
    public String getTooltipDescriptionId() {
        return "attribute.name.generic.attack_damage";
    }
}
