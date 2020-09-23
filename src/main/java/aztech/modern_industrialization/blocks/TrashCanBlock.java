package aztech.modern_industrialization.blocks;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.AttributeProvider;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.item.ItemInsertable;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TrashCanBlock extends Block implements AttributeProvider {
    private static final ItemInsertable ITEM_TRASH = (stack, simulation) -> ItemStack.EMPTY;
    private static final FluidInsertable FLUID_TRASH = (fluidVolume, simulation) -> FluidVolumeUtil.EMPTY;

    public TrashCanBlock() {
        super(FabricBlockSettings.of(Material.METAL).hardness(6.0f).resistance(1200).sounds(BlockSoundGroup.METAL));
    }

    @Override
    public void addAllAttributes(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
        to.offer(ITEM_TRASH);
        to.offer(FLUID_TRASH);
    }
}
