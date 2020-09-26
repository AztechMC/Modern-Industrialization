package aztech.modern_industrialization.machines.special;

import alexiil.mc.lib.attributes.AttributeList;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;

public class DieselGeneratorBlockEntity extends MachineBlockEntity {
    private final EnergyExtractable extractable;
    private final CableTier tier;
    private int extraStoredEu = 0;

    public DieselGeneratorBlockEntity(MachineFactory factory, CableTier tier) {
        super(factory);

        this.tier = tier;
        extractable = buildExtractable(tier);
    }

    @Override
    protected long getMaxStoredEu() {
        return tier.getMaxInsert() * 10;
    }

    @Override
    public void addAllAttributes(AttributeList<?> to) {
        if(to.getTargetSide() == outputDirection) {
            to.offer(extractable);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("extraStoredEu", extraStoredEu);
        super.toTag(tag);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.extraStoredEu = tag.getInt("extraStoredEu");
    }

    @Override
    public void tick() {
        if(world.isClient) return;

        boolean wasActive = isActive;

        while(tier.getEu() > extraStoredEu) {
            ConfigurableFluidStack stack = fluidStacks.get(0);
            if(stack.getAmount() <= 0) break;
            int burnTicks = FluidFuelRegistry.getBurnTicks(stack.getFluid());
            if(burnTicks == 0) break;
            extraStoredEu += 32 * burnTicks;
            stack.decrement(1);
        }

        int transformed = (int) Math.min(Math.min(extraStoredEu, tier.getEu()), getMaxStoredEu() - storedEu);
        if(transformed > 0) {
            extraStoredEu -= transformed;
            storedEu += transformed;
            isActive = true;
        } else {
            isActive = false;
        }

        if(wasActive != isActive) {
            sync();
        }
        markDirty();
    }
}
