package aztech.modern_industrialization.material;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class MaterialItem extends Item {

    private String materialId, itemType;

    public MaterialItem(Settings settings, String materialId, String itemType) {
        super(settings);
        this.materialId = materialId;
        this.itemType = itemType;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Text getName() {
        if(itemType.equals(materialId)){
            return new TranslatableText("modern_industrialization:material:"+materialId);
        }
        String itemTypeKey = "modern_industrialization:item_type:"+itemType;
        String materialKey = "modern_industrialization:material:"+materialId;
        return new TranslatableText(itemTypeKey, new TranslatableText(materialKey).getString());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Text getName(ItemStack stack) {
        return this.getName();
    }



}
