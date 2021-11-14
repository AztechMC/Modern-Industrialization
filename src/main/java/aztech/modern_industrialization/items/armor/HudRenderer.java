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
package aztech.modern_industrialization.items.armor;

import aztech.modern_industrialization.items.FluidFuelItemHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class HudRenderer {
    public static void onRenderHud(MatrixStack matrices, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            ItemStack chest = mc.player.getEquippedStack(EquipmentSlot.CHEST);
            if (chest.getItem() instanceof JetpackItem jetpack) {
                boolean active = jetpack.isActivated(chest);
                Text activeText = new TranslatableText("text.modern_industrialization.jetpack_" + active)
                        .setStyle(Style.EMPTY.withColor(active ? Formatting.GREEN : Formatting.RED));
                mc.textRenderer.drawWithShadow(matrices, activeText, 4, 4, 16383998);
                Text fillText = new TranslatableText("text.modern_industrialization.jetpack_fill",
                        FluidFuelItemHelper.getAmount(chest) * 100 / JetpackItem.CAPACITY);
                mc.textRenderer.drawWithShadow(matrices, fillText, 4, 14, 16383998);
            } else if (chest.getItem() instanceof GraviChestPlateItem gsp) {
                boolean active = gsp.isActivated(chest);
                Text activeText = new TranslatableText("text.modern_industrialization.gravichestplate_" + active)
                        .setStyle(Style.EMPTY.withColor(active ? Formatting.GREEN : Formatting.RED));
                mc.textRenderer.drawWithShadow(matrices, activeText, 4, 4, 16383998);
                Text fillText = new TranslatableText("text.modern_industrialization.energy_fill",
                        gsp.getEnergy(chest) * 100 / GraviChestPlateItem.ENERGY_CAPACITY);
                mc.textRenderer.drawWithShadow(matrices, fillText, 4, 14, 16383998);
            }
        }
    }
}
