package aztech.modern_industrialization.material;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class MaterialBlock extends Block {

    private String materialId, blockType;

    public MaterialBlock(Settings settings, String materialId, String blockType) {
        super(settings);
        this.materialId = materialId;
        this.blockType = blockType;
    }

    @Environment(EnvType.CLIENT)
    public MutableText getName() {
        String blockTypeKey = "modern_industrialization:block_type:"+blockType;
        String materialKey = "modern_industrialization:material:"+materialId;
        return new TranslatableText(blockTypeKey, new TranslatableText(materialKey).getString());
    }

}
