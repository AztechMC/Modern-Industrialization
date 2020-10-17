package aztech.modern_industrialization.nuclear;

import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.items.LockableFluidItem;
import java.util.Random;
import net.minecraft.item.ItemStack;

public class NuclearCoolant extends MINuclearItem {

    private int multiplier;

    public NuclearCoolant(String id, int maxHeat, int multiplier) {
        super(id, -1, maxHeat);
        this.multiplier = multiplier;
        setFluidLockable(true);
    }

    @Override
    public void tick(ItemStack is, NuclearReactorBlockEntity nuclearReactor, double neutronPulse, Random rand) {
        NuclearFluidCoolant coolant = getCoolant(is);
        if (coolant != null) {
            int extractMax = nuclearReactor.getMaxFluidExtraction(coolant.fluid);
            int insertMax = nuclearReactor.getMaxFluidInsertion(coolant.fluidResult);
            int s = Math.min(extractMax, insertMax);
            int u = Math.min(s, 64 * multiplier);
            int v = Math.min(u, getHeat(is) / coolant.heatConsumed);
            setHeat(is, getHeat(is) - v * coolant.heatConsumed);
            int r = nuclearReactor.extractFluidFromInputHatch(FluidKeys.WATER, v);
            if (r != 0) {
                throw new IllegalStateException("Remaining extracted fluid : " + r);
            }
            r = nuclearReactor.insertFluidInOutputHatch(MIFluids.STEAM.key, v);
            if (r != 0) {
                throw new IllegalStateException("Remaining inserted fluid : " + r);
            }
        }
    }

    public NuclearFluidCoolant getCoolant(ItemStack is) {
        for (NuclearFluidCoolant coolant : NuclearFluidCoolant.values()) {
            if (coolant.fluid == LockableFluidItem.getFluid(is)) {
                return coolant;
            }
        }
        return null;
    }

    @Override
    public double getNeutronPulse(ItemStack is) {
        return 0;
    }

    @Override
    public double getHeatProduction(ItemStack is, double neutronReceived) {
        return 0;
    }

    @Override
    public double getNeutronReflection(ItemStack is, int angle) {
        NuclearFluidCoolant coolant = getCoolant(is);
        if (coolant != null) {
            return coolant.neutronReflection * 0.25;
        }
        return 0;
    }

    @Override
    public double getHeatTransferMax(ItemStack is) {
        NuclearFluidCoolant coolant = getCoolant(is);
        if (coolant != null) {
            return coolant.heatTransfer * multiplier;
        }
        return 0;
    }

    @Override
    public double getHeatTransferNeighbourFraction(ItemStack is) {
        return 0;
    }

    public int getMultiplier() {
        return multiplier;
    }
}
