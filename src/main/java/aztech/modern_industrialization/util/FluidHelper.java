package aztech.modern_industrialization.util;

import aztech.modern_industrialization.fluid.CraftingFluid;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FluidHelper {
    public static Text getFluidName(Fluid fluid) {
        if (fluid == Fluids.EMPTY) {
            return new TranslatableText("text.modern_industrialization.fluid_slot_empty");
        } else {
            Identifier id = Registry.FLUID.getId(fluid);
            TranslatableText text = new TranslatableText("block." + id.getNamespace() + "." + id.getPath());

            if (fluid instanceof CraftingFluid) {
                text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(((CraftingFluid) fluid).color)));
            }

            return text;
        }
    }

    @Environment(EnvType.CLIENT)
    public static Text getFluidAmount(long amount, long capacity) {
        String text = "";
        boolean showMb = !Screen.hasShiftDown() && !MinecraftClient.getInstance().player.isSneaking();

        if (showMb) {
            if (0 < amount && amount < 81) text += "< ";

            amount /= 81;
            capacity /= 81;
        }

        text += amount + " / " + capacity;
        return new TranslatableText("text.modern_industrialization.fluid_amount_" + (showMb ? "mb" : "md"), text);
    }
}
