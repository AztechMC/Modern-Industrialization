package aztech.modern_industrialization.material;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class MaterialBlockItem extends BlockItem {

    private String materialId, blockType;

    public MaterialBlockItem(Block block, Settings settings,  String materialId, String blockType) {
        super(block, settings);
        this.materialId = materialId;
        this.blockType = blockType;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Text getName() {
        String blockTypeKey = "modern_industrialization:block_type:"+blockType;
        String materialKey = "modern_industrialization:material:"+materialId;
        return new TranslatableText(materialKey).append(" ").append(new TranslatableText(blockTypeKey));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Text getName(ItemStack stack) {
        return this.getName();
    }
}
