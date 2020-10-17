package aztech.modern_industrialization.nuclear;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface NuclearReactorComponent {

    public double getNeutronPulse(ItemStack is);

    public double getHeatProduction(ItemStack is, double neutronReceived);

    public double getNeutronReflection(ItemStack is ,int angle); // 0 = 0°, 1 = 90°, 2 = 180°

    public double getHeatTransferMax(ItemStack is);

    public double getHeatTransferNeighbourFraction(ItemStack is); // the rest of heat is transfered to itself

}
