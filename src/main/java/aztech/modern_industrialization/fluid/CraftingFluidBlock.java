package aztech.modern_industrialization.fluid;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;

/**
 * This is not a registered block, but it allows LBA to get the name of the fluid by calling fluid.getBlockState().getBlock().getTranslationKey()
 */
public class CraftingFluidBlock extends Block {
    private final String translationKey;

    public CraftingFluidBlock(String name) {
        super(FabricBlockSettings.of(Material.WATER));
        this.translationKey = "block.modern_industrialization." + name;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}
