package aztech.modern_industrialization.machinesv2.components;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

public class OrientationComponent {
    private Direction facingDirection = Direction.NORTH;
    private Direction outputDirection = null;
    public boolean extractItems = false;
    public boolean extractFluids = false;
    public final Params params;

    public OrientationComponent(Params params) {
        this.params = params;
        if (params.hasOutput) {
            outputDirection = Direction.NORTH;
        }
    }

    public void readNbt(CompoundTag tag) {
        facingDirection = Direction.byId(tag.getInt("facingDirection"));
        if (params.hasOutput) {
            outputDirection = Direction.byId(tag.getInt("outputDirection"));
        }
        extractItems = tag.getBoolean("extractItems");
        extractFluids = tag.getBoolean("extractFluids");
    }

    public void writeNbt(CompoundTag tag) {
        tag.putInt("facingDirection", facingDirection.getId());
        if (params.hasOutput) {
            tag.putInt("outputDirection", outputDirection.getId());
            tag.putBoolean("extractItems", extractItems);
            tag.putBoolean("extractFluids", extractFluids);
        }
    }

    public void writeModelData(MachineModelClientData data) {
        data.frontDirection = facingDirection;
        if (params.hasOutput) {
            data.outputDirection = outputDirection;
            data.itemAutoExtract = extractItems;
            data.fluidAutoExtract = extractFluids;
        }
    }

    /**
     * Try to rotate the machine, and return true if something was rotated.
     */
    public boolean onUse(PlayerEntity player, Hand hand, Direction face) {
        if (player.getStackInHand(hand).getItem().isIn(ModernIndustrialization.WRENCHES)) {
            if (player.isSneaking()) {
                if (params.hasOutput) {
                    outputDirection = face;
                    return true;
                }
            } else {
                if (face.getAxis().isHorizontal()) {
                    facingDirection = face;
                }
                // We consume the event to prevent the GUI from opening.
                return true;
            }
        }
        return false;
    }

    public void onPlaced(LivingEntity placer, ItemStack itemStack) {
        Direction dir = placer.getHorizontalFacing();
        facingDirection = dir.getOpposite();
        if (params.hasOutput) {
            outputDirection = dir;
        }
    }

    public static class Params {
        public final boolean hasOutput;
        public final boolean hasExtractItems;
        public final boolean hasExtractFluids;

        public Params(boolean hasOutput, boolean hasExtractItems, boolean hasExtractFluids) {
            this.hasOutput = hasOutput;
            this.hasExtractItems = hasExtractItems;
            this.hasExtractFluids = hasExtractFluids;
        }
    }
}
