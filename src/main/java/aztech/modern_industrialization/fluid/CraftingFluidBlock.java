package aztech.modern_industrialization.fluid;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;

/**
 * This is not a registered block, but it allows LBA to get the name of the
 * fluid by calling fluid.getBlockState().getBlock().getTranslationKey()
 */
public class CraftingFluidBlock extends Block {
    private final String translationKey;
    private final Style nameStyle;

    public CraftingFluidBlock(String name, int color) {
        super(FabricBlockSettings.of(Material.WATER));
        this.translationKey = "block.modern_industrialization." + name;
        this.nameStyle = Style.EMPTY.withColor(TextColor.fromRgb(color));
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    @Override
    public MutableText getName() {
        return new TranslatableText(this.translationKey).setStyle(nameStyle);
    }
}
