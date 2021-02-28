package aztech.modern_industrialization.machinesv2.helper;

import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

public class OrientationHelper {


    public static ActionResult onUse(PlayerEntity player, Hand hand,
                                     Direction face, OrientationComponent orientation, MachineBlockEntity be){
        if (orientation.onUse(player, hand, face)) {
            be.markDirty();
            if (!be.getWorld().isClient()) {
                be.sync();
            }
            return ActionResult.success(be.getWorld().isClient);
        }
        return ActionResult.PASS;
    }
}
