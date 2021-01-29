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
package aztech.modern_industrialization.nuclear;

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.items.LockableFluidItem;
import java.util.List;
import java.util.Random;
import me.shedaniel.cloth.api.durability.bar.DurabilityBarItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public abstract class MINuclearItem extends MIItem implements DurabilityBarItem, LockableFluidItem, NuclearReactorComponent {

    private int durability;
    private int maxHeat;
    private Item depleted;
    private boolean isFluidLockable = false;

    public MINuclearItem(String id, int durability, int maxHeat, Item depleted) {
        super(id, 1);
        this.durability = durability;
        this.maxHeat = maxHeat;
        this.depleted = depleted;
    }

    protected MINuclearItem setFluidLockable(boolean fluidLockable) {
        this.isFluidLockable = fluidLockable;
        return this;
    }

    public MINuclearItem(String id, int durability, int maxHeat) {
        this(id, durability, maxHeat, null);
    }

    public MINuclearItem(String id, int maxHeat) {
        this(id, -1, maxHeat, null);
    }

    public static int getHeat(ItemStack itemStack) {
        return itemStack.getOrCreateTag().contains("heat") ? itemStack.getOrCreateTag().getInt("heat") : 0;
    }

    public static void setHeat(ItemStack itemStack, int heat) {
        if (heat > 0) {
            itemStack.getOrCreateTag().putInt("heat", heat);
        } else if (heat == 0) {
            itemStack.getOrCreateTag().remove("heat");
        } else {
            throw new IllegalArgumentException("Heat cannot be negative : " + heat);
        }

    }

    public abstract void tick(ItemStack is, NuclearReactorBlockEntity nuclearReactor, double neutronPulse, Random rand);

    public int getDurability() {
        return durability;
    }

    public int getMaxHeat() {
        return maxHeat;
    }

    @Override
    public double getDurabilityBarProgress(ItemStack itemStack) {
        return ((double) itemStack.getDamage()) / durability;
    }

    @Override
    public boolean hasDurabilityBar(ItemStack itemStack) {
        return durability != -1 && itemStack.getDamage() != 0;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        if (stack.getTag() != null) {
            if (getDurability() > 0) {
                Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xcbf026));
                String quantity = (getDurability() - stack.getDamage()) + " / " + getDurability();
                tooltip.add(new TranslatableText("text.modern_industrialization.burnup", quantity).setStyle(style));
            }

            int heat = getHeat(stack);
            int color;
            double progress = (double) heat / getMaxHeat();

            if (progress > 0.9) {
                color = 0xd41900;
            } else if (progress > 0.75) {
                color = colorInter(0xff9100, 0xd41900, 0.75, 0.9, progress);
                ;
            } else if (progress > 0.5) {
                color = colorInter(0xffee00, 0xff9100, 0.5, 0.75, progress);
            } else if (progress > 0.25) {
                color = colorInter(0x00ff15, 0xffee00, 0.25, 0.5, progress);
            } else {
                color = colorInter(0xffffff, 0x00ff15, 0, 0.25, progress);
            }
            Style styleHeat = Style.EMPTY.withColor(TextColor.fromRgb(color));
            String heatString = heat + " / " + getMaxHeat();
            tooltip.add(new TranslatableText("text.modern_industrialization.heat", heatString).setStyle(styleHeat));
            this.appendFluidTooltip(stack, world, tooltip, context);
        }
    }

    public static int colorInter(int c1, int c2, double a, double b, double x) {
        double r1 = (c1 & 0xff0000) >> 16;
        double r2 = (c2 & 0xff0000) >> 16;
        double g1 = (c1 & 0x00ff00) >> 8;
        double g2 = (c2 & 0x00ff00) >> 8;
        double b1 = (c1 & 0x0000ff);
        double b2 = (c2 & 0x0000ff);

        double inter = (x - a) / (b - a);

        double red = inter * r2 + (1 - inter) * r1;
        double green = inter * g2 + (1 - inter) * g1;
        double blue = inter * b2 + (1 - inter) * b1;
        return (clip((int) red) << 16) + (clip((int) green) << 8) + clip((int) blue);

    }

    public static int clip(int x) {
        if (x < 0) {
            return 0;
        }
        return x < 255 ? x : 255;
    }

    public Item getDepleted() {
        return depleted;
    }

    @Override
    public boolean isFluidLockable() {
        return isFluidLockable;
    }
}
