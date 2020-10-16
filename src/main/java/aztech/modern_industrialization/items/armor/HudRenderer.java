package aztech.modern_industrialization.items.armor;

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
            if (chest.getItem() instanceof JetpackItem) {
                JetpackItem jetpack = (JetpackItem) chest.getItem();
                boolean active = jetpack.isActivated(chest);
                Text activeText = new TranslatableText("text.modern_industrialization.jetpack_" + active)
                        .setStyle(Style.EMPTY.withColor(active ? Formatting.GREEN : Formatting.RED));
                mc.textRenderer.drawWithShadow(matrices, activeText, 4, 4, 16383998);
                Text fillText = new TranslatableText("text.modern_industrialization.jetpack_fill",
                        jetpack.getAmount(chest) * 100 / jetpack.getCapacity());
                mc.textRenderer.drawWithShadow(matrices, fillText, 4, 14, 16383998);
            }
        }
    }
}
