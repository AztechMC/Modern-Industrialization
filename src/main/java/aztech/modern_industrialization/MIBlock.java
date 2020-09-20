package aztech.modern_industrialization;

import aztech.modern_industrialization.machines.impl.multiblock.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.material.MIMaterialSetup;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;

public class MIBlock extends Block {


    private String id;
    public static HashMap<String, MIBlock> blocks = new HashMap<String, MIBlock>();
    private BlockItem blockItem;

    public MIBlock(String id, Settings settings) {
        super(settings);
        if (blocks.containsKey(id)) {
            throw new IllegalArgumentException("Item id already taken : " + id);
        } else {
            this.id = id;
            blocks.put(id, this);
            blockItem = new BlockItem(this, new Item.Settings().group(ModernIndustrialization.ITEM_GROUP));
        }
    }

    public MIBlock(String id) {
        this(id, FabricBlockSettings.of(MIMaterialSetup.METAL_MATERIAL).
                hardness(4.0f).breakByTool(FabricToolTags.PICKAXES).requiresTool());
    }

    public String getId() {
        return id;
    }

    public BlockItem getItem() {
        return blockItem;
    }

    public static final MIBlock BLOCK_FIRE_CLAY_BRICKS = new MIBlock("fire_clay_bricks",
            FabricBlockSettings.of(MIMaterialSetup.STONE_MATERIAL).hardness(2.0f)
                    .resistance(6.0f)
                    .breakByTool(FabricToolTags.PICKAXES, 0)
                    .requiresTool());

    public static final MIBlock STEEL_MACHINE_CASING = new MIBlock("steel_machine_casing");
    public static final MIBlock STEEL_MACHINE_CASING_PIPE = new MIBlock("steel_machine_casing_pipe");
    public static final MIBlock BASIC_MACHINE_HULL = new MIBlock("lv_machine_hull");
    public static final MIBlock BRONZE_PLATED_BRICKS = new MIBlock("bronze_plated_bricks");
    public static final MIBlock BRONZE_MACHINE_CASING = new MIBlock("bronze_machine_casing");
    public static final MIBlock BRONZE_MACHINE_CASING_PIPE = new MIBlock("bronze_machine_casing_pipe");
    public static final MIBlock ADVANCED_MACHINE_CASING = new MIBlock("advanced_machine_casing");
    public static final MIBlock HEATPROOF_MACHINE_CASING = new MIBlock("heatproof_machine_casing");
    public static final MIBlock ADVANCED_MACHINE_HULL = new MIBlock("advanced_machine_hull");
}
