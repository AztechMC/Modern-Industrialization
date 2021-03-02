package aztech.modern_industrialization.machinesv2.multiblocks;

import aztech.modern_industrialization.machines.impl.multiblock.HatchType;

public class HatchFlags {
    private final int flags;

    public HatchFlags(int flags) {
        this.flags = flags;
    }

    public boolean allows(HatchType type) {
        return (flags & (1 << type.getId())) > 0;
    }

    public static class Builder {
        private int flags = 0;

        public Builder with(HatchType type) {
            flags |= 1 << type.getId();
            return this;
        }

        public HatchFlags build() {
            return new HatchFlags(flags);
        }
    }
}
