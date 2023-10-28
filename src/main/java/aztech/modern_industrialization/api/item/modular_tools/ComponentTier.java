package aztech.modern_industrialization.api.item.modular_tools;

import org.jetbrains.annotations.Nullable;

public enum ComponentTier {
    LV,
    MV,
    HV,
    EV;

    public static boolean canUse(@Nullable ComponentTier maxComponentTier,
            @Nullable ComponentTier potentialComponentTier) {
        if (maxComponentTier == null) {
            return false;
        } else if (potentialComponentTier == null) {
            return true;
        } else {
            return maxComponentTier.compareTo(potentialComponentTier) >= 0;
        }
    }
}
