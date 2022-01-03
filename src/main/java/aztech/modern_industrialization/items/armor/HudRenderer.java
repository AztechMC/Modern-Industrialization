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
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class HudRenderer {
    public static void onRenderHud(PoseStack matrices, float delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.getItem() instanceof JetpackItem jetpack) {
                boolean active = jetpack.isActivated(chest);
                Component activeText = new TranslatableComponent("text.modern_industrialization.jetpack_" + active)
                        .setStyle(Style.EMPTY.withColor(active ? ChatFormatting.GREEN : ChatFormatting.RED));
                mc.font.drawShadow(matrices, activeText, 4, 4, 16383998);
                Component fillText = new TranslatableComponent("text.modern_industrialization.jetpack_fill",
                        FluidFuelItemHelper.getAmount(chest) * 100 / JetpackItem.CAPACITY);
                mc.font.drawShadow(matrices, fillText, 4, 14, 16383998);
            } else if (chest.getItem() instanceof GraviChestPlateItem gsp) {
                boolean active = gsp.isActivated(chest);
                Component activeText = new TranslatableComponent("text.modern_industrialization.gravichestplate_" + active)
                        .setStyle(Style.EMPTY.withColor(active ? ChatFormatting.GREEN : ChatFormatting.RED));
                mc.font.drawShadow(matrices, activeText, 4, 4, 16383998);
                Component fillText = new TranslatableComponent("text.modern_industrialization.energy_fill",
                        gsp.getEnergy(chest) * 100 / GraviChestPlateItem.ENERGY_CAPACITY);
                mc.font.drawShadow(matrices, fillText, 4, 14, 16383998);
            }
        }
    }
}
