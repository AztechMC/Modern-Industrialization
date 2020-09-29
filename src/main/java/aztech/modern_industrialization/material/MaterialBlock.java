package aztech.modern_industrialization.material;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.patchouli.common.item.PatchouliItems;

public class MaterialBlock extends Block {

    private String materialId, blockType;

    public MaterialBlock(Settings settings, String materialId, String blockType) {
        super(settings);
        this.materialId = materialId;
        this.blockType = blockType;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack handStack = player.inventory.getMainHandStack();
        if (handStack.getItem() == Items.BOOK && blockType.equals("ore")) {
            if (FabricLoader.getInstance().isModLoaded("patchouli")) {
                ItemStack guide = new ItemStack(PatchouliItems.book);
                CompoundTag tag = new CompoundTag();
                tag.putString("patchouli:book", "modern_industrialization:book");
                guide.setTag(tag);
                handStack.decrement(1);
                player.inventory.offerOrDrop(world, guide);
                return ActionResult.success(world.isClient);
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }
}
