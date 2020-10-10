package aztech.modern_industrialization;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import me.shedaniel.cloth.api.durability.bar.DurabilityBarItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

import java.util.List;

public class MINuclearItem extends MIItem implements DurabilityBarItem {

    private int durability;
    private Item depleted;

    public MINuclearItem(String id, int durability, Item depleted) {
        super(id, 1);
        this.durability = durability;
        this.depleted = depleted;
    }

    public MINuclearItem(String id, int durability) {
        this(id, durability, null);
    }

    public int getDurability() {
        return durability;
    }

    @Override
    public double getDurabilityBarProgress(ItemStack itemStack) {
        return ((double)itemStack.getDamage())/durability;
    }

    @Override
    public boolean hasDurabilityBar(ItemStack itemStack) {
        return itemStack.getDamage() != 0;
    }
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        if(stack.getTag() != null) {
            Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xcbf026)).withItalic(true);
            String quantity = (getDurability() - stack.getDamage()) + " / " + getDurability();
            tooltip.add(new TranslatableText("text.modern_industrialization.burnup", quantity).setStyle(style));
        }
    }

    public Item getDepleted(){
        return depleted;
    }

}
