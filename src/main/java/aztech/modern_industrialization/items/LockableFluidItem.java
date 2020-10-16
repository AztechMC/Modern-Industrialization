package aztech.modern_industrialization.items;

import alexiil.mc.lib.attributes.AttributeProviderItem;
import alexiil.mc.lib.attributes.ItemAttributeList;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.misc.LimitedConsumer;
import alexiil.mc.lib.attributes.misc.Reference;
import aztech.modern_industrialization.MIItem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class LockableFluidItem extends MIItem implements AttributeProviderItem {
    public LockableFluidItem(String id) {
        super(id, 1);
    }

    public static FluidKey getFluid(ItemStack stack) {
        CompoundTag fluidTag = stack.getSubTag("fluid");
        return fluidTag == null ? FluidKeys.EMPTY : FluidKey.fromTag(fluidTag);
    }

    protected static void setFluid(ItemStack stack, FluidKey fluid) {
        if (fluid.isEmpty()) {
            stack.setTag(null);
        } else {
            stack.getOrCreateTag().put("fluid", fluid.toTag());
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        FluidKey fluid = getFluid(stack);
        if (!fluid.isEmpty()) {
            tooltip.add(fluid.name);
        }
    }

    @Override
    public void addAllAttributes(Reference<ItemStack> reference, LimitedConsumer<ItemStack> limitedConsumer, ItemAttributeList<?> to) {
        if (to.attribute == FluidAttributes.INSERTABLE) {
            to.offer((FluidInsertable) (fluidVolume, simulation) -> {
                setFluid(reference.get(), fluidVolume.getFluidKey());
                return fluidVolume;
            });
        }
    }
}
