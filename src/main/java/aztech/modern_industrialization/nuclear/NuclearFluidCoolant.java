package aztech.modern_industrialization.nuclear;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import aztech.modern_industrialization.MIFluids;

public enum NuclearFluidCoolant {

    WATER(FluidKeys.WATER, MIFluids.STEAM.key, 1, 128, 0.2);

    public final FluidKey fluid;
    public final FluidKey fluidResult;
    public int heatConsumed;
    public double heatTransfer;
    public double neutronReflection;

    NuclearFluidCoolant(FluidKey fluid, FluidKey fluidResult, int heatConsumed, double heatTransfer, double neutronReflection) {
        this.fluid = fluid;
        this.fluidResult = fluidResult;
        this.heatConsumed = heatConsumed;
        this.heatTransfer = heatTransfer;
        this.neutronReflection = neutronReflection;
    }
}
