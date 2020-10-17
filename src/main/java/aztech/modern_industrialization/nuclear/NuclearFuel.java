package aztech.modern_industrialization.nuclear;

import java.util.Random;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class NuclearFuel extends MINuclearItem {

    private int multiplier;
    private NuclearFuelType type;

    public NuclearFuel(String id, NuclearFuelType type, int multiplier, Item depleted) {
        super(id, type.durability, type.maxHeat, depleted);
        this.type = type;
        this.multiplier = multiplier;
    }

    @Override
    public double getNeutronPulse(ItemStack is) {
        return type.neutronPulse * multiplier;
    }

    @Override
    public double getHeatProduction(ItemStack is, double neutronReceived) {
        return type.heatProduction * multiplier * (1.0d + neutronReceived * 0.5) * (1 - (0.5 * getHeat(is)) / getMaxHeat());
    }

    @Override
    public double getNeutronReflection(ItemStack is, int angle) {
        return 0;
    }

    @Override
    public double getHeatTransferMax(ItemStack is) {
        return 0;
    }

    @Override
    public double getHeatTransferNeighbourFraction(ItemStack is) {
        return 0;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public NuclearFuelType getType() {
        return type;
    }

    @Override
    public void tick(ItemStack is, NuclearReactorBlockEntity nuclearReactor, double neutronPulse, Random rand) {
        int damage = is.getDamage();
        damage += 1;
        damage += NuclearReactorLogic.doubleToInt(neutronPulse * 0.1, rand);
        is.setDamage(Math.min(this.getDurability(), damage));

    }
}
