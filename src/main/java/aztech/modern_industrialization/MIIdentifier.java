package aztech.modern_industrialization;

import net.minecraft.util.Identifier;

/**
 * An Identifier with the MI namespace.
 */
public class MIIdentifier extends Identifier {
    public MIIdentifier(String path) {
        super(ModernIndustrialization.MOD_ID, path);
    }
}
