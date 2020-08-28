package aztech.modern_industrialization.rei;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.util.Identifier;

public class CorePlugin implements REIPluginV0 {
    @Override
    public Identifier getPluginIdentifier() {
        return new MIIdentifier("core");
    }

    @Override
    public void postRegister() {
        EntryRegistry.getInstance().removeEntry(EntryStack.create(ModernIndustrialization.ITEM_FLUID_SLOT));
    }
}
