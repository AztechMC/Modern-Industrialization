package aztech.modern_industrialization.machinesv2.components;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machinesv2.IComponent;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeTemplate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

public class ActiveShapeComponent implements IComponent {
    public final ShapeTemplate[] shapeTemplates;
    private int activeShape = 0;

    public ActiveShapeComponent(ShapeTemplate[] shapeTemplates) {
        this.shapeTemplates = shapeTemplates;
    }

    public ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        Item handItem = player.getStackInHand(hand).getItem();
        if (handItem == ModernIndustrialization.ITEM_SCREWDRIVER && shapeTemplates.length > 1) {
            activeShape = (activeShape + 1) % shapeTemplates.length;
            return ActionResult.success(player.getEntityWorld().isClient());
        }
        return ActionResult.PASS;
    }

    public ShapeTemplate getActiveShape() {
        return shapeTemplates[activeShape];
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.putInt("activeShape", activeShape);
    }

    @Override
    public void readNbt(CompoundTag tag) {
        activeShape = tag.getInt("activeShape");
    }
}
