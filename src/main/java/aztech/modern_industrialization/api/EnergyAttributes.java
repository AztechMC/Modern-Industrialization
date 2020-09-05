package aztech.modern_industrialization.api;

import alexiil.mc.lib.attributes.Attribute;
import alexiil.mc.lib.attributes.Attributes;

public class EnergyAttributes {
    public static final Attribute<EnergyInsertable> INSERTABLE;
    public static final Attribute<EnergyExtractable> EXTRACTABLE;

    static {
        INSERTABLE = Attributes.create(EnergyInsertable.class);
        EXTRACTABLE = Attributes.create(EnergyExtractable.class);
    }
}
